package com.iqbal.pemetaanpeminatan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.iqbal.pemetaanpeminatan.data.SiswaDetail // Pastikan ini ter-import
import com.iqbal.pemetaanpeminatan.databinding.FragmentAdminDetailKelasBinding

class AdminDetailKelasFragment : Fragment() {

    private var _binding: FragmentAdminDetailKelasBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels()

    // Variabel untuk fitur sorting
    private var isSortedByName = false
    private var daftarSiswaAsli = listOf<SiswaDetail>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminDetailKelasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvSiswaDetail.layoutManager = LinearLayoutManager(requireContext())

        // Fitur Tombol Sort
        binding.btnSort.setOnClickListener {
            if (daftarSiswaAsli.isNotEmpty()) {
                isSortedByName = !isSortedByName // Tukar status
                val sortedList = if (isSortedByName) {
                    Toast.makeText(requireContext(), "Diurutkan: Abjad (A-Z)", Toast.LENGTH_SHORT).show()
                    daftarSiswaAsli.sortedBy { it.namaLengkap }
                } else {
                    Toast.makeText(requireContext(), "Diurutkan: Skor SPK", Toast.LENGTH_SHORT).show()
                    daftarSiswaAsli.sortedByDescending { it.skorSpk }
                }
                binding.rvSiswaDetail.adapter = DetailSiswaAdapter(sortedList) { nisnTerpilih ->
                    viewModel.muatProfilSiswa(nisnTerpilih)
                }
            }
        }

        // --- FITUR PENCARIAN REAL-TIME ---
        binding.edtSearchSiswa.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Ambil teks yang diketik dan ubah ke huruf kecil semua
                val query = s.toString().trim().lowercase()

                // Saring daftarSiswaAsli yang mengandung kata yang dicari
                val filteredList = if (query.isEmpty()) {
                    daftarSiswaAsli
                } else {
                    daftarSiswaAsli.filter {
                        it.namaLengkap?.lowercase()?.contains(query) == true
                    }
                }

                // Terapkan juga logika sorting yang sedang aktif ke hasil pencarian
                val finalSortedList = if (isSortedByName) {
                    filteredList.sortedBy { it.namaLengkap }
                } else {
                    filteredList.sortedByDescending { it.skorSpk }
                }

                // Pasang data yang sudah disaring ke layar
                binding.rvSiswaDetail.adapter = DetailSiswaAdapter(finalSortedList) { nisnTerpilih ->
                    viewModel.muatProfilSiswa(nisnTerpilih)
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        val idKelas = arguments?.getInt("EXTRA_ID_KELAS", 0) ?: 0
        setupObservers()

        if (idKelas != 0) {
            viewModel.muatDetailKelas(idKelas)
        } else {
            Toast.makeText(requireContext(), "ID Kelas tidak valid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBarDetail.visibility = if (loading) View.VISIBLE else View.GONE
        }

        // Menangkap data profil lengkap dan memunculkan Dialog
        viewModel.profilSiswa.observe(viewLifecycleOwner) { profil ->
            tampilkanDialogBiodata(profil)
        }

        viewModel.detailKelas.observe(viewLifecycleOwner) { response ->
            binding.tvDetailNamaKelas.text = response.namaKelas
            binding.tvDetailJumlahSiswa.text = "Total: ${response.jumlahSiswa} Siswa"

            // Simpan data asli dari server, lalu pasang ke Adapter
            daftarSiswaAsli = response.dataSiswa
            isSortedByName = false // Reset ke urutan skor bawaan server
            binding.rvSiswaDetail.adapter = DetailSiswaAdapter(daftarSiswaAsli) { nisnTerpilih ->
                viewModel.muatProfilSiswa(nisnTerpilih) // Panggil data ke server saat diklik
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