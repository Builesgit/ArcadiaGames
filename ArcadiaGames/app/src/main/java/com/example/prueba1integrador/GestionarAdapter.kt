package com.example.prueba1integrador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

// data class ItemInventario movida a Juego

class GestionarAdapter(
    private var listaInventario: List<ItemInventario>, // Cambiado de List<Juego>
    private val onEditClick: (Juego) -> Unit,
    private val onDeleteClick: (Juego, List<String>) -> Unit // Callback modificado para soportar borrado inteligente
) : RecyclerView.Adapter<GestionarAdapter.GestionViewHolder>() {

    class GestionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImagen: ImageView = view.findViewById(R.id.iv_juego_mgmt)
        val tvNombre: TextView = view.findViewById(R.id.tv_nombre_mgmt)
        val tvPrecio: TextView = view.findViewById(R.id.tv_precio_mgmt)
        val tvCantidad: TextView = view.findViewById(R.id.tv_cantidad_mgmt) // Nuevo badge
        val btnEditar: ImageButton = view.findViewById(R.id.btn_editar_mgmt)
        val btnEliminar: ImageButton = view.findViewById(R.id.btn_eliminar_mgmt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GestionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_juego_gestion, parent, false)
        return GestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: GestionViewHolder, position: Int) {
        val item = listaInventario[position]
        val juego = item.juego

        holder.tvNombre.text = juego.nombre
        holder.tvPrecio.text = juego.precio

        // Mostrar cantidad si es mayor a 1, sino ocultar (o mostrar x1)
        holder.tvCantidad.text = "Stock: ${item.cantidad}"
        holder.tvCantidad.visibility = View.VISIBLE

        Glide.with(holder.itemView.context)
            .load(juego.imagenUrl)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(holder.ivImagen)

        holder.btnEditar.setOnClickListener { onEditClick(juego) }

        // Pasamos tanto el juego como la lista de IDs al callback de eliminar
        holder.btnEliminar.setOnClickListener { onDeleteClick(juego, item.idsAgrupados) }
    }

    override fun getItemCount(): Int = listaInventario.size

    fun actualizarLista(nuevaLista: List<ItemInventario>) {
        listaInventario = nuevaLista
        notifyDataSetChanged()
    }
}
