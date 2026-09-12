package com.iqbal.pemetaanpeminatan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iqbal.pemetaanpeminatan.data.LoginResponse
import com.iqbal.pemetaanpeminatan.repository.PemetaanRepository // Pastikan di-import
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : ViewModel() {

    // 1. Panggil Repository sebagai sumber kebenaran data
    private val repository = PemetaanRepository()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _responseServer = MutableLiveData<LoginResponse>()
    val responseServer: LiveData<LoginResponse> = _responseServer

    private val _pesanError = MutableLiveData<Event<String>>()
    val pesanError: LiveData<Event<String>> = _pesanError

    fun prosesLogin(nisnInput: String, passwordInput: String) {
        _isLoading.value = true

        val requestData = mapOf(
            "nisn" to nisnInput,
            "password" to passwordInput
        )

        // 2. Gunakan repository, BUKAN ApiConfig
        val client = repository.loginUser(requestData)

        client.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    _responseServer.value = response.body()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Error code: ${response.code()}"
                    _pesanError.value = Event("Gagal (${response.code()}): $errorMsg")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                _isLoading.value = false
                _pesanError.value = Event("Error Jaringan: ${t.message}")
            }
        })
    }
}