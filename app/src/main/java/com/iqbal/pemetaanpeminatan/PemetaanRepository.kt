package com.iqbal.pemetaanpeminatan.repository

import com.iqbal.pemetaanpeminatan.data.ApiConfig
import com.iqbal.pemetaanpeminatan.data.SetupPeminatanRequest
import com.iqbal.pemetaanpeminatan.data.UpdateNilaiRequest
import com.iqbal.pemetaanpeminatan.data.UpdateProfilRequest

class PemetaanRepository {
    private val apiService = ApiConfig.getApiService()

    // Rute Login & Register
    fun loginUser(requestData: Map<String, String>) = apiService.loginUser(requestData)
    fun registerSiswa(requestData: Map<String, String>) = apiService.registerSiswa(requestData)

    // Rute Dasbor Siswa (Sesuaikan tipe parameternya dengan yang ada di ApiService Anda)
    fun getStatusSiswa(nisn: String) = apiService.getStatusSiswa(nisn)

    // Rute Dasbor Admin
    fun eksekusiPenjurusanGlobal() = apiService.eksekusiPenjurusanGlobal()

    fun getDaftarKelas() = apiService.getDaftarKelas()

    fun getSiswaPerKelas(idKelas: Int) = apiService.getSiswaPerKelas(idKelas)

    fun getProfilSiswa(nisn: String) = apiService.getProfilSiswa(nisn)

    fun updateProfilSiswa(request: UpdateProfilRequest) = apiService.updateProfilSiswa(request)

    fun updateNilaiSiswa(request: UpdateNilaiRequest) = apiService.updateNilaiSiswa(request)

    fun setupPeminatan(request: SetupPeminatanRequest) = apiService.setupPeminatan(request)

    fun getSiswaDitolak() = apiService.getSiswaDitolak()

    fun bukaKunciNilai(nisn: String) = apiService.bukaKunciNilai(nisn)

    fun resetTahunAjaran() = apiService.resetTahunAjaran()

    fun cariSiswaGlobal(query: String) = apiService.cariSiswaGlobal(query)

}