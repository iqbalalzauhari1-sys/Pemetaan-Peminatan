from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List, Dict, Any

# Import langsung dari file root
from database import get_db
import models
from schemas import UpdateNilaiRequest, EksekusiPeminatanResponse

router = APIRouter(prefix="/api/peminatan", tags=["Peminatan SAW & Greedy"])

# ==========================================
# HELPER ALGORITMA SAW & GREEDY
# ==========================================

def normalize_weights(kriteria_list: List[Dict[str, Any]]) -> Dict[str, float]:
    """Memastikan total bobot kriteria tepat 1.0 (Auto-Normalize)"""
    total_bobot = sum(k['bobot'] for k in kriteria_list)
    if total_bobot <= 0:
        raise ValueError("Total bobot kriteria harus lebih besar dari 0")
    return {k['kriteria_id']: k['bobot'] / total_bobot for k in kriteria_list}


def hitung_saw(siswa_data: List[Dict[str, Any]], kriteria_list: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
    """Menghitung skor preferensi SAW (Aman dari ZeroDivisionError)"""
    if not siswa_data or not kriteria_list:
        return []

    bobot_normalized = normalize_weights(kriteria_list)
    
    max_min_val = {}
    for k in kriteria_list:
        k_id = k['kriteria_id']
        vals = [s['nilai'][k_id] for s in siswa_data if k_id in s['nilai']]
        
        max_val = max(vals) if vals else 0.0
        min_val = min(vals) if vals else 0.0

        max_min_val[k_id] = {
            'max': max_val if max_val > 0 else 1.0,
            'min': min_val if min_val > 0 else 1.0
        }

    nila_preferensi = []
    for s in siswa_data:
        v_score = 0.0
        for k in kriteria_list:
            k_id = k['kriteria_id']
            val = s['nilai'].get(k_id, 0.0)
            tipe = k.get('tipe', 'benefit')

            if tipe == 'benefit':
                r_ij = val / max_min_val[k_id]['max']
            else:
                r_ij = max_min_val[k_id]['min'] / val if val > 0 else 0.0
            
            v_score += r_ij * bobot_normalized[k_id]

        siswa_result = s.copy()
        siswa_result['skor_saw'] = round(v_score, 4)
        # Tie-breaker sekunder dari total hasil psikotes
        siswa_result['psikotes_total'] = s.get('nilai_literasi_psikotes', 0) + s.get('nilai_numerasi_psikotes', 0)
        nila_preferensi.append(siswa_result)

    return nila_preferensi


def alokasi_kuota_greedy(siswa_ranked: List[Dict[str, Any]], kuota_kelas: Dict[str, int]) -> List[Dict[str, Any]]:
    """Alokasi Kuota Kelas dengan Algoritma Greedy & Aturan Tie-Breaker"""
    # Pengurutan: 1. Skor SAW (desc), 2. Total Psikotes (desc), 3. ID Siswa (asc)
    sorted_siswa = sorted(
        siswa_ranked,
        key=lambda x: (-x['skor_saw'], -x.get('psikotes_total', 0), x['siswa_id'])
    )

    sisa_kuota = kuota_kelas.copy()
    hasil_alokasi = []

    for s in sorted_siswa:
        pilihan_1 = s.get('pilihan_1')
        pilihan_2 = s.get('pilihan_2')
        jurusan_diterima = None

        if pilihan_1 in sisa_kuota and sisa_kuota[pilihan_1] > 0:
            jurusan_diterima = pilihan_1
            sisa_kuota[pilihan_1] -= 1
        elif pilihan_2 in sisa_kuota and sisa_kuota[pilihan_2] > 0:
            jurusan_diterima = pilihan_2
            sisa_kuota[pilihan_2] -= 1
        else:
            jurusan_diterima = "Belum Ter-alokasi (Kuota Penuh)"

        hasil_alokasi.append({
            "siswa_id": s['siswa_id'],
            "nama_siswa": s['nama_siswa'],
            "skor_saw": s['skor_saw'],
            "jurusan_diterima": jurusan_diterima,
            "alasan": f"Diterima di {jurusan_diterima}" if jurusan_diterima in [pilihan_1, pilihan_2] else "Kuota penuh"
        })

    return hasil_alokasi


# ==========================================
# ENDPOINT ROUTER
# ==========================================

@router.post("/eksekusi", response_model=EksekusiPeminatanResponse)
def eksekusi_peminatan_siswa(db: Session = Depends(get_db)):
    try:
        # Transaksi Atomic
        with db.begin():
            kriteria_db = db.query(models.Kriteria).all()
            if not kriteria_db:
                raise HTTPException(status_code=400, detail="Data kriteria peminatan belum diset.")
            
            kriteria_list = [
                {"kriteria_id": str(k.id), "bobot": k.bobot, "tipe": k.tipe}
                for k in kriteria_db
            ]

            siswa_db = db.query(models.Siswa).filter(models.Siswa.is_active == True).all()
            if not siswa_db:
                raise HTTPException(status_code=400, detail="Tidak ada data siswa aktif yang diproses.")

            data_siswa_input = []
            for s in siswa_db:
                data_siswa_input.append({
                    "siswa_id": s.id,
                    "nama_siswa": s.nama_lengkap,
                    "pilihan_1": s.pilihan_jurusan_1,
                    "pilihan_2": s.pilihan_jurusan_2,
                    "nilai_literasi_psikotes": s.nilai_literasi_psikotes,
                    "nilai_numerasi_psikotes": s.nilai_numerasi_psikotes,
                    "nilai": {
                        "literasi": s.nilai_literasi_psikotes,
                        "numerasi": s.nilai_numerasi_psikotes,
                        "raport": s.nilai_rata_rata_raport
                    }
                })

            siswa_ranked = hitung_saw(data_siswa_input, kriteria_list)
            kuota_db = db.query(models.KuotaKelas).all()
            kuota_map = {k.nama_jurusan: k.kuota_tersedia for k in kuota_db}

            hasil_alokasi = alokasi_kuota_greedy(siswa_ranked, kuota_map)

            for item in hasil_alokasi:
                hasil_record = db.query(models.HasilPeminatan).filter_by(siswa_id=item['siswa_id']).first()
                if not hasil_record:
                    hasil_record = models.HasilPeminatan(siswa_id=item['siswa_id'])
                    db.add(hasil_record)
                
                hasil_record.skor_saw = item['skor_saw']
                hasil_record.jurusan_diterima = item['jurusan_diterima']
                hasil_record.keterangan = item['alasan']

        return EksekusiPeminatanResponse(
            status="success",
            message="Eksekusi peminatan siswa berhasil.",
            total_diproses=len(siswa_db),
            berhasil_dialokasikan=len([h for h in hasil_alokasi if "Belum" not in h['jurusan_diterima']])
        )

    except HTTPException:
        raise
    except Exception as e:
        db.rollback()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Terjadi kesalahan saat memproses eksekusi: {str(e)}"
        )