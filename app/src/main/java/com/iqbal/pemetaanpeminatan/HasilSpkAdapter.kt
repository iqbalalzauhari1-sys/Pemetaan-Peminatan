package com.iqbal.pemetaanpeminatan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iqbal.pemetaanpeminatan.data.SiswaDiterima
import com.iqbal.pemetaanpeminatan.databinding.ItemHasilSpkBinding

class HasilSpkAdapter(private val listSiswa: List<SiswaDiterima>) : RecyclerView.Adapter<HasilSpkAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemHasilSpkBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(siswa: SiswaDiterima) {
            binding.tvItemNama.text = siswa.nama
            binding.tvItemKelas.text = "Ditempatkan di: ${siswa.kelas}"
            binding.tvItemSkor.text = "${siswa.skorAkhir}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHasilSpkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listSiswa[position])
    }

    override fun getItemCount(): Int = listSiswa.size
}