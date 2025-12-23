package com.dev_rafid.kalibeningapp.Pelanggan

import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dev_rafid.kalibeningapp.Model.Ikan
import com.dev_rafid.kalibeningapp.R

class IkanAdapter (
    private val listIkan: List<Ikan>,
    private val onItemClick: (Ikan) -> Unit
): RecyclerView.Adapter<IkanAdapter.IkanViewHolder>(){
    inner class IkanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivIkonIkan: ImageView = itemView.findViewById(R.id.iv_ikonJenisIkan)
        val tvNamaJenisIkan: TextView = itemView.findViewById(R.id.tv_JenisNamaIkan)
        fun bind(ikan: Ikan) {
            tvNamaJenisIkan.text = ikan.namaIkan
            if (ikan.gambarUrlIkan.isNotEmpty()) {
                try {
                    val imageByte = Base64.decode(ikan.gambarUrlIkan, Base64.DEFAULT)
                    Glide.with(itemView.context)
                        .asBitmap()
                        .load(imageByte)
                        .placeholder(R.drawable.ikon_nila)
                        .into(ivIkonIkan)
                } catch (e: Exception){
                    ivIkonIkan.setImageResource(R.drawable.ikon_nila)
                }
            } else {
                ivIkonIkan.setImageResource(R.drawable.ikon_nila)
            }
            itemView.setOnClickListener { onItemClick(ikan) }
        }
         }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IkanViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_produk_ikan, parent, false)
        return IkanViewHolder(view)
        }
    override fun onBindViewHolder(holder: IkanViewHolder, position: Int) {
        holder.bind(listIkan[position])
     }
    override fun getItemCount(): Int = listIkan.size
}