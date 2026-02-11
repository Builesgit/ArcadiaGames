package com.example.prueba1integrador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class HistorialAdapter(
    private val lista: List<AccionHistorial>,
    private val esModoMenu: Boolean = false // Nuevo parámetro para alternar vistas
) : RecyclerView.Adapter<HistorialAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtTipoAccion: TextView = view.findViewById(R.id.txtTipoAccion)
        val txtDetalleAccion: TextView = view.findViewById(R.id.txtDetalleAccion)
        val txtTiempo: TextView = view.findViewById(R.id.txtTiempo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Seleccionamos el layout según el contexto (Home vs Vista Completa)
        val layout = if (esModoMenu) R.layout.item_menu_historial else R.layout.item_historial
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]

        // 1. TÍTULO DINÁMICO (Mejorado para detectar las nuevas acciones de stock)
        holder.txtTipoAccion.text = when {
            item.accion.contains("añadió", ignoreCase = true) -> "Nuevo Producto"
            item.accion.contains("eliminó", ignoreCase = true) -> "Producto Eliminado"
            item.accion.contains("redujo", ignoreCase = true) ||
                    item.accion.contains("stock", ignoreCase = true) -> "Stock Actualizado" // <--- NUEVO
            item.accion.contains("actualizó", ignoreCase = true) -> "Producto Actualizado"
            item.accion.contains("alquiló", ignoreCase = true) -> "Producto Alquilado"
            item.accion.contains("compró", ignoreCase = true) -> "Compra Realizada"
            else -> "Actividad"
        }

        // 2. DETALLE DE LA ACCIÓN
        // Corregido: usando txtDetalleAccion para que coincida con tu ViewHolder
        val accionAMostrar = when {
            item.accion.contains("redujo", ignoreCase = true) -> "redujo stock"
            item.accion.contains("añadió", ignoreCase = true) -> "añadió"
            item.accion.contains("eliminó", ignoreCase = true) -> "eliminó"
            item.accion.contains("actualizó", ignoreCase = true) -> "actualizó"
            item.accion.contains("alquiló", ignoreCase = true) -> "alquiló"
            item.accion.contains("compró", ignoreCase = true) -> "compró"
            else -> item.accion
        }

        // Usamos el nombre correcto de la variable: txtDetalleAccion
        if (item.accion.contains("(")) {
            holder.txtDetalleAccion.text = "${item.usuarioNombre} ${item.accion}: '${item.productoNombre}'"
        } else {
            holder.txtDetalleAccion.text = "${item.usuarioNombre} $accionAMostrar: '${item.productoNombre}'"
        }

        // 3. Lógica de tiempo relativo
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