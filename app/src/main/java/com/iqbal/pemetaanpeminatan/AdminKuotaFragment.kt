package com.iqbal.pemetaanpeminatan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.iqbal.pemetaanpeminatan.data.DefaultResponse
import com.iqbal.pemetaanpeminatan.data.InputKelas
import com.iqbal.pemetaanpeminatan.data.SetupPeminatanRequest
import com.iqbal.pemetaanpeminatan.databinding.FragmentAdminKuotaBinding
import com.iqbal.pemetaanpeminatan.repository.PemetaanRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminKuotaFragment : Fragment() {

    private var _binding: FragmentAdminKuotaBinding? = null
    private val binding get() = _binding!!
    private val repository = PemetaanRepository()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminKuotaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSimpanKuota.setOnClickListener {
            val jurusan = binding.dropdownPeminatan.text.toString().trim()

            if (jurusan.isEmpty() || jurusan == "Pilih Peminatan...") {
                Toast.makeText(requireContext(), "Harap pilih jurusan terlebih dahulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Ambil data Kelas 1 (Wajib)
            val namaKelas1 = binding.edtNamaKelas1.text.toString().trim()
            val kuotaKelas1 = binding.edtKuotaKelas1.text.toString().toIntOrNull()

            if (namaKelas1.isEmpty() || kuotaKelas1 == null || kuotaKelas1 <= 0) {
                Toast.makeText(requireContext(), "Data Kelas Utama (1) wajib diisi dengan benar!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val daftarKelas = mutableListOf<InputKelas>()
            daftarKelas.add(InputKelas(namaKelas1, kuotaKelas1))

            // Ambil data Kelas 2 (Opsional)
            val namaKelas2 = binding.edtNamaKelas2.text.toString().trim()
            val kuotaKelas2 = binding.edtKuotaKelas2.text.toString().toIntOrNull()
            if (namaKelas2.isNotEmpty() && kuotaKelas2 != null && kuotaKelas2 > 0) {
                daftarKelas.add(InputKelas(namaKelas2, kuotaKelas2))
            }

            val request = SetupPeminatanRequest(nama_peminatan = jurusan, daftar_kelas = daftarKelas)
            kirimDataKeServer(request)
        }
    }

    private fun kirimDataKeServer(request: SetupPeminatanRequest) {
        binding.progressBarKuota.visibility = View.VISIBLE
        binding.btnSimpanKuota.isEnabled = false

        repository.setupPeminatan(request).enqueue(object : Callback<DefaultResponse> {
            override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                binding.progressBarKuota.visibility = View.GONE
                binding.btnSimpanKuota.isEnabled = true

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Kuota berhasil diperbarui!", Toast.LENGTH_LONG).show()
                    // Kosongkan form setelah sukses
                    binding.edtNamaKelas1.text?.clear()
                    binding.edtKuotaKelas1.text?.clear()
                    binding.edtNamaKelas2.text?.clear()
                    binding.edtKuotaKelas2.text?.clear()
                    binding.edtNamaKelas3.text?.clear()
                    binding.edtKuotaKelas3.text?.clear()
                    binding.edtNamaKelas4.text?.clear()
                    binding.edtKuotaKelas4.text?.clear()
                } else {
                    Toast.makeText(requireContext(), "Gagal mengatur kuota", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                binding.progressBarKuota.visibility = View.GONE
                binding.btnSimpanKuota.isEnabled = true
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}