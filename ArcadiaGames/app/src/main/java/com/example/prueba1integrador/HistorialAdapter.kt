package com.example.prueba1integrador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistorialAdapter(
    private val lista: List<AccionHistorial>,
    private val esModoMenu: Boolean = false
) : RecyclerView.Adapter<HistorialAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtTipoAccion: TextView = view.findViewById(R.id.txtTipoAccion)
        val txtDetalleAccion: TextView = view.findViewById(R.id.txtDetalleAccion)
        val txtTiempo: TextView = view.findViewById(R.id.txtTiempo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = if (esModoMenu) R.layout.item_menu_historial else R.layout.item_historial
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]

        // 1. TÍTULO DINÁMICO (Sincronizado con Manager y Adapter)
        // Usamos ignoreCase = true para evitar el error de "Actividad"
        holder.txtTipoAccion.text = when {
            item.accion.contains("realizado", ignoreCase = true) -> "Alquiler Realizado"
            item.accion.contains("devuelto", ignoreCase = true) -> "Alquiler Devuelto"
            item.accion.contains("Añadió nuevo", ignoreCase = true) -> "Nuevo Producto"
            item.accion.contains("vació", ignoreCase = true) -> "Producto Agotado"
            item.accion.contains("compró", ignoreCase = true) || item.accion.contains("compra", ignoreCase = true) -> "Compra Realizada"
            item.accion.contains("Actualizó stock", ignoreCase = true) ||
                    item.accion.contains("redujo stock", ignoreCase = true) ||
                    item.accion.contains("stock", ignoreCase = true) -> "Stock Actualizado"
            else -> "Actividad"
        }

        // 2. DETALLE DE LA ACCIÓN
        // Mostramos quién hizo qué y sobre qué producto
        holder.txtDetalleAccion.text = "${item.usuarioNombre} ${item.accion}: '${item.productoNombre}'"

        // 3. LÓGICA DE TIEMPO RELATIVO
        val ahora = System.currentTimeMillis()
        val diff = ahora - item.fecha

        val segundos = diff / 1000
        val minutos = segundos / 60
        val horas = minutos / 60
        val dias = horas / 24

        holder.txtTiempo.text = when {
            minutos < 1 -> "ahora"
            minutos < 60 -> "hace $minutos min"
            horas < 24 -> "hace $horas h"
            else -> "hace $dias d"
        }
    }

    override fun getItemCount() = lista.size
}