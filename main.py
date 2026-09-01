from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session
from sqlalchemy import or_
import models, schemas
from database import engine, SessionLocal
from fastapi.responses import StreamingResponse
import pandas as pd
import io

models.Base.metadata.create_all(bind=engine)

app = FastAPI(title="API Pemetaan Peminatan Siswa Super Algoritma")

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

@app.get("/")
def read_root():
    return {"pesan": "Server Backend Penjurusan 2.0 (With Class Distribution) MENYALA!"}

@app.post("/register/")
def register_siswa(data: schemas.PendaftaranSiswa, db: Session = Depends(get_db)):
    cek_user = db.query(models.User).filter(models.User.username == data.nisn).first()
    if cek_user:
        raise HTTPException(status_code=400, detail="NISN sudah terdaftar!")

    db_user = models.User(username=data.nisn, password=data.password, role="siswa")
    db.add(db_user)
    db.commit()
    db.refresh(db_user) 

    # Simpan Profil Awal (Hanya NISN dan Nama)
    db_siswa = models.Siswa(
        id_user=db_user.id_user,
        nisn=data.nisn,
        nama_lengkap=data.nama_lengkap
        # Field alamat, data ortu, dll otomatis bernilai NULL di database
    )
    db.add(db_siswa)
    db.commit()
    
    return {"status": "Sukses", "pesan": "Registrasi berhasil!"}

@app.post("/login/")
def login_user(data: schemas.LoginUser, db: Session = Depends(get_db)):
    user = db.query(models.User).filter(models.User.username == data.nisn).first()
    if not user or user.password != data.password:
        raise HTTPException(status_code=401, detail="NISN atau Password salah!")
    return {"status": "Sukses", "pesan": "Login berhasil!", "role": user.role, "nisn": user.username}

@app.get("/profil-siswa/{nisn}")
def profil_siswa(nisn: str, db: Session = Depends(get_db)):
    siswa = db.query(models.Siswa).filter(models.Siswa.nisn == nisn).first()
    if not siswa:
        raise HTTPException(status_code=404, detail="Siswa tidak ditemukan!")
    return siswa

@app.put("/update-profil/")
def update_profil_siswa(data: schemas.UpdateProfil, db: Session = Depends(get_db)):
    siswa = db.query(models.Siswa).filter(models.Siswa.nisn == data.nisn).first()
    if not siswa:
        raise HTTPException(status_code=404, detail="Siswa tidak ditemukan!")
    
    siswa.tempat_tanggal_lahir = data.tempat_tanggal_lahir
    siswa.alamat_siswa = data.alamat_siswa
    siswa.status_keluarga = data.status_keluarga
    siswa.anak_ke = data.anak_ke
    siswa.asal_sekolah = data.asal_sekolah
    siswa.nama_ayah = data.nama_ayah
    siswa.pekerjaan_ayah = data.pekerjaan_ayah
    siswa.nama_ibu = data.nama_ibu
    siswa.pekerjaan_ibu = data.pekerjaan_ibu
    siswa.alamat_ortu = data.alamat_ortu
    siswa.no_telp_ortu = data.no_telp_ortu
    # Simpan data wali
    siswa.nama_wali = data.nama_wali
    siswa.pekerjaan_wali = data.pekerjaan_wali
    siswa.no_telp_wali = data.no_telp_wali
    siswa.alamat_wali = data.alamat_wali
    
    db.commit()
    return {"status": "Sukses", "pesan": "Profil berhasil diperbarui!"}

@app.put("/update-nilai/")
def update_nilai_siswa(data: schemas.UpdateNilai, db: Session = Depends(get_db)):
    siswa = db.query(models.Siswa).filter(models.Siswa.nisn == data.nisn).first()
    if not siswa:
        raise HTTPException(status_code=404, detail="Siswa tidak ditemukan!")

    if siswa.nilai_raport is not None:
        raise HTTPException(status_code=400, detail="Data sudah terkunci! Hubungi Admin untuk mengubah data.")
    
    siswa.nilai_raport = data.nilai_raport
    siswa.nilai_literasi = data.nilai_literasi
    siswa.nilai_numerasi = data.nilai_numerasi
    siswa.id_angket_pilihan = data.id_angket_pilihan
    siswa.status_validasi_nilai = "Menunggu Proses Pemetaan"
    db.commit()
    return {"status": "Sukses", "pesan": "Data akademik berhasil disimpan!"}

@app.put("/buka-kunci-nilai/{nisn}")
def buka_kunci_nilai(nisn: str, db: Session = Depends(get_db)):
    siswa = db.query(models.Siswa).filter(models.Siswa.nisn == nisn).first()
    if not siswa:
        raise HTTPException(status_code=404, detail="Siswa tidak ditemukan!")
    
    # Reset semua nilai akademik agar siswa bisa input ulang
    siswa.nilai_raport = None
    siswa.nilai_literasi = None
    siswa.nilai_numerasi = None
    siswa.id_angket_pilihan = None
    siswa.status_validasi_nilai = "Belum Mengisi Data"
    siswa.skor_spk = 0.0
    siswa.id_kelas_diterima = None
    
    db.commit()
    return {"status": "Sukses", "pesan": f"Akses input nilai untuk {siswa.nama_lengkap} telah dibuka!"}

# Endpoint ADMIN: Membuat Jurusan beserta Kelas dan Kuota Dinamis
@app.post("/setup-peminatan/")
def setup_peminatan(data: schemas.InputPeminatan, db: Session = Depends(get_db)):
    total_kuota = sum([k.kuota_kelas for k in data.daftar_kelas])
    nama_pem_upper = data.nama_peminatan.upper()
    
    # 1. Cek apakah jurusan sudah ada
    jurusan = db.query(models.Peminatan).filter(models.Peminatan.nama_peminatan == nama_pem_upper).first()
    
    if not jurusan:
        # Jika belum ada, buat baru
        jurusan = models.Peminatan(nama_peminatan=nama_pem_upper, kapasitas_total=total_kuota)
        db.add(jurusan)
        db.commit()
        db.refresh(jurusan)
    else:
        # Jika sudah ada, update total kuotanya
        jurusan.kapasitas_total = total_kuota
        db.commit()

    # 2. Hapus data kelas lama milik jurusan ini (Reset) agar tidak menumpuk
    db.query(models.Kelas).filter(models.Kelas.id_peminatan == jurusan.id_peminatan).delete()
    
    # 3. Masukkan kelas yang baru
    for kelas in data.daftar_kelas:
        kelas_baru = models.Kelas(
            nama_kelas=kelas.nama_kelas, 
            id_peminatan=jurusan.id_peminatan, 
            kuota_kelas=kelas.kuota_kelas
        )
        db.add(kelas_baru)
    
    db.commit()
    return {"pesan": f"Jurusan {jurusan.nama_peminatan} dengan total kuota {total_kuota} berhasil diatur!"}

# SUPER ALGORITMA: SPK + Distribusi Massal (1 Tombol)
@app.post("/eksekusi-penjurusan-global/")
def eksekusi_penjurusan_global(db: Session = Depends(get_db)):
    semua_siswa = db.query(models.Siswa).filter(models.Siswa.nilai_raport != None).all()
    if not semua_siswa:
        raise HTTPException(status_code=400, detail="Belum ada siswa yang mengisi nilai akademik.")

    # 1. Normalisasi (SAW) - Cari Nilai Maksimal Global
    max_raport = max([s.nilai_raport for s in semua_siswa])
    max_literasi = max([s.nilai_literasi for s in semua_siswa])
    max_numerasi = max([s.nilai_numerasi for s in semua_siswa])

    # 2. Hitung Skor SPK Berdasarkan Jurusan Pilihan Siswa
    for s in semua_siswa:
        # Kita asumsikan r_angket = 1.0 (karena ini memproses pilihan utama mereka)
        r_angket = 1.0
        r_raport = s.nilai_raport / max_raport if max_raport > 0 else 0
        r_literasi = s.nilai_literasi / max_literasi if max_literasi > 0 else 0
        r_numerasi = s.nilai_numerasi / max_numerasi if max_numerasi > 0 else 0

        s.skor_spk = (0.70 * r_angket) + (0.15 * r_raport) + (0.075 * r_literasi) + (0.075 * r_numerasi)
        s.id_kelas_diterima = None # Reset kelas lama jika ada
        s.status_validasi_nilai = "Menunggu Proses"

    # 3. Sorting (Greedy) - Urutkan dari skor tertinggi ke terendah secara global
    siswa_sorted = sorted(semua_siswa, key=lambda x: x.skor_spk, reverse=True)

    # 4. Siapkan Keranjang Kelas
    semua_kelas = db.query(models.Kelas).all()
    sisa_kuota = {k.id_kelas: {"nama": k.nama_kelas, "sisa": k.kuota_kelas, "id_pem": k.id_peminatan} for k in semua_kelas}

    hasil_diterima = []
    
    # 5. Distribusikan ke Kelas
    for s in siswa_sorted:
        pilihan_jurusan = s.id_angket_pilihan
        if not pilihan_jurusan:
            s.status_validasi_nilai = "DITOLAK (TIDAK MEMILIH JURUSAN)"
            continue

        kelas_terpilih = None
        # Cari kelas yang jurusannya cocok dengan pilihan siswa DAN kuotanya masih sisa
        for k_id, k_info in sisa_kuota.items():
            if k_info["id_pem"] == pilihan_jurusan and k_info["sisa"] > 0:
                kelas_terpilih = k_id
                break

        if kelas_terpilih:
            s.id_kelas_diterima = kelas_terpilih
            s.status_validasi_nilai = f"DITERIMA DI {sisa_kuota[kelas_terpilih]['nama']}"
            sisa_kuota[kelas_terpilih]["sisa"] -= 1 # Kurangi kuota kelas
            
            if len(hasil_diterima) < 20:  # Batasi hanya menampilkan 20 siswa teratas di hasil_diterima
                hasil_diterima.append({"nama": s.nama_lengkap, "kelas": sisa_kuota[kelas_terpilih]['nama'], "skor_akhir": round(s.skor_spk, 4)})
        else:
            # AMBIL NAMA JURUSAN AGAR TERTULIS DI DATABASE
            pem = db.query(models.Peminatan).filter(models.Peminatan.id_peminatan == pilihan_jurusan).first()
            nama_pem = pem.nama_peminatan if pem else "JURUSAN"
            
            s.status_validasi_nilai = f"DITOLAK DARI {nama_pem} (KUOTA PENUH)"

    db.commit()
    return {
        "status": "Sukses",
        "jurusan": "Distribusi Massal (Semua Peminatan)",
        "kuota_tersedia": sum([k["sisa"] for k in sisa_kuota.values()]),
        "siswa_diterima": hasil_diterima
    }

# Endpoint sementara untuk Admin (Tanpa Auth JWT untuk testing)
@app.put("/jadikan-admin/{nisn}")
def jadikan_admin(nisn: str, db: Session = Depends(get_db)):
    user = db.query(models.User).filter(models.User.username == nisn).first()
    if user:
        user.role = "admin"
        db.commit()
        return {"pesan": f"Akun {nisn} sekarang ADMIN."}

# ==========================================
# ENDPOINT UNTUK FILTERING KELAS (ADMIN)
# ==========================================

@app.get("/daftar-kelas/")
def get_daftar_kelas(db: Session = Depends(get_db)):
    # Ambil semua data kelas
    semua_kelas = db.query(models.Kelas).all()
    
    hasil = []
    for k in semua_kelas:
        # Hitung berapa banyak siswa yang sudah masuk ke kelas ini
        jumlah_siswa = db.query(models.Siswa).filter(models.Siswa.id_kelas_diterima == k.id_kelas).count()
        
        hasil.append({
            "id_kelas": k.id_kelas,
            "nama_kelas": k.nama_kelas,
            "kuota_maksimal": k.kuota_kelas,
            "terisi": jumlah_siswa
        })
        
    return hasil

@app.get("/kelas/{id_kelas}/siswa")
def get_siswa_per_kelas(id_kelas: int, db: Session = Depends(get_db)):
    # Ambil data kelas untuk judul
    kelas = db.query(models.Kelas).filter(models.Kelas.id_kelas == id_kelas).first()
    if not kelas:
        raise HTTPException(status_code=404, detail="Kelas tidak ditemukan")

    # Ambil semua siswa yang memiliki id_kelas_diterima sama dengan id_kelas
    daftar_siswa = db.query(models.Siswa).filter(models.Siswa.id_kelas_diterima == id_kelas).order_by(models.Siswa.skor_spk.desc()).all()
    
    hasil_siswa = []
    for s in daftar_siswa:
        hasil_siswa.append({
            "nisn": s.nisn,
            "nama_lengkap": s.nama_lengkap,
            "skor": round(s.skor_spk, 4),
            "asal_sekolah": s.asal_sekolah
        })
        
    return {
        "nama_kelas": kelas.nama_kelas,
        "jumlah_siswa": len(hasil_siswa),
        "data_siswa": hasil_siswa
    }

@app.get("/siswa-ditolak/")
def get_siswa_ditolak(db: Session = Depends(get_db)):
    daftar_siswa = db.query(models.Siswa).filter(models.Siswa.status_validasi_nilai.contains("DITOLAK")).order_by(models.Siswa.skor_spk.desc()).all()
    
    hasil_siswa = []
    for s in daftar_siswa:
        # Cari tau jurusan apa yang dia pilih namun gagal
        pilihan = db.query(models.Peminatan).filter(models.Peminatan.id_peminatan == s.id_angket_pilihan).first()
        nama_pilihan = pilihan.nama_peminatan if pilihan else "Tidak Memilih"
        
        hasil_siswa.append({
            "nisn": s.nisn,
            "nama_lengkap": s.nama_lengkap,
            "skor": round(s.skor_spk, 4),
            # TRIK CERDAS: Kita sisipkan info jurusan di variabel ini agar langsung muncul di Card Android
            "asal_sekolah": f"{s.asal_sekolah}<br><b>Peminatan Yang di Pilih: {nama_pilihan}</b>"
        })
        
    return {
        "nama_kelas": "Daftar Siswa Tertolak",
        "jumlah_siswa": len(hasil_siswa),
        "data_siswa": hasil_siswa
    }

# ==========================================
# FITUR LIFETIME (UNDUH & RESET)
# ==========================================
@app.get("/export-laporan/")
def export_laporan(db: Session = Depends(get_db)):
    # Ambil seluruh referensi master data
    siswa_all = db.query(models.Siswa).order_by(models.Siswa.nama_lengkap.asc()).all()
    kelas_all = {k.id_kelas: k.nama_kelas for k in db.query(models.Kelas).all()}
    pem_all = {p.id_peminatan: p.nama_peminatan for p in db.query(models.Peminatan).all()}
    users_all = {u.id_user: u for u in db.query(models.User).all()}

    data_sebelum = []
    data_setelah = []
    akun_siswa = []
    
    for s in siswa_all:
        nama_pem = pem_all.get(s.id_angket_pilihan, "Belum Memilih")
        nama_kelas = kelas_all.get(s.id_kelas_diterima, "Belum Ada")
        user_info = users_all.get(s.id_user)
        status_akhir = s.status_validasi_nilai if s.status_validasi_nilai else "Belum Dicek"
        alamat_orang_tua = s.alamat_siswa if s.alamat_ortu == "Sama dengan siswa" else s.alamat_ortu

        # 1. Base Biodata (Tanpa hasil SPK)
        base_dict = {
            "NISN": s.nisn,
            "Nama Lengkap": s.nama_lengkap,
            "Tempat Tanggal Lahir": s.tempat_tanggal_lahir,
            "Alamat": s.alamat_siswa,
            "Status Keluarga": s.status_keluarga,
            "Anak Ke": s.anak_ke,
            "Asal Sekolah": s.asal_sekolah,
            "Nama Ayah": s.nama_ayah,
            "Pekerjaan Ayah": s.pekerjaan_ayah,
            "Nama Ibu": s.nama_ibu,
            "Pekerjaan Ibu": s.pekerjaan_ibu,
            "Alamat Ortu": alamat_orang_tua,
            "No Telp Ortu": s.no_telp_ortu,
            "Nama Wali": s.nama_wali,
            "Pekerjaan Wali": s.pekerjaan_wali,
            "Alamat Wali": s.alamat_wali,
            "No Telp Wali": s.no_telp_wali,
            "Nilai Raport": s.nilai_raport,
            "Nilai Literasi": s.nilai_literasi,
            "Nilai Numerasi": s.nilai_numerasi,
            "Pilihan Jurusan": nama_pem
        }
        data_sebelum.append(base_dict.copy())
        
        # 2. Data Setelah Diolah (+ SPK dan Kelas)
        setelah_dict = base_dict.copy()
        setelah_dict["Skor"] = round(s.skor_spk, 4) if s.skor_spk else 0.0
        setelah_dict["Status Kelulusan"] = status_akhir
        setelah_dict["Kelas Penempatan"] = nama_kelas
        data_setelah.append(setelah_dict)
        
        # 3. Data Akun Siswa
        if user_info and user_info.role == "siswa":
            akun_siswa.append({
                "Nama Lengkap": s.nama_lengkap,
                "Username (NISN)": user_info.username,
                "Password": user_info.password
            })
            
    # Ubah menjadi DataFrame Pandas
    df_sebelum = pd.DataFrame(data_sebelum)
    df_setelah = pd.DataFrame(data_setelah)
    df_akun = pd.DataFrame(akun_siswa)

    # Tulis ke dalam File Excel (.xlsx) dengan multi-sheet dinamis
    buffer = io.BytesIO()
    with pd.ExcelWriter(buffer, engine='openpyxl') as writer:
        # Sheet 1 & 2: Data Utama
        df_sebelum.to_excel(writer, index=False, sheet_name="Data Mentah")
        df_setelah.to_excel(writer, index=False, sheet_name="Data Setelah Diolah")
        
        # Memisahkan sheet berdasarkan Jurusan dan Kelas
        if not df_setelah.empty:
            df_lolos = df_setelah[df_setelah["Status Kelulusan"].str.contains("DITERIMA", na=False)]
            
            # Buat sheet otomatis untuk setiap Jurusan
            for jurusan, group in df_lolos.groupby("Pilihan Jurusan"):
                nama_sheet = f"Jurusan {jurusan}"[:31] # Excel membatasi nama sheet maksimal 31 karakter
                group.sort_values(by="Skor", ascending=False).to_excel(writer, index=False, sheet_name=nama_sheet)
                
            # Buat sheet otomatis untuk setiap Kelas
            for kelas, group in df_lolos.groupby("Kelas Penempatan"):
                nama_sheet = f"Kelas {kelas}"[:31] 
                group.sort_values(by="Skor", ascending=False).to_excel(writer, index=False, sheet_name=nama_sheet)
                
        # Sheet Terakhir: Akun Login
        df_akun.to_excel(writer, index=False, sheet_name="Akun Login Siswa")
        
    buffer.seek(0)
    
    headers = {"Content-Disposition": "attachment; filename=Laporan_Lengkap_Pemetan_siswa.xlsx"}
    return StreamingResponse(buffer, media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", headers=headers)

@app.delete("/reset-tahun-ajaran/")
def reset_tahun_ajaran(db: Session = Depends(get_db)):
    # 1. Hapus seluruh data profil Siswa
    db.query(models.Siswa).delete()
    # 2. Hapus seluruh akun Login yang ber-role "siswa"
    db.query(models.User).filter(models.User.role == "siswa").delete()
    
    db.commit()
    return {"status": "Sukses", "pesan": "Sistem berhasil dibersihkan dan siap untuk tahun ajaran baru!"}

@app.get("/cari-siswa/")
def cari_siswa_global(q: str = "", db: Session = Depends(get_db)):
    if not q:
        return []
    
    # Cari berdasarkan nama (tidak peduli huruf besar/kecil) atau NISN
    hasil = db.query(models.Siswa).filter(
        models.Siswa.nama_lengkap.ilike(f"%{q}%") | 
        models.Siswa.nisn.contains(q)
    ).limit(20).all() # Batasi 20 nama agar aplikasi tidak berat
    
    return hasil