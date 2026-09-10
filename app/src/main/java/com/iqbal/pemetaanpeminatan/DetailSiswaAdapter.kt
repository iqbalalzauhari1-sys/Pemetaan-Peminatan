package com.iqbal.pemetaanpeminatan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iqbal.pemetaanpeminatan.data.SiswaDetail
import com.iqbal.pemetaanpeminatan.databinding.ItemDetailSiswaBinding

// Tambahkan parameter onItemClick
class DetailSiswaAdapter(
    private val listSiswa: List<SiswaDetail>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<DetailSiswaAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemDetailSiswaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(siswa: SiswaDetail) {
            binding.tvItemDetailNama.text = siswa.namaLengkap
            binding.tvItemDetailNisn.text = "NISN: ${siswa.nisn}"
            val teksAsal = "Asal: ${siswa.asalSekolah}"
            binding.tvItemDetailAsal.text = android.text.Html.fromHtml(teksAsal, android.text.Html.FROM_HTML_MODE_COMPACT)
            binding.tvItemDetailSkor.text = "${siswa.skorSpk}"

            // Berikan aksi klik pada kartu (MaterialCardView)
            binding.root.setOnClickListener {
                siswa.nisn?.let { nisn -> onItemClick(nisn) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDetailSiswaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listSiswa[position])
    }

    override fun getItemCount(): Int = listSiswa.size
}