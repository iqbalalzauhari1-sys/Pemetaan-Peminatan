from pydantic import BaseModel, Field, field_validator
from typing import Optional, List

# Skema JSON saat Siswa Mendaftar
class PendaftaranSiswa(BaseModel):
    nisn: str
    password: str
    nama_lengkap: str

# Skema JSON saat Login
class LoginUser(BaseModel):
    nisn: str
    password: str

# Skema JSON saat Input Nilai
class UpdateNilai(BaseModel):
    nisn: str
    nilai_raport: float
    nilai_literasi: float
    nilai_numerasi: float
    id_angket_pilihan: int

# Skema JSON untuk Admin: Setup Kelas & Jurusan
class InputKelas(BaseModel):
    nama_kelas: str
    kuota_kelas: int

class InputPeminatan(BaseModel):
    nama_peminatan: str
    daftar_kelas: List[InputKelas]

# Skema JSON saat Siswa Melengkapi Profil
class UpdateProfil(BaseModel):
    nisn: str
    tempat_tanggal_lahir: str
    alamat_siswa: str
    status_keluarga: str
    anak_ke: int
    asal_sekolah: str
    nama_ayah: str
    pekerjaan_ayah: str
    nama_ibu: str
    pekerjaan_ibu: str
    alamat_ortu: str
    no_telp_ortu: str
    # Data tambahan opsional
    nama_wali: Optional[str] = None
    pekerjaan_wali: Optional[str] = None
    no_telp_wali: Optional[str] = None
    alamat_wali: Optional[str] = None

class UpdateNilaiRequest(BaseModel):
    siswa_id: int
    # Hasil psikotes literasi dan numerasi (0 - 100)
    nilai_literasi_psikotes: float = Field(..., ge=0.0, le=100.0, description="Nilai Tes Literasi (Psikotes)")
    nilai_numerasi_psikotes: float = Field(..., ge=0.0, le=100.0, description="Nilai Tes Numerasi (Psikotes)")
    
    # Nilai pendukung lain jika ada
    nilai_rata_rata_raport: float = Field(..., ge=0.0, le=100.0, description="Rata-rata Nilai Raport")

    @field_validator('nilai_literasi_psikotes', 'nilai_numerasi_psikotes', 'nilai_rata_rata_raport')
    @classmethod
    def check_valid_score(cls, v: float) -> float:
        if v < 0 or v > 100:
            raise ValueError("Nilai harus berada dalam rentang 0 hingga 100")
        return round(v, 2)


class KriteriaBobotSchema(BaseModel):
    kriteria_id: str
    nama_kriteria: str
    bobot: float = Field(..., ge=0.0, le=1.0)
    tipe: str = Field(..., pattern="^(benefit|cost)$")


class EksekusiPeminatanResponse(BaseModel):
    status: str
    message: str
    total_diproses: int
    berhasil_dialokasikan: int