package com.iqbal.pemetaanpeminatan.data

data class InputKelas(
    val nama_kelas: String,
    val kuota_kelas: Int
)

data class SetupPeminatanRequest(
    val nama_peminatan: String,
    val daftar_kelas: List<InputKelas>
)