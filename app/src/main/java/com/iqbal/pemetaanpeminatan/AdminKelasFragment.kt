package com.iqbal.pemetaanpeminatan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import com.iqbal.pemetaanpeminatan.databinding.FragmentAdminKelasBinding

class AdminKelasFragment : Fragment() {

    private var _binding: FragmentAdminKelasBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels() // Gunakan AdminViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminKelasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvDaftarKelas.layoutManager = LinearLayoutManager(requireContext())

        // Aksi ketika kartu Siswa Tertolak ditekan
        binding.cardSiswaDitolak.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("EXTRA_ID_KELAS", -1) // Kirim ID -1 sebagai kode rahasia
            findNavController().navigate(R.id.action_adminKelasFragment_to_adminDetailKelasFragment, bundle)
        }

        // Aksi ketika tombol "Enter / Search" di keyboard HP ditekan
        binding.edtSearchGlobal.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.edtSearchGlobal.text.toString().trim()
                if (query.isNotEmpty()) {
                    viewModel.cariSiswaGlobal(query)
                }
                true
            } else false
        }

        setupObservers()

        // Perintahkan ViewModel untuk memuat data kelas dari Python
        viewModel.muatDaftarKelas()
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBarKelas.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.daftarKelas.observe(viewLifecycleOwner) { listKelas ->
            if (listKelas.isNotEmpty()) {
                val adapter = KelasAdapter(listKelas) { kelasTerpilih ->
                    // KIRIM ID KELAS KE HALAMAN DETAIL
                    val bundle = Bundle()
                    bundle.putInt("EXTRA_ID_KELAS", kelasTerpilih.idKelas)
                    findNavController().navigate(R.id.action_adminKelasFragment_to_adminDetailKelasFragment, bundle)
                }
                binding.rvDaftarKelas.adapter = adapter
            } else {
                binding.rvDaftarKelas.adapter = null
            }
        }

        viewModel.pesanError.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { error ->
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.hasilCariSiswa.observe(viewLifecycleOwner) { listSiswa ->
            if (listSiswa.isEmpty()) {
                Toast.makeText(requireContext(), "Siswa tidak ditemukan", Toast.LENGTH_SHORT).show()
            } else {
                // Ubah data menjadi array teks yang cantik untuk ditampilkan di Dialog
                val arrayNama = listSiswa.map { "${it.namaLengkap}\nStatus: ${it.statusValidasi ?: "Belum Dicek"}" }.toTypedArray()

                com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Hasil Pencarian")
                    .setItems(arrayNama) { _, which ->
                        // Jika nama diklik, munculkan pop-up biodata penuh!
                        val siswaTerpilih = listSiswa[which]
                        tampilkanDialogBiodata(siswaTerpilih)
                    }
                    .setNegativeButton("Tutup", null)
                    .show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun tampilkanDialogBiodata(profil: com.iqbal.pemetaanpeminatan.data.ProfilSiswaResponse) {
        // Susun teks dengan format HTML agar rapi (tebal/tipis)
        val pesanHtml = """
            <b>NISN:</b> ${profil.nisn}<br>
            <b>TTL:</b> ${profil.ttl ?: "-"}<br>
            <b>Alamat:</b> ${profil.alamatSiswa ?: "-"}<br>
            <b>Status Keluarga:</b> ${profil.statusKeluarga ?: "-"} (Anak ke-${profil.anakKe ?: 0})<br>
            <b>Asal Sekolah:</b> ${profil.asalSekolah ?: "-"}<br>
            <br>
            <b>DATA ORANG TUA</b><br>
            <b>Ayah:</b> ${profil.namaAyah ?: "-"} (${profil.pekerjaanAyah ?: "-"})<br>
            <b>Ibu:</b> ${profil.namaIbu ?: "-"} (${profil.pekerjaanIbu ?: "-"})<br>
            <b>Telp/HP:</b> ${profil.noTelpOrtu ?: "-"}<br>
            <br>
            <b>NILAI AKADEMIK</b><br>
            <b>Raport:</b> ${profil.nilaiRaport ?: 0.0}<br>
            <b>Literasi:</b> ${profil.nilaiLiterasi ?: 0.0}<br>
            <b>Numerasi:</b> ${profil.nilaiNumerasi ?: 0.0}
        """.trimIndent()

        // Panggil MaterialAlertDialogBuilder bawaan M3
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(profil.namaLengkap)
            .setMessage(android.text.Html.fromHtml(pesanHtml, android.text.Html.FROM_HTML_MODE_COMPACT))
            .setNeutralButton("Buka Kunci Nilai") { _, _ ->
                com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Konfirmasi")
                    .setMessage("Data nilai siswa ini akan di-reset dan siswa akan bisa melakukan input ulang. Yakin?")
                    .setPositiveButton("Ya, Buka Kunci") { _, _ ->
                        viewModel.bukaKunciSiswa(profil.nisn)
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
            .setPositiveButton("Tutup") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}