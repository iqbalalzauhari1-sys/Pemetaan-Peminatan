package com.iqbal.pemetaanpeminatan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iqbal.pemetaanpeminatan.data.KelasResponse
import com.iqbal.pemetaanpeminatan.databinding.ItemKelasBinding

// Adapter ini menerima fungsi klik (Lambda) agar fragment tahu kelas mana yang ditekan
class KelasAdapter(
    private val listKelas: List<KelasResponse>,
    private val onItemClick: (KelasResponse) -> Unit
) : RecyclerView.Adapter<KelasAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemKelasBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(kelas: KelasResponse) {
            binding.tvNamaKelas.text = kelas.namaKelas
            binding.tvKapasitasKelas.text = "Terisi: ${kelas.terisi} / ${kelas.kuotaMaksimal} Siswa"

            // Aksi saat kartu ditekan
            binding.root.setOnClickListener {
                onItemClick(kelas)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemKelasBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listKelas[position])
    }

    override fun getItemCount(): Int = listKelas.size
}