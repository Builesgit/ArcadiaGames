package com.example.prueba1integrador.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.prueba1integrador.model.Juego
import com.example.prueba1integrador.R

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
        // Usamos el layout de carta que ya tienes diseñado
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_carta_juego, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val juego = data[position]

        holder.tvName.text = juego.nombre
        // Prioriza plataforma, si está vacía usa la categoría
        holder.tvPlatform.text = if (juego.plataforma.isNotBlank()) juego.plataforma else juego.categoria
        holder.tvPrice.text = juego.precio

        // Carga de imagen inteligente
        if (juego.imagenUrl.isNotEmpty()) {
            Glide.with(holder.img.context)
                .load(juego.imagenUrl)
                .placeholder(R.drawable.fondo_sin_logo)
                .into(holder.img)
        } else if (juego.imagenResId != 0) {
            holder.img.setImageResource(juego.imagenResId)
        }

        // Eventos
        holder.itemView.setOnClickListener { onClickItem(juego) }

        // Botón de eliminar (Asegúrate de que el ID en item_carta_juego sea btnDelete)
        holder.btnDelete.setOnClickListener {
            onDeleteAt(holder.bindingAdapterPosition)
        }
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.imgGame)
        val tvName: TextView = v.findViewById(R.id.tvGameName)
        val tvPlatform: TextView = v.findViewById(R.id.tvPlatform)
        val tvPrice: TextView = v.findViewById(R.id.tvPrice)
        val btnDelete: ImageView = v.findViewById(R.id.btnDelete)
    }
}