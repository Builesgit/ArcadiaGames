package com.example.prueba1integrador.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.prueba1integrador.model.JuegoComprado
import com.example.prueba1integrador.R
import java.text.SimpleDateFormat
import java.util.*

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

        // 🔥 RESET COLOR SIEMPRE (muy importante)
        holder.fecha.setTextColor(Color.WHITE)

        holder.nombre.text = juego.nombre
        holder.plataforma.text = "Plataforma: ${juego.plataforma}"
        holder.precio.text = "Precio: ${juego.precio}"

        // =============================
        // FECHAS Y ESTADO
        // =============================
        if (item.esAlquiler) {

            val inicioMillis = item.fechaInicioMillis
            val finMillis = item.fechaFinMillis

            val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            val inicioStr = inicioMillis?.let { formato.format(Date(it)) } ?: "-"
            val finStr = finMillis?.let { formato.format(Date(it)) } ?: "-"

            holder.fecha.text = "Alquilado del $inicioStr al $finStr"

            if (finMillis != null) {

                val hoy = System.currentTimeMillis()
                val estadoActivo = hoy <= finMillis
                val estadoTexto = if (estadoActivo) "ACTIVO" else "INACTIVO"

                holder.fecha.append("\nEstado: $estadoTexto")

                // Solo coloreamos el estado, no todo el texto
                if (estadoActivo) {
                    holder.fecha.setTextColor(Color.parseColor("#2E7D32")) // Verde
                } else {
                    holder.fecha.setTextColor(Color.parseColor("#C62828")) // Rojo
                }
            }

        } else {

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaCompra = item.fechaCompra?.let { Date(it) }

            holder.fecha.text =
                fechaCompra?.let { "Comprado el ${sdf.format(it)}" }
                    ?: "Compra registrada"

            // 🔥 Nos aseguramos que compras siempre sean blancas
            holder.fecha.setTextColor(Color.WHITE)
        }

        // =============================
        // CARGAR IMAGEN
        // =============================
        if (!juego.imagenUrl.isNullOrBlank()) {

            Glide.with(holder.itemView.context)
                .load(juego.imagenUrl)
                .placeholder(R.drawable.logo_perfil)
                .error(R.drawable.logo_perfil)
                .into(holder.imagen)

        } else if (juego.imagenResId != 0) {

            holder.imagen.setImageResource(juego.imagenResId)

        } else {

            holder.imagen.setImageResource(R.drawable.logo_perfil)
        }
    }
}