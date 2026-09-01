from sqlalchemy import Column, Integer, String, Float, ForeignKey
from sqlalchemy.orm import relationship
from database import Base

# Tabel untuk Autentikasi (Siswa & Admin)
class User(Base):
    __tablename__ = "tb_users"

    id_user = Column(Integer, primary_key=True, index=True)
    username = Column(String, unique=True, index=True) # NISN atau NIP
    password = Column(String)
    role = Column(String, default="siswa") 

    # Relasi 1-to-1 ke profil siswa
    siswa = relationship("Siswa", back_populates="user", uselist=False)

# Tabel untuk Master Kuota Peminatan / Jurusan
class Peminatan(Base):
    __tablename__ = "tb_peminatan"

    id_peminatan = Column(Integer, primary_key=True, index=True)
    nama_peminatan = Column(String, unique=True)
    kapasitas_total = Column(Integer)
    
    # Relasi 1-to-Many ke tb_kelas
    kelas = relationship("Kelas", back_populates="peminatan")

# Tabel BARU: Master Kuota Kelas
class Kelas(Base):
    __tablename__ = "tb_kelas"

    id_kelas = Column(Integer, primary_key=True, index=True)
    nama_kelas = Column(String, unique=True)
    id_peminatan = Column(Integer, ForeignKey("tb_peminatan.id_peminatan"))
    kuota_kelas = Column(Integer)

    # Relasi
    peminatan = relationship("Peminatan", back_populates="kelas")
    siswa = relationship("Siswa", back_populates="kelas_diterima")

# Tabel Profil & Nilai Siswa (Diperbarui Massif)
class Siswa(Base):
    __tablename__ = "tb_siswa"

    id_siswa = Column(Integer, primary_key=True, index=True)
    id_user = Column(Integer, ForeignKey("tb_users.id_user")) 
    nisn = Column(String, unique=True, index=True)
    
    # Data Profil & Keluarga
    nama_lengkap = Column(String)
    tempat_tanggal_lahir = Column(String)
    alamat_siswa = Column(String)
    status_keluarga = Column(String)
    anak_ke = Column(Integer)
    asal_sekolah = Column(String)
    
    # Data Orang Tua
    nama_ayah = Column(String)
    pekerjaan_ayah = Column(String)
    nama_ibu = Column(String)
    pekerjaan_ibu = Column(String)
    alamat_ortu = Column(String)
    no_telp_ortu = Column(String)

    # Data Wali (Opsional)
    nama_wali = Column(String, nullable=True)
    pekerjaan_wali = Column(String, nullable=True)
    no_telp_wali = Column(String, nullable=True)
    alamat_wali = Column(String, nullable=True)
    
    # Data Akademik
    nilai_raport = Column(Float, nullable=True)
    nilai_literasi = Column(Float, nullable=True)
    nilai_numerasi = Column(Float, nullable=True)
    id_angket_pilihan = Column(Integer, ForeignKey("tb_peminatan.id_peminatan"), nullable=True)
    
    # Hasil SPK & Distribusi Kelas
    skor_spk = Column(Float, default=0.0)
    status_validasi_nilai = Column(String, default="Belum Dicek")
    id_kelas_diterima = Column(Integer, ForeignKey("tb_kelas.id_kelas"), nullable=True)

    # Relasi balik
    user = relationship("User", back_populates="siswa")
    kelas_diterima = relationship("Kelas", back_populates="siswa")