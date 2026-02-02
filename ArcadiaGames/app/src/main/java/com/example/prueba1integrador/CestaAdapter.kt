package com.example.prueba1integrador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CestaAdapter(
    private val onClickItem: (Juego) -> Unit,
    private val onDeleteAt: (Int) -> Unit
) : RecyclerView.Adapter<CestaAdapter.VH>() {

    private val data = mutableListOf<Juego>()

    fun submitList(list: List<Juego>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_carta_juego, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val juego = data[position]

        holder.tvName.text = juego.nombre
        holder.tvPlatform.text = juego.plataforma.ifBlank { juego.categoria }
        holder.tvPrice.text = juego.precio

        if (juego.imagenUrl.isNotEmpty()) {
            Glide.with(holder.img.context)
                .load(juego.imagenUrl)
                .into(holder.img)
        } else if (juego.imagenResId != 0) {
            holder.img.setImageResource(juego.imagenResId)
        }

        holder.itemView.setOnClickListener { onClickItem(juego) }
        holder.btnDelete.setOnClickListener { onDeleteAt(holder.bindingAdapterPosition) }
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.imgGame)
        val tvName: TextView = v.findViewById(R.id.tvGameName)
        val tvPlatform: TextView = v.findViewById(R.id.tvPlatform)
        val tvPrice: TextView = v.findViewById(R.id.tvPrice)
        val btnDelete: ImageView = v.findViewById(R.id.btnDelete)
    }
}