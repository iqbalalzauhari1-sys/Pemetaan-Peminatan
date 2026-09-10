package com.iqbal.pemetaanpeminatan.data

import com.google.gson.annotations.SerializedName

data class DetailKelasResponse(
    @SerializedName("nama_kelas") val namaKelas: String,
    @SerializedName("jumlah_siswa") val jumlahSiswa: Int,
    @SerializedName("data_siswa") val dataSiswa: List<SiswaDetail>
)

data class SiswaDetail(
    @SerializedName("nisn") val nisn: String?,
    @SerializedName("nama_lengkap") val namaLengkap: String?,
    @SerializedName("skor") val skorSpk: Double?,
    @SerializedName("asal_sekolah") val asalSekolah: String?
)