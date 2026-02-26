package com.example.prueba1integrador.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.prueba1integrador.R
import com.example.prueba1integrador.model.Juego

class EstadisticasAdapter(
    private val lista: List<Juego>,
    private val esRankingVentas: Boolean = false
) : RecyclerView.Adapter<EstadisticasAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imagen: ImageView = view.findViewById(R.id.iv_juego_stat)
        val nombre: TextView = view.findViewById(R.id.tv_nombre_stat)
        val metrica: TextView = view.findViewById(R.id.tv_metrica_stat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_juego_estadisticas, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val juego = lista[position]
        holder.nombre.text = juego.nombre

        if (esRankingVentas) {
            // Ranking de Ventas
            val puesto = position + 1
            holder.metrica.text = "${puesto}º más vendido\n(${juego.rendimiento_ventas} ventas)"
            holder.metrica.setTextColor(android.graphics.Color.parseColor("#4CAF50")) // Verde
        } else {
            // Ranking de Vistas
            holder.metrica.text = "${juego.rendimiento_vistas} visualizaciones"
            holder.metrica.setTextColor(android.graphics.Color.parseColor("#FFC107")) // Ámbar
        }

        Glide.with(holder.itemView.context)
            .load(juego.imagenUrl)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(holder.imagen)
    }

    override fun getItemCount() = lista.size
}