package com.iqbal.pemetaanpeminatan.data

import com.google.gson.annotations.SerializedName

data class EksekusiResponse(
    @SerializedName("status") val status: String,
    @SerializedName("jurusan") val jurusan: String,
    @SerializedName("kuota_tersedia") val kuotaTersedia: Int,
    @SerializedName("siswa_diterima") val siswaDiterima: List<SiswaDiterima>?
)

data class SiswaDiterima(
    @SerializedName("nama") val nama: String,
    @SerializedName("kelas") val kelas: String,
    @SerializedName("skor_akhir") val skorAkhir: Double
)
