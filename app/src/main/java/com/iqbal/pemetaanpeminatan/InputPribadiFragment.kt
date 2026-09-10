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
import com.iqbal.pemetaanpeminatan.data.UpdateProfilRequest
import com.iqbal.pemetaanpeminatan.databinding.FragmentInputPribadiBinding
import com.iqbal.pemetaanpeminatan.repository.PemetaanRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InputPribadiFragment : Fragment() {

    private var _binding: FragmentInputPribadiBinding? = null
    private val binding get() = _binding!!
    private val repository = PemetaanRepository()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInputPribadiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val nisnAktif = SessionManager(requireContext()).getNisn() ?: ""

        if (nisnAktif.isNotEmpty()) {
            muatDataLama(nisnAktif)
        }

        binding.edtTanggalLahir.setOnClickListener {
            val datePicker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
                .setTitleText("Pilih Tanggal Lahir")
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                // Format angka millis menjadi format tanggal rapi (cth: 10 Agustus 2010)
                val sdf = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale("id", "ID"))
                val dateStr = sdf.format(java.util.Date(selection))
                binding.edtTanggalLahir.setText(dateStr)
            }
            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }

        // --- LOGIKA CERDAS CHECKBOX ALAMAT ---
        binding.cbAlamatOrtuSama.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Copy teks dari alamat siswa dan kunci kolomnya agar tidak bisa diedit manual
                binding.edtAlamatOrtu.setText(binding.edtAlamatSiswa.text.toString())
                binding.edtAlamatOrtu.isEnabled = false
            } else {
                // Kosongkan dan buka kembali kuncinya
                binding.edtAlamatOrtu.text?.clear()
                binding.edtAlamatOrtu.isEnabled = true
            }
        }

        // Opsional: Jika alamat siswa diketik ulang saat checkbox aktif, alamat ortu otomatis ikut berubah
        binding.edtAlamatSiswa.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.cbAlamatOrtuSama.isChecked) {
                    binding.edtAlamatOrtu.setText(s.toString())
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        // -------------------------------------

        binding.btnSimpanProfil.setOnClickListener {
            val anakKe = binding.edtAnakKe.text.toString().toIntOrNull() ?: 0

            // Logika Gabung Tempat Tanggal Lahir (Kodingan sebelumnya)
            val tempatLahir = binding.edtTempatLahir.text.toString().trim()
            val tanggalLahir = binding.edtTanggalLahir.text.toString().trim()
            val gabunganTtl = if (tempatLahir.isNotEmpty() && tanggalLahir.isNotEmpty()) {
                "$tempatLahir, $tanggalLahir"
            } else ""

            // Ambil Data Wali
            val namaWali = binding.edtNamaWali.text.toString().trim()
            val kerjaWali = binding.edtKerjaWali.text.toString().trim()
            val telpWali = binding.edtTelpWali.text.toString().trim()
            val alamatWali = binding.edtAlamatWali.text.toString().trim()

            val request = UpdateProfilRequest(
                nisn = nisnAktif,
                tempat_tanggal_lahir = gabunganTtl,
                alamat_siswa = binding.edtAlamatSiswa.text.toString(),
                status_keluarga = binding.edtStatusKeluarga.text.toString(),
                anak_ke = anakKe,
                asal_sekolah = binding.edtAsalSekolah.text.toString(),
                nama_ayah = binding.edtNamaAyah.text.toString(),
                pekerjaan_ayah = binding.edtKerjaAyah.text.toString(),
                nama_ibu = binding.edtNamaIbu.text.toString(),
                pekerjaan_ibu = binding.edtKerjaIbu.text.toString(),
                alamat_ortu = binding.edtAlamatOrtu.text.toString(),
                no_telp_ortu = binding.edtTelpOrtu.text.toString(),

                // Masukkan data wali (Jika kosong, akan dikirim sebagai null/string kosong)
                nama_wali = namaWali.ifEmpty { null },
                pekerjaan_wali = kerjaWali.ifEmpty { null },
                no_telp_wali = telpWali.ifEmpty { null },
                alamat_wali = alamatWali.ifEmpty { null }
            )
            kirimDataKeServer(request)
        }

        // --- LOGIKA CERDAS CHECKBOX ALAMAT WALI ---
        binding.cbAlamatWaliSama.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.edtAlamatWali.setText(binding.edtAlamatSiswa.text.toString())
                binding.edtAlamatWali.isEnabled = false
            } else {
                binding.edtAlamatWali.text?.clear()
                binding.edtAlamatWali.isEnabled = true
            }
        }

        // (Modifikasi bagian addTextChangedListener milik edtAlamatSiswa yang sebelumnya kita buat)
        binding.edtAlamatSiswa.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.cbAlamatOrtuSama.isChecked) {
                    binding.edtAlamatOrtu.setText(s.toString())
                }
                if (binding.cbAlamatWaliSama.isChecked) { // Tambahkan ini agar Wali ikut ter-update
                    binding.edtAlamatWali.setText(s.toString())
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun kirimDataKeServer(request: UpdateProfilRequest) {
        binding.progressBarInput.visibility = View.VISIBLE
        binding.btnSimpanProfil.isEnabled = false // MATIKAN TOMBOL

        repository.updateProfilSiswa(request).enqueue(object : Callback<DefaultResponse> {
            override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                binding.progressBarInput.visibility = View.GONE
                binding.btnSimpanProfil.isEnabled = true

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Data berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack() // Kembali ke Home Siswa
                } else {
                    Toast.makeText(requireContext(), "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                binding.progressBarInput.visibility = View.GONE
                binding.btnSimpanProfil.isEnabled = true
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun muatDataLama(nisn: String) {
        binding.progressBarInput.visibility = View.VISIBLE
        // Kita gunakan endpoint getProfilSiswa yang sudah ada di repository
        repository.getProfilSiswa(nisn).enqueue(object : Callback<com.iqbal.pemetaanpeminatan.data.ProfilSiswaResponse> {
            override fun onResponse(call: Call<com.iqbal.pemetaanpeminatan.data.ProfilSiswaResponse>, response: Response<com.iqbal.pemetaanpeminatan.data.ProfilSiswaResponse>) {
                binding.progressBarInput.visibility = View.GONE
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!

                    // 1. Pecah kembali Tempat, Tanggal Lahir
                    val ttl = data.ttl ?: ""
                    if (ttl.contains(", ")) {
                        val parts = ttl.split(", ")
                        binding.edtTempatLahir.setText(parts[0])
                        binding.edtTanggalLahir.setText(parts[1])
                    } else {
                        binding.edtTempatLahir.setText(ttl)
                    }

                    // 2. Isi Data Pribadi
                    binding.edtAlamatSiswa.setText(data.alamatSiswa ?: "")
                    binding.edtStatusKeluarga.setText(data.statusKeluarga ?: "")
                    binding.edtAnakKe.setText(data.anakKe?.toString() ?: "")
                    binding.edtAsalSekolah.setText(data.asalSekolah ?: "")

                    // 3. Isi Data Orang Tua
                    binding.edtNamaAyah.setText(data.namaAyah ?: "")
                    binding.edtKerjaAyah.setText(data.pekerjaanAyah ?: "")
                    binding.edtNamaIbu.setText(data.namaIbu ?: "")
                    binding.edtKerjaIbu.setText(data.pekerjaanIbu ?: "")
                    binding.edtAlamatOrtu.setText(data.alamatOrtu ?: "")
                    binding.edtTelpOrtu.setText(data.noTelpOrtu ?: "")

                    // 4. Isi Data Wali
                    binding.edtNamaWali.setText(data.namaWali ?: "")
                    binding.edtKerjaWali.setText(data.pekerjaanWali ?: "")
                    binding.edtAlamatWali.setText(data.alamatWali ?: "")
                    binding.edtTelpWali.setText(data.noTelpWali ?: "")

                    // 5. Cerdas: Centang Checkbox Otomatis jika alamat sama
                    val alamatSiswa = data.alamatSiswa ?: ""
                    if (alamatSiswa.isNotEmpty()) {
                        if (alamatSiswa == data.alamatOrtu) {
                            binding.cbAlamatOrtuSama.isChecked = true
                        }
                        if (alamatSiswa == data.alamatWali) {
                            binding.cbAlamatWaliSama.isChecked = true
                        }
                    }
                }
            }

            override fun onFailure(call: Call<com.iqbal.pemetaanpeminatan.data.ProfilSiswaResponse>, t: Throwable) {
                binding.progressBarInput.visibility = View.GONE
                Toast.makeText(requireContext(), "Gagal memuat data sebelumnya", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}