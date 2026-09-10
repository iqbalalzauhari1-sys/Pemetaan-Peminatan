package com.iqbal.pemetaanpeminatan.data

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @field:SerializedName("status")
    val status: String? = null,

    @field:SerializedName("pesan")
    val pesan: String? = null,

    @field:SerializedName("nisn")
    val nisn: String? = null
)