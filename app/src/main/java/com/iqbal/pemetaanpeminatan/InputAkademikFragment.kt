package com.iqbal.pemetaanpeminatan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.iqbal.pemetaanpeminatan.data.DefaultResponse
import com.iqbal.pemetaanpeminatan.data.ProfilSiswaResponse
import com.iqbal.pemetaanpeminatan.data.UpdateNilaiRequest
import com.iqbal.pemetaanpeminatan.databinding.FragmentInputAkademikBinding
import com.iqbal.pemetaanpeminatan.repository.PemetaanRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InputAkademikFragment : Fragment() {

    private var _binding: FragmentInputAkademikBinding? = null
    private val binding get() = _binding!!
    private val repository = PemetaanRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInputAkademikBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nisnAktif = SessionManager(requireContext()).getNisn() ?: ""

        if (nisnAktif.isNotEmpty()) {
            muatDataLama(nisnAktif)
        }

        binding.btnSimpanAkademik.setOnClickListener {
            if (validateInputs()) {
                val raport = binding.edtNilaiRaport.text.toString().toFloat()
                val literasi = binding.edtNilaiLiterasi.text.toString().toFloat()
                val numerasi = binding.edtNilaiNumerasi.text.toString().toFloat()
                val angketPilihan = binding.spinnerPeminatan.selectedItemPosition

                val request = UpdateNilaiRequest(
                    nisn = nisnAktif,
                    nilai_raport = raport,
                    nilai_literasi = literasi,
                    nilai_numerasi = numerasi,
                    id_angket_pilihan = angketPilihan
                )

                kirimDataKeServer(request)
            }
        }
    }

    private fun validateInputs(): Boolean {
        val strRaport = binding.edtNilaiRaport.text.toString().trim()
        val strLiterasi = binding.edtNilaiLiterasi.text.toString().trim()
        val strNumerasi = binding.edtNilaiNumerasi.text.toString().trim()
        val spinnerPosition = binding.spinnerPeminatan.selectedItemPosition

        if (strRaport.isEmpty()) {
            binding.edtNilaiRaport.error = "Nilai rata-rata raport tidak boleh kosong"
            return false
        }
        if (strLiterasi.isEmpty()) {
            binding.edtNilaiLiterasi.error = "Nilai psikotes literasi tidak boleh kosong"
            return false
        }
        if (strNumerasi.isEmpty()) {
            binding.edtNilaiNumerasi.error = "Nilai psikotes numerasi tidak boleh kosong"
            return false
        }

        val valRaport = strRaport.toFloatOrNull()
        val valLiterasi = strLiterasi.toFloatOrNull()
        val valNumerasi = strNumerasi.toFloatOrNull()

        if (valRaport == null || valRaport < 0 || valRaport > 100) {
            binding.edtNilaiRaport.error = "Nilai harus dalam rentang 0 - 100"
            return false
        }
        if (valLiterasi == null || valLiterasi < 0 || valLiterasi > 100) {
            binding.edtNilaiLiterasi.error = "Nilai harus dalam rentang 0 - 100"
            return false
        }
        if (valNumerasi == null || valNumerasi < 0 || valNumerasi > 100) {
            binding.edtNilaiNumerasi.error = "Nilai harus dalam rentang 0 - 100"
            return false
        }
        if (spinnerPosition <= 0) {
            Toast.makeText(requireContext(), "Silakan pilih peminatan jurusan", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun kirimDataKeServer(request: UpdateNilaiRequest) {
        setLoadingState(true)

        repository.updateNilaiSiswa(request).enqueue(object : Callback<DefaultResponse> {
            override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                setLoadingState(false)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), response.body()?.pesan ?: "Data berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                setLoadingState(false)
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun muatDataLama(nisn: String) {
        setLoadingState(true)
        repository.getProfilSiswa(nisn).enqueue(object : Callback<ProfilSiswaResponse> {
            override fun onResponse(call: Call<ProfilSiswaResponse>, response: Response<ProfilSiswaResponse>) {
                setLoadingState(false)
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    data.nilaiRaport?.let { binding.edtNilaiRaport.setText(it.toString()) }
                    data.nilaiLiterasi?.let { binding.edtNilaiLiterasi.setText(it.toString()) }
                    data.nilaiNumerasi?.let { binding.edtNilaiNumerasi.setText(it.toString()) }
                }
            }

            override fun onFailure(call: Call<ProfilSiswaResponse>, t: Throwable) {
                setLoadingState(false)
            }
        })
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBarAkademik.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSimpanAkademik.isEnabled = !isLoading
        binding.edtNilaiRaport.isEnabled = !isLoading
        binding.edtNilaiLiterasi.isEnabled = !isLoading
        binding.edtNilaiNumerasi.isEnabled = !isLoading
        binding.spinnerPeminatan.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
