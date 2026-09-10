package com.iqbal.pemetaanpeminatan.data

data class UpdateNilaiRequest(
    val nisn: String,
    val nilai_raport: Float,
    val nilai_literasi: Float,
    val nilai_numerasi: Float,
    val id_angket_pilihan: Int
)