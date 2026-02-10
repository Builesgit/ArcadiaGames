package com.example.prueba1integrador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class HistorialAdapter(private val lista: List<AccionHistorial>) :
    RecyclerView.Adapter<HistorialAdapter.ViewHolder>() {

    // ViewHolder actualizado con los nuevos IDs de tu diseño premium
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtTipoAccion: TextView = view.findViewById(R.id.txtTipoAccion)
        val txtDetalleAccion: TextView = view.findViewById(R.id.txtDetalleAccion)
        val txtTiempo: TextView = view.findViewById(R.id.txtTiempo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflamos el nuevo layout item_historial
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_historial, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]

        // 1. Título dinámico basado en la acción (estilo gamer)
        holder.txtTipoAccion.text = when {
            item.accion.contains("compró", ignoreCase = true) -> "Compra Realizada"
            item.accion.contains("añadió", ignoreCase = true) -> "Nuevo Producto Añadido"
            item.accion.contains("eliminó", ignoreCase = true) -> "Producto Eliminado"
            item.accion.contains("intercambió", ignoreCase = true) -> "Juego Intercambiado"
            else -> "Actividad Detectada"
        }

        // 2. Detalle de la acción (Quién hizo qué)
        // Ejemplo: "Admin añadió: 'Elden Ring'" o "Juan compró: 'FIFA 24'"
        holder.txtDetalleAccion.text = "${item.usuarioNombre} ${item.accion}: '${item.productoNombre}'"

        // 3. Lógica de tiempo relativo (hace X min / hace X h)
        val ahora = System.currentTimeMillis()
        val diff = ahora - item.fecha

        val segundos = diff / 1000
        val minutos = segundos / 60
        val horas = minutos / 60
        val dias = horas / 24

        holder.txtTiempo.text = when {
            minutos < 1 -> "ahora mismo"
            minutos < 60 -> "hace $minutos min"
            horas < 24 -> "hace $horas h"
            else -> "hace $dias d"
        }
    }

    override fun getItemCount() = lista.size
}