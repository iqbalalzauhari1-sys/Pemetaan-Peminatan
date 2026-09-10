package com.iqbal.pemetaanpeminatan

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.iqbal.pemetaanpeminatan.data.DefaultResponse
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInputAkademikBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val nisnAktif = SessionManager(requireContext()).getNisn() ?: ""

        binding.btnSimpanAkademik.setOnClickListener {
            val raportStr = binding.edtNilaiRaport.text.toString().trim()
            val literasiStr = binding.edtNilaiLiterasi.text.toString().trim()
            val numerasiStr = binding.edtNilaiNumerasi.text.toString().trim()

            val idPeminatan = binding.spinnerPeminatan.selectedItemPosition

            if (raportStr.isNotEmpty() && literasiStr.isNotEmpty() && numerasiStr.isNotEmpty() && idPeminatan != 0) {
                val request = UpdateNilaiRequest(
                    nisn = nisnAktif,
                    nilai_raport = raportStr.toFloatOrNull() ?: 0f,
                    nilai_literasi = literasiStr.toFloatOrNull() ?: 0f,
                    nilai_numerasi = numerasiStr.toFloatOrNull() ?: 0f,
                    id_angket_pilihan = idPeminatan
                )
                kirimNilaiKeServer(request)
            } else {
                Toast.makeText(requireContext(), "Harap isi semua nilai dan pilih jurusan!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun kirimNilaiKeServer(request: UpdateNilaiRequest) {
        binding.progressBarAkademik.visibility = View.VISIBLE
        binding.btnSimpanAkademik.isEnabled = false

        repository.updateNilaiSiswa(request).enqueue(object : Callback<DefaultResponse> {
            override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                binding.progressBarAkademik.visibility = View.GONE
                binding.btnSimpanAkademik.isEnabled = true

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Nilai akademik berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack() // Kembali ke Home Siswa
                } else {
                    Toast.makeText(requireContext(), "Gagal menyimpan nilai", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                binding.progressBarAkademik.visibility = View.GONE
                binding.btnSimpanAkademik.isEnabled = true
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}