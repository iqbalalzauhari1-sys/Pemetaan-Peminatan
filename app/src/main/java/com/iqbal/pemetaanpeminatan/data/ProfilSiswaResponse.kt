package com.iqbal.pemetaanpeminatan.data

import com.google.gson.annotations.SerializedName

data class ProfilSiswaResponse(
    @SerializedName("nisn") val nisn: String,
    @SerializedName("nama_lengkap") val namaLengkap: String,
    @SerializedName("tempat_tanggal_lahir") val ttl: String?,
    @SerializedName("alamat_siswa") val alamatSiswa: String?,
    @SerializedName("status_keluarga") val statusKeluarga: String?,
    @SerializedName("anak_ke") val anakKe: Int?,
    @SerializedName("asal_sekolah") val asalSekolah: String?,
    @SerializedName("nama_ayah") val namaAyah: String?,
    @SerializedName("pekerjaan_ayah") val pekerjaanAyah: String?,
    @SerializedName("nama_ibu") val namaIbu: String?,
    @SerializedName("pekerjaan_ibu") val pekerjaanIbu: String?,
    @SerializedName("alamat_ortu") val alamatOrtu: String?,
    @SerializedName("no_telp_ortu") val noTelpOrtu: String?,
    @SerializedName("nama_wali") val namaWali: String?,
    @SerializedName("pekerjaan_wali") val pekerjaanWali: String?,
    @SerializedName("alamat_wali") val alamatWali: String?,
    @SerializedName("no_telp_wali") val noTelpWali: String?,
    @SerializedName("nilai_raport") val nilaiRaport: Double?,
    @SerializedName("nilai_literasi") val nilaiLiterasi: Double?,
    @SerializedName("nilai_numerasi") val nilaiNumerasi: Double?,
    @SerializedName("skor_spk") val skorSpk: Double?,
    @SerializedName("status_validasi_nilai") val statusValidasi: String?
)