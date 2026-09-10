package com.iqbal.pemetaanpeminatan

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.iqbal.pemetaanpeminatan.data.DefaultResponse
import com.iqbal.pemetaanpeminatan.databinding.FragmentAdminProfileBinding
import com.iqbal.pemetaanpeminatan.repository.PemetaanRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminProfileFragment : Fragment() {

    private var _binding: FragmentAdminProfileBinding? = null
    private val binding get() = _binding!!
    private val repository = PemetaanRepository()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val session = SessionManager(requireContext())
        binding.tvAdminNisn.text = "Username: ${session.getNisn()}"

        // 1. Aksi Tombol Unduh (Lempar ke Browser untuk otomatis download file)
        binding.btnUnduhLaporan.setOnClickListener {
            Toast.makeText(requireContext(), "Menyiapkan file Excel...", Toast.LENGTH_SHORT).show()
            // Sesuaikan IP ini dengan IP yang ada di ApiConfig.kt Anda!
            val url = "https://pemetaan-peminatan-production.up.railway.app/export-laporan/"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        // 2. Aksi Tombol Reset
        binding.btnResetSistem.setOnClickListener {
            com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Peringatan Keras!")
                .setMessage("Tindakan ini akan MENGHAPUS SELURUH DATA SISWA. Apakah Anda sudah mengunduh laporan (Excel) terlebih dahulu?")
                .setPositiveButton("Ya, Reset Sistem") { _, _ ->
                    jalankanResetSistem()
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        // 3. Aksi Logout
        binding.btnLogoutAdmin.setOnClickListener {
            session.logout()
            Toast.makeText(requireContext(), "Admin berhasil logout", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_adminProfileFragment_to_loginFragment)
        }
    }

    private fun jalankanResetSistem() {
        // Matikan tombol agar tidak di-spam
        binding.btnResetSistem.isEnabled = false
        repository.resetTahunAjaran().enqueue(object : Callback<DefaultResponse> {
            override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                binding.btnResetSistem.isEnabled = true
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Sistem Berhasil Direset!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(), "Gagal mereset sistem", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                binding.btnResetSistem.isEnabled = true
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}