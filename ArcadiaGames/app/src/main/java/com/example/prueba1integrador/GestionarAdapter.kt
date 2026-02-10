package com.example.prueba1integrador

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class GestionarAdapter(
    private var listaInventario: List<ItemInventario>,
    private val onDeleteClick: (Juego, List<String>) -> Unit
) : RecyclerView.Adapter<GestionarAdapter.GestionViewHolder>() {

    class GestionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImagen: ImageView = view.findViewById(R.id.iv_juego_mgmt)
        val tvNombre: TextView = view.findViewById(R.id.tv_nombre_mgmt)
        val tvPrecio: TextView = view.findViewById(R.id.tv_precio_mgmt)
        val tvCantidad: TextView = view.findViewById(R.id.tv_cantidad_mgmt)
        val btnEditar: ImageButton = view.findViewById(R.id.btn_editar_mgmt)
        val btnEliminar: ImageButton = view.findViewById(R.id.btn_eliminar_mgmt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_juego_gestion, parent, false)
        return GestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: GestionViewHolder, position: Int) {
        val item = listaInventario[position]
        val juego = item.juego
        val context = holder.itemView.context

        holder.tvNombre.text = juego.nombre
        holder.tvPrecio.text = juego.precio
        holder.tvCantidad.text = "Stock: ${item.cantidad}"
        holder.tvCantidad.visibility = View.VISIBLE

        Glide.with(context)
            .load(juego.imagenUrl)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(holder.ivImagen)

        holder.btnEditar.setOnClickListener {
            val intent = Intent(context, EditarInventarioActivity::class.java)
            intent.putExtra("JUEGO", juego)
            context.startActivity(intent)
        }

        holder.btnEliminar.setOnClickListener {
            onDeleteClick(juego, item.idsAgrupados)
        }
    }

    override fun getItemCount(): Int = listaInventario.size

    fun actualizarLista(nuevaLista: List<ItemInventario>) {
        listaInventario = nuevaLista
        notifyDataSetChanged()
    }
}
