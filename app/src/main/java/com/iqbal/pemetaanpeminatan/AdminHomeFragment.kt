package com.iqbal.pemetaanpeminatan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.iqbal.pemetaanpeminatan.databinding.FragmentAdminHomeBinding

class AdminHomeFragment : Fragment() {

    private var _binding: FragmentAdminHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup Layout Manager untuk RecyclerView
        binding.rvHasilSpk.layoutManager = LinearLayoutManager(requireContext())

        setupObservers()

        binding.btnEksekusiGlobal.setOnClickListener {
            viewModel.jalankanAlgoritmaGlobal()
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBarAdmin.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.hasilServer.observe(viewLifecycleOwner) { respon ->
            binding.tvInfoEksekusi.text = "Hasil Distribusi: ${respon.jurusan}"

            if (!respon.siswaDiterima.isNullOrEmpty()) {
                val adapter = HasilSpkAdapter(respon.siswaDiterima!!)
                binding.rvHasilSpk.adapter = adapter
            } else {
                Toast.makeText(requireContext(), "Tidak ada data yang memenuhi kriteria", Toast.LENGTH_SHORT).show()
                binding.rvHasilSpk.adapter = null
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