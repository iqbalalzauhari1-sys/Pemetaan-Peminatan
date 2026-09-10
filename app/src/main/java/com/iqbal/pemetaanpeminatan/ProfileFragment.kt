package com.iqbal.pemetaanpeminatan

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.iqbal.pemetaanpeminatan.data.ProfilSiswaResponse
import com.iqbal.pemetaanpeminatan.databinding.FragmentProfileBinding
import com.iqbal.pemetaanpeminatan.repository.PemetaanRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val repository = PemetaanRepository()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val session = SessionManager(requireContext())
        val nisnAktif = session.getNisn() ?: ""

        if (nisnAktif.isNotEmpty()) {
            muatProfilLengkap(nisnAktif)
        }

        binding.btnLogoutSiswa.setOnClickListener {
            session.logout() // Bersihkan memori pakai SessionManager
            Toast.makeText(requireContext(), "Berhasil Logout", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
    }

    private fun muatProfilLengkap(nisn: String) {
        binding.progressBarProfil.visibility = View.VISIBLE

        repository.getProfilSiswa(nisn).enqueue(object : Callback<ProfilSiswaResponse> {
            override fun onResponse(call: Call<ProfilSiswaResponse>, response: Response<ProfilSiswaResponse>) {
                binding.progressBarProfil.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!

                    // Set Data Pribadi
                    binding.tvProfNama.text = "Nama: ${data.namaLengkap}"
                    binding.tvProfNisn.text = "NISN: ${data.nisn}"
                    binding.tvProfTtl.text = "TTL: ${data.ttl ?: "-"}"
                    binding.tvProfAsal.text = "Asal Sekolah: ${data.asalSekolah ?: "-"}"

                    // Set Data Orang Tua
                    binding.tvProfAyah.text = "Ayah: ${data.namaAyah ?: "-"} (${data.pekerjaanAyah ?: "-"})"
                    binding.tvProfIbu.text = "Ibu: ${data.namaIbu ?: "-"} (${data.pekerjaanIbu ?: "-"})"
                    binding.tvProfAlamat.text = "Alamat: ${data.alamatOrtu ?: "-"}"
                    binding.tvProfTelp.text = "Telp/HP: ${data.noTelpOrtu ?: "-"}"

                    // --- SET DATA WALI ---
                    binding.tvProfWaliNama.text = "Nama: ${data.namaWali ?: "-"}"
                    binding.tvProfWaliKerja.text = "Pekerjaan: ${data.pekerjaanWali ?: "-"}"
                    binding.tvProfWaliAlamat.text = "Alamat: ${data.alamatWali ?: "-"}"
                    binding.tvProfWaliTelp.text = "Telp/HP: ${data.noTelpWali ?: "-"}"

                    // Set Data Akademik
                    binding.tvProfRaport.text = "Rata-rata Raport: ${data.nilaiRaport ?: "0.0"}"
                    binding.tvProfSkor.text = "Skor: ${data.skorSpk ?: "0.0"}"
                    binding.tvProfStatus.text = "Status: ${data.statusValidasi ?: "Belum Mengisi Data"}"

                } else {
                    Toast.makeText(requireContext(), "Gagal memuat profil", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ProfilSiswaResponse>, t: Throwable) {
                binding.progressBarProfil.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}