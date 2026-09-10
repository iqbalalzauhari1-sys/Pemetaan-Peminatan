package com.iqbal.pemetaanpeminatan.data

import com.google.gson.annotations.SerializedName

data class StatusResponse(
    @field:SerializedName("nisn")
    val nisn: String? = null,

    @field:SerializedName("nama")
    val nama: String? = null,

    @field:SerializedName("status_kelulusan")
    val statusKelulusan: String? = null
)