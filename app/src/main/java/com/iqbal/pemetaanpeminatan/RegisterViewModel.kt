package com.iqbal.pemetaanpeminatan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iqbal.pemetaanpeminatan.data.RegisterResponse
import com.iqbal.pemetaanpeminatan.repository.PemetaanRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterViewModel : ViewModel() {

    private val repository = PemetaanRepository()

    // STATE (Loading)
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // EVENT (Pesan sukses/gagal yang hanya muncul 1x)
    private val _responseServer = MutableLiveData<Event<RegisterResponse>>()
    val responseServer: LiveData<Event<RegisterResponse>> = _responseServer

    private val _pesanError = MutableLiveData<Event<String>>()
    val pesanError: LiveData<Event<String>> = _pesanError

    fun daftarSiswa(nisnInput: String, passwordInput: String, namaInput: String) {
        _isLoading.value = true

        val requestData = mapOf(
            "nisn" to nisnInput,
            "password" to passwordInput,
            "nama_lengkap" to namaInput
        )

        val client = repository.registerSiswa(requestData)
        client.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    _responseServer.value = Event(response.body()!!)
                } else {
                    _pesanError.value = Event("Gagal Mendaftar. NISN mungkin sudah ada.")
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                _isLoading.value = false
                _pesanError.value = Event("Error jaringan: ${t.message}")
            }
        })
    }
}