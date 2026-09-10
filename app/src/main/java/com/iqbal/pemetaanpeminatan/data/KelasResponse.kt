package com.iqbal.pemetaanpeminatan.data // Sesuaikan package

import com.google.gson.annotations.SerializedName

data class KelasResponse(
    @SerializedName("id_kelas") val idKelas: Int,
    @SerializedName("nama_kelas") val namaKelas: String,
    @SerializedName("kuota_maksimal") val kuotaMaksimal: Int,
    @SerializedName("terisi") val terisi: Int
)