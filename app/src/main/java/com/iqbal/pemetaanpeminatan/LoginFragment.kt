package com.iqbal.pemetaanpeminatan // Sesuaikan jika package Anda berbeda

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.iqbal.pemetaanpeminatan.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    // Setup ViewBinding khusus untuk Fragment
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    // Inisialisasi ViewModel menggunakan Android KTX
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mengecek Sesi Login (SharedPreferences)
        val session = SessionManager(requireContext())
        if (session.isLogin()) {
            arahkahKeDashboard(session.getRole(), session.getNisn())
            return
        }

        setupObservers()

        // Aksi Tombol Login
        binding.btnLogin.setOnClickListener {
            val nisn = binding.edtNisn.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()

            if (nisn.isNotEmpty() && password.isNotEmpty()) {
                viewModel.prosesLogin(nisn, password)
            } else {
                Toast.makeText(requireContext(), "NISN dan Password harus diisi", Toast.LENGTH_SHORT).show()
            }
        }

        // Aksi Pindah ke Halaman Daftar menggunakan Navigation Component
        binding.tvDaftarSekarang.setOnClickListener {
            // Pastikan ID action ini sama dengan yang ada di main_navigation.xml Anda
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBarLogin.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.responseServer.observe(viewLifecycleOwner) { respon ->
            Toast.makeText(requireContext(), "Halo ${respon.role}! ${respon.pesan}", Toast.LENGTH_LONG).show()

            // Simpan sesi secara permanen
            SessionManager(requireContext()).simpanSesi(respon.nisn ?: "", respon.role ?: "")

            // Pindah halaman
            arahkahKeDashboard(respon.role, respon.nisn)
        }

        viewModel.pesanError.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { error ->
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun arahkahKeDashboard(role: String?, nisn: String?) {
        if (role == "admin") {
            findNavController().navigate(R.id.action_loginFragment_to_adminFragment)
        } else {
            // Mengirim NISN ke SiswaFragment menggunakan Bundle
            val bundle = Bundle()
            bundle.putString("EXTRA_NISN", nisn)
            findNavController().navigate(R.id.action_loginFragment_to_siswaFragment, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Wajib dibersihkan untuk menghindari memory leak di Fragment
        _binding = null
    }
}