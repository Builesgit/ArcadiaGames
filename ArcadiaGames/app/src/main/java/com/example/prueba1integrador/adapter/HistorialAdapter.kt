package com.example.prueba1integrador.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.prueba1integrador.model.AccionHistorial
import com.example.prueba1integrador.R

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

        // 1. TÍTULO DINÁMICO (Prioridad por palabra clave de acción)
        holder.txtTipoAccion.text = when {
            // Si la acción es específicamente el aviso de stock agotado
            item.accion.contains("Agotado", ignoreCase = true) -> "STOCK AGOTADO"

            // Si la acción contiene compra
            item.accion.contains("compró", ignoreCase = true) ||
                    item.accion.contains("compra", ignoreCase = true) -> "COMPRA REALIZADA"

            // Resto de casos
            item.accion.contains("realizado", ignoreCase = true) -> "ALQUILER REALIZADO"
            item.accion.contains("devuelto", ignoreCase = true) -> "ALQUILER DEVUELTO"
            item.accion.contains("Añadió", ignoreCase = true) ||
                    item.accion.contains("nuevo", ignoreCase = true) -> "NUEVO PRODUCTO"
            item.accion.contains("borró", ignoreCase = true) ||
                    item.accion.contains("eliminó", ignoreCase = true) -> "PRODUCTO ELIMINADO"
            item.accion.contains("stock", ignoreCase = true) -> "STOCK ACTUALIZADO"
            else -> "ACTIVIDAD"
        }

        // 2. DETALLE DE LA ACCIÓN
        // Si es el registro de agotado, usamos el formato directo
        if (item.accion.contains("Agotado", ignoreCase = true)) {
            holder.txtDetalleAccion.text = "Agotado el stock de '${item.productoNombre}'"
        } else {
            // Para la compra, alquiler, etc., usamos el formato estándar
            holder.txtDetalleAccion.text = "${item.usuarioNombre} ${item.accion}: '${item.productoNombre}'"
        }

        // 3. TIEMPO RELATIVO
        val ahora = System.currentTimeMillis()
        val diff = ahora - item.fecha
        val minutos = (diff / 1000) / 60
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