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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class JuegoAdapter(
    private var listaJuego: List<Juego>,
    private val esCarousel: Boolean = false,
    private val esAdmin: Boolean = false,
    private val onJuegoClick: (Juego) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
            ListaViewHolder(inflater.inflate(R.layout.item_juego_catalogo_u, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val actualPosition = if (esCarousel && listaJuego.isNotEmpty()) position % listaJuego.size else position
        val juego = listaJuego[actualPosition]

        holder.itemView.setOnClickListener {
            if (!esAdmin) {
                mostrarDialogoDetalle(holder.itemView, juego)
            } else {
                onJuegoClick(juego)
            }
        }

        val imageView = if (holder is CarouselViewHolder) holder.ivPortada else (holder as ListaViewHolder).ivPortada

        Glide.with(imageView.context)
            .load(juego.imagenUrl)
            .centerCrop()
            .transition(DrawableTransitionOptions.withCrossFade())
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(imageView)

        when (holder) {
            is CarouselViewHolder -> {
                holder.tvTitulo.text = juego.nombre
                holder.tvPrecio.text = juego.precio
            }
            is ListaViewHolder -> {
                holder.tvTitulo.text = juego.nombre
                holder.tvPrecio.text = juego.precio
                holder.tvDescripcion.text = juego.descripcion
                holder.tvCategoria.text = juego.categoria
                holder.tvPlataformas.text = juego.plataforma.ifEmpty {
                    juego.tags.joinToString(" · ")
                }
            }
        }
    }

    private fun mostrarDialogoDetalle(view: View, juego: Juego) {
        val context = view.context
        val dialogBinding = DialogoDetalleJuegoBinding.inflate(LayoutInflater.from(context))

        val builder = AlertDialog.Builder(context)
        builder.setView(dialogBinding.root)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.tvDetalleNombre.text = juego.nombre
        dialogBinding.tvDetalleDescripcion.text = juego.descripcion
        dialogBinding.tvDetallePrecio.text = juego.precio
        dialogBinding.tvDetalleCategoria.text = juego.categoria
        dialogBinding.tvDetallePlataformas.text = juego.plataforma

        if (juego.stock > 0) {
            dialogBinding.tvDetalleStock.text = "Stock: ${juego.stock}"
            dialogBinding.tvDetalleStock.setTextColor(Color.parseColor("#4CAF50"))
            dialogBinding.btnComprar.isEnabled = true
            dialogBinding.btnAlquilar.isEnabled = true
        } else {
            dialogBinding.tvDetalleStock.text = "Agotado"
            dialogBinding.tvDetalleStock.setTextColor(Color.RED)
            dialogBinding.btnComprar.isEnabled = false
            dialogBinding.btnAlquilar.isEnabled = false
        }

        Glide.with(context).load(juego.imagenUrl).into(dialogBinding.ivDetalleImagen)

        dialogBinding.btnComprar.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            if (uid.isEmpty()) {
                Toast.makeText(context, "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // SOLO AÑADIR A CESTA: La compra real ocurre en PagoActivity
            FirebaseDatabase.getInstance().getReference("cesta").child(uid).child(juego.id).setValue(juego)
                .addOnSuccessListener {
                    Toast.makeText(context, "Añadido a la cesta", Toast.LENGTH_SHORT).show()
                    context.startActivity(Intent(context, CestaActivity::class.java))
                    dialog.dismiss()
                }
        }

        dialogBinding.btnAlquilar.setOnClickListener {
            val intent = Intent(context, AlquilarJuegoActivity::class.java)
            intent.putExtra("JUEGO", juego)
            context.startActivity(intent)
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun getItemCount(): Int = if (esCarousel && listaJuego.isNotEmpty()) INFINITE_COUNT else listaJuego.size

    class CarouselViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPortada: ImageView = view.findViewById(R.id.img_juego)
        val tvTitulo: TextView = view.findViewById(R.id.tv_nombre_jc)
        val tvPrecio: TextView = view.findViewById(R.id.tv_precio_jc)
    }

    class ListaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPortada: ImageView = view.findViewById(R.id.iv_juego_portada)
        val tvTitulo: TextView = view.findViewById(R.id.tv_juego_titulo)
        val tvDescripcion: TextView = view.findViewById(R.id.tv_juego_descripcion)
        val tvPrecio: TextView = view.findViewById(R.id.tv_juego_precio)
        val tvCategoria: TextView = view.findViewById(R.id.tv_juego_categoria)
        val tvPlataformas: TextView = view.findViewById(R.id.tv_juego_plataformas)
    }

    fun setFilteredList(filteredList: List<Juego>) {
        this.listaJuego = filteredList
        notifyDataSetChanged()
    }
}