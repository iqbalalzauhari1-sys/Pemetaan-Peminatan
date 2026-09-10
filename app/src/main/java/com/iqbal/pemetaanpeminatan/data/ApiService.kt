package com.iqbal.pemetaanpeminatan.data

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @POST("register/")
    fun registerSiswa(
        @Body request: Map<String, String>
    ): Call<RegisterResponse>

    // Tambahkan baris ini untuk Login
    @POST("login/")
    fun loginUser(
        @Body request: Map<String, String>
    ): Call<LoginResponse>

    // Tambahkan di bawah fungsi updateNilai
    @POST("eksekusi-penjurusan-global/")
    fun eksekusiPenjurusanGlobal(): Call<EksekusiResponse>

    // Tambahkan di bawah fungsi eksekusiPenjurusan
    @GET("status-siswa/{nisn}")
    fun getStatusSiswa(
        @Path("nisn") nisn: String
    ): Call<StatusResponse>

    @GET("daftar-kelas/")
    fun getDaftarKelas(): Call<List<KelasResponse>>

    @GET("kelas/{id_kelas}/siswa")
    fun getSiswaPerKelas(
        @Path("id_kelas") idKelas: Int
    ): Call<DetailKelasResponse>

    @GET("profil-siswa/{nisn}")
    fun getProfilSiswa(
        @Path("nisn") nisn: String
    ): Call<ProfilSiswaResponse>

    @PUT("update-profil/")
    fun updateProfilSiswa(
        @Body request: UpdateProfilRequest
    ): Call<DefaultResponse>

    @PUT("update-nilai/")
    fun updateNilaiSiswa(
        @Body request: UpdateNilaiRequest
    ): Call<DefaultResponse>

    @POST("setup-peminatan/")
    fun setupPeminatan(
        @Body request: SetupPeminatanRequest
    ): Call<DefaultResponse>

    @GET("siswa-ditolak/")
    fun getSiswaDitolak(): Call<DetailKelasResponse>

    @PUT("buka-kunci-nilai/{nisn}")
    fun bukaKunciNilai(
        @Path("nisn") nisn: String
    ): Call<DefaultResponse>

    @DELETE("reset-tahun-ajaran/")
    fun resetTahunAjaran(): Call<DefaultResponse>

    @GET("cari-siswa/")
    fun cariSiswaGlobal(
        @retrofit2.http.Query("q") query: String
    ): Call<List<ProfilSiswaResponse>>

}