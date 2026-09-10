package com.iqbal.pemetaanpeminatan.data

data class UpdateProfilRequest(
    val nisn: String,
    val tempat_tanggal_lahir: String,
    val alamat_siswa: String,
    val status_keluarga: String,
    val anak_ke: Int,
    val asal_sekolah: String,
    val nama_ayah: String,
    val pekerjaan_ayah: String,
    val nama_ibu: String,
    val pekerjaan_ibu: String,
    val alamat_ortu: String,
    val no_telp_ortu: String,
    // Tambahan Data Wali
    val nama_wali: String? = null,
    val pekerjaan_wali: String? = null,
    val no_telp_wali: String? = null,
    val alamat_wali: String? = null
)