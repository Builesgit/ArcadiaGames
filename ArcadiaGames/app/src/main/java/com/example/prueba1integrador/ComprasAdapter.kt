package com.example.prueba1integrador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*
import kotlin.let
import kotlin.text.isNotEmpty

class ComprasAdapter(
    private val listaCompras: List<JuegoComprado>
) : RecyclerView.Adapter<ComprasAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.tvNombreJuego)
        val plataforma: TextView = view.findViewById(R.id.tvPlataforma)
        val precio: TextView = view.findViewById(R.id.tvPrecio)
        val fecha: TextView = view.findViewById(R.id.tvFecha)
        val imagen: ImageView = view.findViewById(R.id.imgJuego)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_compra, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listaCompras.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = listaCompras[position]
        val juego = item.juego

        holder.nombre.text = juego.nombre
        holder.plataforma.text = "Plataforma: ${juego.plataforma}"
        holder.precio.text = "Precio: ${juego.precio}"

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        if (item.esAlquiler) {

            val inicio = item.fechaInicio?.let { Date(it) }
            val fin = item.fechaFin?.let { Date(it) }

            if (inicio != null && fin != null) {
                holder.fecha.text =
                    "Alquilado del ${sdf.format(inicio)} al ${sdf.format(fin)}"
            } else {
                holder.fecha.text = "Alquiler activo"
            }

        } else {

            val fechaCompra = item.fechaCompra?.let { Date(it) }
            holder.fecha.text =
                fechaCompra?.let { "Comprado el ${sdf.format(it)}" }
                    ?: "Compra registrada"
        }

        // Cargar imagen
        if (juego.imagenUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(juego.imagenUrl)
                .into(holder.imagen)
        } else if (juego.imagenResId != 0) {
            holder.imagen.setImageResource(juego.imagenResId)
        }
    }
}
