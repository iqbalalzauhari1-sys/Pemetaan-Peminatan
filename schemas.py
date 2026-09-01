from pydantic import BaseModel
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
    nama_wali: str | None = None
    pekerjaan_wali: str | None = None
    no_telp_wali: str | None = None
    alamat_wali: str | None = None