import random
from database import SessionLocal
import models

# Buka koneksi database
db = SessionLocal()

# Konfigurasi: Kita buat 300 Siswa untuk memicu "Kuota Penuh" di beberapa kelas
JUMLAH_SISWA = 300

# Kumpulan suku kata untuk membuat nama yang realistis
nama_depan = ["Budi", "Siti", "Andi", "Ayu", "Rizky", "Putri", "Dwi", "Eka", "Fajar", "Ilham", "Lestari", "Muhammad", "Nur", "Tegar", "Sarah"]
nama_belakang = ["Saputra", "Wulandari", "Hidayat", "Sari", "Ramadhan", "Setiawan", "Ningsih", "Prasetyo", "Agustina", "Santoso", "Rahayu", "Wijaya", "Kusuma"]

print(f"Menciptakan {JUMLAH_SISWA} data siswa dummy secara ajaib...")

berhasil = 0
for i in range(JUMLAH_SISWA):
    # Buat NISN unik (10 digit)
    nisn = f"00{random.randint(10000000, 99999999)}"
    
    # Pastikan tidak ada NISN ganda
    if db.query(models.User).filter(models.User.username == nisn).first():
        continue

    # 1. Buat Akun Login (Password sama dengan NISN)
    user = models.User(username=nisn, password=nisn, role="siswa")
    db.add(user)
    db.commit()
    db.refresh(user)

    # 2. Buat Profil & Nilai Akademik Acak
    nama = f"{random.choice(nama_depan)} {random.choice(nama_belakang)}"
    raport = round(random.uniform(70.0, 98.0), 1)     # Nilai 70 - 98
    literasi = round(random.uniform(60.0, 100.0), 1)  # Nilai 60 - 100
    numerasi = round(random.uniform(50.0, 100.0), 1)  # Nilai 50 - 100
    id_jurusan = random.randint(1, 6) 
    alamat_palsu = f"Jl. Dummy No. {random.randint(1, 100)}"                # Acak memilih 1 dari 6 jurusan

    siswa = models.Siswa(
        id_user=user.id_user,
        nisn=nisn,
        nama_lengkap=nama,
        tempat_tanggal_lahir="Purwakarta, 10 Agustus 2010",
        alamat_siswa=alamat_palsu,
        status_keluarga="Anak Kandung",
        anak_ke=random.randint(1, 3),
        asal_sekolah=f"SMP Negeri {random.randint(1, 10)} Purwakarta",
        nama_ayah="Bapak " + random.choice(nama_depan),
        pekerjaan_ayah="Wiraswasta",
        nama_ibu="Ibu " + random.choice(nama_depan),
        pekerjaan_ibu="Ibu Rumah Tangga",
        alamat_ortu=alamat_palsu,
        no_telp_ortu=f"0812{random.randint(100000, 999999)}",
        nilai_raport=raport,
        nilai_literasi=literasi,
        nilai_numerasi=numerasi,
        id_angket_pilihan=id_jurusan,
        status_validasi_nilai="Menunggu Proses Pemetaan"
    )
    db.add(siswa)
    berhasil += 1

# Simpan semua data profil ke database sekaligus
db.commit()
db.close()

print("-" * 50)
print(f"🎉 SUKSES! {berhasil} siswa dummy berhasil disuntikkan ke database.")
print("Silakan buka aplikasi Android Anda (Login sebagai Admin) dan tekan tombol Distribusi!")
print("-" * 50)