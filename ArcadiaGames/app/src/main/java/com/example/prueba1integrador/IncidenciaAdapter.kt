package com.example.prueba1integrador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class IncidenciaAdapter(
    private var lista: List<Incidencia>,
    private val onClick: (Incidencia) -> Unit
) : RecyclerView.Adapter<IncidenciaAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: View = view.findViewById(R.id.item_container)
        val tvTema: TextView = view.findViewById(R.id.tv_tema_incidencia)
        val tvUser: TextView = view.findViewById(R.id.tv_usuario)
        val tvTag: TextView = view.findViewById(R.id.tv_tag_tipo)
        val ivIcono: ImageView = view.findViewById(R.id.iv_icono_tipo)
        val cardIcono: CardView = view.findViewById(R.id.card_icono_bg)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.tvTema.text = item.tema
        holder.tvUser.text = item.usuarioEmail
        holder.tvTag.text = item.tipo.uppercase()

        // LÓGICA DE ESTILOS DINÁMICOS
        val (colorFondo, colorTexto, drawableTag) = when (item.tipo.lowercase()) {
            "bug" -> Triple("#3D1A1A", "#F0424C", R.drawable.background_bug)
            "juego" -> Triple("#1A2740", "#60A5FA", R.drawable.background_juego)
            "usuario" -> Triple("#1F1C37", "#A855F7", R.drawable.background_usuario)
            else -> Triple("#433F28", "#FACC15", R.drawable.background_otros)
        }

        // Aplicar los colores al diseño unificado
        holder.cardIcono.setCardBackgroundColor(android.graphics.Color.parseColor(colorFondo))
        holder.ivIcono.setColorFilter(android.graphics.Color.parseColor(colorTexto))
        holder.tvTag.setBackgroundResource(drawableTag)

        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mostrar_incidencia, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = lista.size

    fun updateList(nuevaLista: List<Incidencia>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}