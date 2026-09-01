from database import SessionLocal, engine
import models

# 1. Pastikan tabel tercipta di SQLite
models.Base.metadata.create_all(bind=engine)
db = SessionLocal()

print("Memulai inisialisasi database awal...")

# 2. Setup Master Akun Admin (Username: 12345, Pass: admin)
if not db.query(models.User).filter(models.User.username == "12345").first():
    db.add(models.User(username="12345", password="admin", role="admin"))
    db.commit()
    print("Akun Admin berhasil dibuat.")

# 3. Setup Master Jurusan & Kelas Dasar
if not db.query(models.Peminatan).first():
    print("Membuat master data jurusan dan kelas default...")
    
    # URUTAN HARUS SAMA PERSIS DENGAN SPINNER ANDROID:
    # 1: FISIKA, 2: KIMIA, 3: IPA, 4: IPS, 5: SOSIOLOGI, 6: GEOGRAFI
    fisika = models.Peminatan(nama_peminatan="FISIKA", kapasitas_total=45)
    kimia = models.Peminatan(nama_peminatan="KIMIA", kapasitas_total=45)
    ipa = models.Peminatan(nama_peminatan="IPA", kapasitas_total=45)
    ips = models.Peminatan(nama_peminatan="IPS", kapasitas_total=45)
    sosiologi = models.Peminatan(nama_peminatan="SOSIOLOGI", kapasitas_total=45)
    geografi = models.Peminatan(nama_peminatan="GEOGRAFI", kapasitas_total=45)
    
    db.add_all([fisika, kimia, ipa, ips, sosiologi, geografi])
    db.commit()
    
    # Bikin 1 Kelas Kosong untuk masing-masing jurusan sebagai pancingan
    db.add_all([
        models.Kelas(nama_kelas="11 FISIKA 1", id_peminatan=fisika.id_peminatan, kuota_kelas=45),
        models.Kelas(nama_kelas="11 KIMIA 1", id_peminatan=kimia.id_peminatan, kuota_kelas=45),
        models.Kelas(nama_kelas="11 IPA 1", id_peminatan=ipa.id_peminatan, kuota_kelas=45),
        models.Kelas(nama_kelas="11 IPS 1", id_peminatan=ips.id_peminatan, kuota_kelas=45),
        models.Kelas(nama_kelas="11 SOSIOLOGI 1", id_peminatan=sosiologi.id_peminatan, kuota_kelas=45),
        models.Kelas(nama_kelas="11 GEOGRAFI 1", id_peminatan=geografi.id_peminatan, kuota_kelas=45)
    ])
    db.commit()
    print("✅ Data Jurusan dan Kelas berhasil diinisialisasi.")

db.close()
print("-" * 40)
print("INISIALISASI DATABASE SELESAI!")
print("Akun Admin: [Username: 12345 | Password: admin]")
print("Silakan jalankan server dengan: uvicorn main:app --reload")
print("-" * 40)