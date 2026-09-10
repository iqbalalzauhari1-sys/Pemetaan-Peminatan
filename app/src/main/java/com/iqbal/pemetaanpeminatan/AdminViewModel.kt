package com.iqbal.pemetaanpeminatan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iqbal.pemetaanpeminatan.data.DefaultResponse
import com.iqbal.pemetaanpeminatan.data.DetailKelasResponse
import com.iqbal.pemetaanpeminatan.data.EksekusiResponse
import com.iqbal.pemetaanpeminatan.data.InputKelas
import com.iqbal.pemetaanpeminatan.data.SetupPeminatanRequest
import com.iqbal.pemetaanpeminatan.data.KelasResponse
import com.iqbal.pemetaanpeminatan.repository.PemetaanRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminViewModel : ViewModel() {

    private val repository = PemetaanRepository()

    // STATE: Hasil perhitungan ditampilkan terus di layar
    private val _hasilServer = MutableLiveData<EksekusiResponse>()
    val hasilServer: LiveData<EksekusiResponse> = _hasilServer

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // EVENT: Pesan Toast error atau sukses eksekusi
    private val _pesanError = MutableLiveData<Event<String>>()
    val pesanError: LiveData<Event<String>> = _pesanError

    // EVENT khusus untuk Kuota
    private val _pesanKuota = MutableLiveData<Event<String>>()
    val pesanKuota: LiveData<Event<String>> = _pesanKuota

    private val _daftarKelas = MutableLiveData<List<KelasResponse>>()
    val daftarKelas: LiveData<List<KelasResponse>> = _daftarKelas

    private val _detailKelas = MutableLiveData<DetailKelasResponse>()
    val detailKelas: LiveData<DetailKelasResponse> = _detailKelas

    // ----- TAMBAHAN UNTUK DETAIL BIODATA -----
    private val _profilSiswa = MutableLiveData<com.iqbal.pemetaanpeminatan.data.ProfilSiswaResponse>()
    val profilSiswa: LiveData<com.iqbal.pemetaanpeminatan.data.ProfilSiswaResponse> = _profilSiswa

    fun muatProfilSiswa(nisn: String) {
        _isLoading.value = true
        val client = repository.getProfilSiswa(nisn)
        client.enqueue(object : Callback<com.iqbal.pemetaanpeminatan.data.ProfilSiswaResponse> {
            override fun onResponse(call: Call<com.iqbal.pemetaanpeminatan.data.ProfilSiswaResponse>, response: Response<com.iqbal.pemetaanpeminatan.data.ProfilSiswaResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    _profilSiswa.value = response.body()
                } else {
                    _pesanError.value = Event("Gagal memuat profil siswa.")
                }
            }
            override fun onFailure(call: Call<com.iqbal.pemetaanpeminatan.data.ProfilSiswaResponse>, t: Throwable) {
                _isLoading.value = false
                _pesanError.value = Event("Error jaringan: ${t.message}")
            }
        })
    }

    fun muatDetailKelas(idKelas: Int) {
        _isLoading.value = true

        // LOGIKA PINTAR: Jika ID = -1, panggil rute ditolak
        val client = if (idKelas == -1) {
            repository.getSiswaDitolak()
        } else {
            repository.getSiswaPerKelas(idKelas)
        }

        client.enqueue(object : Callback<DetailKelasResponse> {
            override fun onResponse(call: Call<DetailKelasResponse>, response: Response<DetailKelasResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    _detailKelas.value = response.body()
                } else {
                    _pesanError.value = Event("Gagal memuat daftar siswa.")
                }
            }
            override fun onFailure(call: Call<DetailKelasResponse>, t: Throwable) {
                _isLoading.value = false
                _pesanError.value = Event("Error jaringan: ${t.message}")
            }
        })
    }

    fun simpanPengaturanKuota(jurusan: String, kelas1: String, kuota1: Int, kelas2: String, kuota2: Int) {
        _isLoading.value = true

        val daftarKelas = listOf(
            InputKelas(kelas1, kuota1),
            InputKelas(kelas2, kuota2)
        )
        val requestData = SetupPeminatanRequest(jurusan, daftarKelas)

        val client = repository.setupPeminatan(requestData)
        client.enqueue(object : Callback<DefaultResponse> {
            override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    _pesanKuota.value = Event(response.body()?.pesan ?: "Berhasil disimpan!")
                } else {
                    _pesanKuota.value = Event("Gagal menyimpan data kuota.")
                }
            }

            override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                _isLoading.value = false
                _pesanKuota.value = Event("Error jaringan: ${t.message}")
            }
        })
    }

    fun muatDaftarKelas() {
        _isLoading.value = true

        val client = repository.getDaftarKelas()
        client.enqueue(object : Callback<List<KelasResponse>> {
            override fun onResponse(call: Call<List<KelasResponse>>, response: Response<List<KelasResponse>>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    _daftarKelas.value = response.body()
                } else {
                    _pesanError.value = Event("Gagal memuat daftar kelas.")
                }
            }

            override fun onFailure(call: Call<List<KelasResponse>>, t: Throwable) {
                _isLoading.value = false
                _pesanError.value = Event("Error jaringan: ${t.message}")
            }
        })
    }

    fun jalankanAlgoritmaGlobal() {
        _isLoading.value = true
        val client = repository.eksekusiPenjurusanGlobal()
        client.enqueue(object : Callback<EksekusiResponse> {
            override fun onResponse(call: Call<EksekusiResponse>, response: Response<EksekusiResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    _hasilServer.value = response.body()
                } else {
                    _pesanError.value = Event("Gagal memproses algoritma massal!")
                }
            }
            override fun onFailure(call: Call<EksekusiResponse>, t: Throwable) {
                _isLoading.value = false
                _pesanError.value = Event("Error jaringan: ${t.message}")
            }
        })
    }

    fun bukaKunciSiswa(nisn: String) {
        _isLoading.value = true
        repository.bukaKunciNilai(nisn).enqueue(object : Callback<DefaultResponse> {
            override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _pesanError.value = Event("Berhasil membuka kunci nilai! Siswa bisa input kembali.")
                } else {
                    _pesanError.value = Event("Gagal membuka kunci data.")
                }
            }
            override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                _isLoading.value = false
                _pesanError.value = Event("Error jaringan: ${t.message}")
            }
        })
    }

    private val _hasilCariSiswa = MutableLiveData<List<com.iqbal.pemetaanpeminatan.data.ProfilSiswaResponse>>()
    val hasilCariSiswa: LiveData<List<com.iqbal.pemetaanpeminatan.data.ProfilSiswaResponse>> = _hasilCariSiswa

    fun cariSiswaGlobal(query: String) {
        _isLoading.value = true
        repository.cariSiswaGlobal(query).enqueue(object : Callback<List<com.iqbal.pemetaanpeminatan.data.ProfilSiswaResponse>> {
            override fun onResponse(call: Call<List<com.iqbal.pemetaanpeminatan.data.ProfilSiswaResponse>>, response: Response<List<com.iqbal.pemetaanpeminatan.data.ProfilSiswaResponse>>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _hasilCariSiswa.value = response.body() ?: emptyList()
                } else {
                    _pesanError.value = Event("Gagal mencari siswa.")
                }
            }
            override fun onFailure(call: Call<List<com.iqbal.pemetaanpeminatan.data.ProfilSiswaResponse>>, t: Throwable) {
                _isLoading.value = false
                _pesanError.value = Event("Error jaringan: ${t.message}")
            }
        })
    }
}