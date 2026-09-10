package com.iqbal.pemetaanpeminatan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.iqbal.pemetaanpeminatan.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    // Gunakan ViewModel yang biasa Anda pakai untuk Register
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()

        binding.btnRegister.setOnClickListener {
            val nisn = binding.edtRegNisn.text.toString()
            val password = binding.edtRegPassword.text.toString()
            val nama = binding.edtRegNama.text.toString()

            if (nisn.isNotEmpty() && password.isNotEmpty() && nama.isNotEmpty()) {
                viewModel.daftarSiswa(nisn, password, nama)
            } else {
                Toast.makeText(requireContext(), "Harap lengkapi NISN, Password, dan Nama", Toast.LENGTH_SHORT).show()
            }
        }

        // Kembali ke halaman Login jika sudah punya akun
        binding.tvKembaliLogin.setOnClickListener {
            findNavController().navigateUp() // Ini aksi tombol back bawaan Navigation Component
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBarRegister.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.responseServer.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { respon ->
                Toast.makeText(requireContext(), "Sukses: ${respon.pesan}", Toast.LENGTH_LONG).show()
                findNavController().navigateUp() // Otomatis kembali ke Login setelah sukses daftar
            }
        }

        viewModel.pesanError.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { error ->
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}