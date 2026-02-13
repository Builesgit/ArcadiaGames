package com.example.prueba1integrador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class IncidenciaAdapter(
    private var listaIncidencias: List<Incidencia>,
    private val onItemClick: (Incidencia) -> Unit
) : RecyclerView.Adapter<IncidenciaAdapter.IncidenciaViewHolder>() {

    companion object {
        private const val TYPE_BUG = 1
        private const val TYPE_USUARIO = 2
        private const val TYPE_JUEGO = 3
        private const val TYPE_OTROS = 4
    }

    override fun getItemViewType(position: Int): Int {
        val tipo = listaIncidencias[position].tipo.lowercase()
        return when {
            tipo.contains("bug") -> TYPE_BUG
            tipo.contains("usuario") -> TYPE_USUARIO
            tipo.contains("juego") -> TYPE_JUEGO
            else -> TYPE_OTROS
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncidenciaViewHolder {
        val layout = when (viewType) {
            TYPE_BUG -> R.layout.item_mostrar_incidencia_bug
            TYPE_USUARIO -> R.layout.item_mostrar_incidencia_usuario
            TYPE_JUEGO -> R.layout.item_mostrar_incidencia_juego
            else -> R.layout.item_mostrar_incidencia_otros
        }
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return IncidenciaViewHolder(view)
    }

    override fun onBindViewHolder(holder: IncidenciaViewHolder, position: Int) {
        val incidencia = listaIncidencias[position]
        holder.bind(incidencia, onItemClick)
    }

    override fun getItemCount(): Int = listaIncidencias.size

    fun updateList(newList: List<Incidencia>) {
        listaIncidencias = newList
        notifyDataSetChanged()
    }

    class IncidenciaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvUsuario: TextView = view.findViewById(R.id.tv_usuario)
        private val tvTema: TextView = view.findViewById(R.id.tv_tema_incidencia)
        private val tvDesc: TextView = view.findViewById(R.id.tv_descripcion_incidencia)
        private val tvInfo: TextView = view.findViewById(R.id.tv_infoadicional)

        fun bind(incidencia: Incidencia, onClick: (Incidencia) -> Unit) {
            tvUsuario.text = incidencia.usuarioEmail
            tvTema.text = incidencia.tema
            tvDesc.text = incidencia.descripcion
            tvInfo.text = incidencia.infoAdicional
            
            itemView.setOnClickListener { onClick(incidencia) }
        }
    }
}