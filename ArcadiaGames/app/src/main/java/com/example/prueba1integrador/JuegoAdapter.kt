package com.example.prueba1integrador

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.prueba1integrador.databinding.DialogoDetalleJuegoBinding
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class JuegoAdapter(
    private var listaJuego: List<Juego>,
    private val esCarousel: Boolean = false,
    private val esAdmin: Boolean = false,
    private val onJuegoClick: (Juego) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val inventoryManager = FirebaseInventoryManager()

    companion object {
        private const val VIEW_TYPE_LISTA = 1
        private const val VIEW_TYPE_CAROUSEL = 2
        private const val INFINITE_COUNT = 10000
    }

    override fun getItemViewType(position: Int): Int = if (esCarousel) VIEW_TYPE_CAROUSEL else VIEW_TYPE_LISTA

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_CAROUSEL) {
            CarouselViewHolder(inflater.inflate(R.layout.item_novedades_carrousel, parent, false))
        } else {
            ListaViewHolder(inflater.inflate(R.layout.item_juego_catalogo, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val actualPosition = if (esCarousel && listaJuego.isNotEmpty()) position % listaJuego.size else position
        val juego = listaJuego[actualPosition]

        holder.itemView.setOnClickListener {
            if (!esAdmin) {
                // Registro inmediato de la vista en estadísticas
                inventoryManager.registrarVista(juego.id)
                mostrarDialogoDetalle(holder.itemView, juego)
            } else {
                onJuegoClick(juego)
            }
        }

        val imageView = if (holder is CarouselViewHolder) holder.ivPortada else (holder as ListaViewHolder).ivPortada
        Glide.with(imageView.context).load(juego.imagenUrl).centerCrop().into(imageView)

        // Asignación de textos según Holder...
        if (holder is CarouselViewHolder) {
            holder.tvTitulo.text = juego.nombre
            holder.tvPrecio.text = juego.precio
        } else if (holder is ListaViewHolder) {
            holder.tvTitulo.text = juego.nombre
            holder.tvPrecio.text = juego.precio
            holder.tvDescripcion.text = juego.descripcion
            holder.tvPlataformas.text = juego.plataforma
        }
    }

    private fun mostrarDialogoDetalle(view: View, juego: Juego) {
        val context = view.context
        val dialogBinding = DialogoDetalleJuegoBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(context).setView(dialogBinding.root).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.tvDetalleNombre.text = juego.nombre
        dialogBinding.tvDetallePrecio.text = juego.precio

        val hayStock = (juego.stock) > 0
        if (!hayStock) {
            dialogBinding.tvDetalleStock.visibility = View.VISIBLE
            dialogBinding.btnComprar.isEnabled = false
            dialogBinding.btnComprar.alpha = 0.5f
        }

        // Lógica de Chips y Botón Comprar...
        dialogBinding.btnComprar.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val selectedId = dialogBinding.chipGroupPlataformasDetalle.checkedChipId
            if (uid.isNotEmpty() && selectedId != View.NO_ID) {
                val plat = dialogBinding.chipGroupPlataformasDetalle.findViewById<Chip>(selectedId).text.toString()
                FirebaseDatabase.getInstance().getReference("cesta").child(uid).child(juego.id)
                    .setValue(juego.copy(plataforma = plat)).addOnSuccessListener {
                        Toast.makeText(context, "Añadido", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
            }
        }
        dialog.show()
    }

    override fun getItemCount(): Int = if (esCarousel) INFINITE_COUNT else listaJuego.size
    class CarouselViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val ivPortada: ImageView = v.findViewById(R.id.img_juego)
        val tvTitulo: TextView = v.findViewById(R.id.tv_nombre_jc)
        val tvPrecio: TextView = v.findViewById(R.id.tv_precio_jc)
    }
    class ListaViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val ivPortada: ImageView = v.findViewById(R.id.iv_juego_portada)
        val tvTitulo: TextView = v.findViewById(R.id.tv_juego_titulo)
        val tvDescripcion: TextView = v.findViewById(R.id.tv_juego_descripcion)
        val tvPrecio: TextView = v.findViewById(R.id.tv_juego_precio)
        val tvPlataformas: TextView = v.findViewById(R.id.tv_juego_plataformas)
    }
    fun setFilteredList(list: List<Juego>) { this.listaJuego = list; notifyDataSetChanged() }
}