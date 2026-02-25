package com.example.prueba1integrador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class EstadisticasAdapter(
    private val lista: List<Juego>,
    private val esRankingVentas: Boolean = false // Nuevo parámetro para saber qué mostrar
) : RecyclerView.Adapter<EstadisticasAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imagen: ImageView = view.findViewById(R.id.iv_juego_stat)
        val nombre: TextView = view.findViewById(R.id.tv_nombre_stat)
        val metrica: TextView = view.findViewById(R.id.tv_metrica_stat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_juego_estadistica, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val juego = lista[position]
        holder.nombre.text = juego.nombre

        if (esRankingVentas) {
            // Ranking basado en la posición de la lista (ya viene ordenada de Firebase)
            val puesto = position + 1
            holder.metrica.text = "${puesto}º más comprado\n(${juego.rendimiento_ventas} ventas)"
        } else {
            holder.metrica.text = "${juego.rendimiento_vistas} vistas"
        }
        Glide.with(holder.itemView.context).load(juego.imagenUrl).into(holder.imagen)
    }

    override fun getItemCount() = lista.size
}