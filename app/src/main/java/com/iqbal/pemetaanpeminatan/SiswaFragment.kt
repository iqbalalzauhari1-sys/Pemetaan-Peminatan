package com.iqbal.pemetaanpeminatan

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.iqbal.pemetaanpeminatan.data.ProfilSiswaResponse
import com.iqbal.pemetaanpeminatan.databinding.FragmentSiswaBinding
import com.iqbal.pemetaanpeminatan.repository.PemetaanRepository
import androidx.navigation.fragment.findNavController
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SiswaFragment : Fragment() {

    private var _binding: FragmentSiswaBinding? = null
    private val binding get() = _binding!!
    private val repository = PemetaanRepository()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSiswaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nisnAktif = SessionManager(requireContext()).getNisn() ?: ""

        if (nisnAktif.isNotEmpty()) {
            muatDataHomeSiswa(nisnAktif)
        } else {
            binding.tvWelcomeSiswa.text = "Gagal memuat NISN"
        }

        // Aksi Tombol (Akan kita buat navigasinya nanti)
        binding.btnInputPribadi.setOnClickListener {
            findNavController().navigate(R.id.action_siswaFragment_to_inputPribadiFragment)
        }

        binding.btnInputAkademik.setOnClickListener {
            findNavController().navigate(R.id.action_siswaFragment_to_inputAkademikFragment)
        }
    }

    private fun muatDataHomeSiswa(nisn: String) {
        binding.progressBarSiswa.visibility = View.VISIBLE

        val client = repository.getProfilSiswa(nisn)
        client.enqueue(object : Callback<ProfilSiswaResponse> {
            override fun onResponse(call: Call<ProfilSiswaResponse>, response: Response<ProfilSiswaResponse>) {
                binding.progressBarSiswa.visibility = View.GONE
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    binding.tvWelcomeSiswa.text = "Halo, ${data.namaLengkap}!"

                    // Cek status, jika kosong berarti belum isi data
                    val status = data.statusValidasi ?: "Data Belum Lengkap"
                    binding.tvStatusSpk.text = status

                    // FITUR KUNCI OTOMATIS
                    if (data.nilaiRaport != null && data.nilaiRaport > 0) {
                        // Kunci tombol jika nilai sudah terisi
                        binding.btnInputAkademik.text = "Data Akademik Terkunci"
                        binding.btnInputAkademik.setIconResource(android.R.drawable.ic_secure) // Ikon gembok
                        binding.btnInputAkademik.setOnClickListener {
                            Toast.makeText(requireContext(), "Data sudah terkunci. Hubungi Admin jika Anda ingin melakukan perubahan.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        // Buka tombol jika belum terisi
                        binding.btnInputAkademik.text = "Nilai & Angket"
                        binding.btnInputAkademik.setIconResource(android.R.drawable.ic_menu_sort_by_size)
                        binding.btnInputAkademik.setOnClickListener {
                            findNavController().navigate(R.id.action_siswaFragment_to_inputAkademikFragment)
                        }
                    }

                    // Jika statusnya diterima/ditolak, ubah warna teks
                    if (status.contains("DITERIMA", ignoreCase = true)) {
                        binding.tvStatusSpk.setTextColor(android.graphics.Color.parseColor("#4CAF50")) // Hijau
                    } else if (status.contains("DITOLAK", ignoreCase = true) || status.contains("TIDAK", ignoreCase = true)) {
                        binding.tvStatusSpk.setTextColor(android.graphics.Color.parseColor("#F44336")) // Merah
                    }

                } else {
                    Toast.makeText(requireContext(), "Gagal menarik data profil", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ProfilSiswaResponse>, t: Throwable) {
                binding.progressBarSiswa.visibility = View.GONE
                Toast.makeText(requireContext(), "Error jaringan: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}