package com.example.prueba1integrador

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.prueba1integrador.databinding.DialogoDetalleJuegoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class JuegoAdapter(
    private var listaJuego: List<Juego>,
    private val esCarousel: Boolean = false,
    private val esAdmin: Boolean = false
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_LISTA = 1
        private const val VIEW_TYPE_CAROUSEL = 2
        private const val INFINITE_COUNT = 10000
    }

    override fun getItemViewType(position: Int): Int {
        return if (esCarousel) VIEW_TYPE_CAROUSEL else VIEW_TYPE_LISTA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_CAROUSEL) {
            CarouselViewHolder(inflater.inflate(R.layout.item_novedades_carrousel, parent, false))
        } else {
            ListaViewHolder(inflater.inflate(R.layout.item_juego_catalogo_u, parent, false))
        }
    }

    override fun getItemCount(): Int {
        return if (esCarousel && listaJuego.isNotEmpty()) INFINITE_COUNT else listaJuego.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val actualPosition = if (esCarousel && listaJuego.isNotEmpty()) {
            position % listaJuego.size
        } else {
            position
        }

        val juego = listaJuego[actualPosition]

        holder.itemView.setOnClickListener {
            if (!esAdmin) {
                mostrarDialogoDetalle(holder.itemView, juego)
            }
        }

        fun cargarImagen(imageView: ImageView) {
            if (juego.imagenUrl.isNotEmpty()) {
                Glide.with(imageView.context)
                    .load(juego.imagenUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_background)
                    .into(imageView)
            } else if (juego.imagenResId != 0) {
                imageView.setImageResource(juego.imagenResId)
            } else {
                imageView.setImageResource(R.drawable.ic_launcher_background)
            }
        }

        when (holder) {
            is CarouselViewHolder -> {
                holder.tvTitulo.text = juego.nombre
                holder.tvPrecio.text = juego.precio
                cargarImagen(holder.ivPortada)
            }

            is ListaViewHolder -> {
                holder.tvTitulo.text = juego.nombre
                holder.tvDescripcion.text = juego.descripcion
                holder.tvPrecio.text = juego.precio

                // ✅ SEPARACIÓN CORRECTA
                holder.tvCategoria.text = juego.categoria

                holder.tvPlataformas.text =
                    juego.plataforma.ifEmpty {
                        juego.tags.joinToString(" · ")
                    }

                cargarImagen(holder.ivPortada)
            }
        }
    }

    // ---------------- VIEW HOLDERS ----------------

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

        // 🆕 NUEVOS CAMPOS
        val tvCategoria: TextView = view.findViewById(R.id.tv_juego_categoria)
        val tvPlataformas: TextView = view.findViewById(R.id.tv_juego_plataformas)
    }

    /**
     * Popup de detalle del juego
     */
    private fun mostrarDialogoDetalle(view: View, juego: Juego) {

        val context = view.context

        val dialogBinding = DialogoDetalleJuegoBinding.inflate(
            LayoutInflater.from(context)
        )

        val dialog = AlertDialog.Builder(context)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.tvDetalleNombre.text = juego.nombre
        dialogBinding.tvDetalleDescripcion.text = juego.descripcion
        dialogBinding.tvDetallePrecio.text = juego.precio

        dialogBinding.tvDetalleCategoria.text = juego.categoria

        dialogBinding.tvDetallePlataformas.text =
            juego.plataforma.ifEmpty {
                juego.tags.joinToString(" · ")
            }

        if (juego.stock > 0) {
            dialogBinding.tvDetalleStock.text = "Stock: ${juego.stock}"
            dialogBinding.tvDetalleStock.setTextColor(Color.GREEN)
            dialogBinding.btnComprar.isEnabled = true
        } else {
            dialogBinding.tvDetalleStock.text = "Stock: 0"
            dialogBinding.tvDetalleStock.setTextColor(Color.RED)
            dialogBinding.btnComprar.isEnabled = false
        }

        if (juego.imagenUrl.isNotEmpty()) {
            Glide.with(context)
                .load(juego.imagenUrl)
                .into(dialogBinding.ivDetalleImagen)
        } else {
            dialogBinding.ivDetalleImagen.setImageResource(juego.imagenResId)
        }

        dialogBinding.btnComprar.setOnClickListener {

            val uid = FirebaseAuth.getInstance().currentUser!!.uid

            FirebaseDatabase.getInstance()
                .getReference("cesta")
                .child(uid)
                .child(juego.id)
                .setValue(juego)
                .addOnSuccessListener {
                    Toast.makeText(
                        context,
                        "Juego añadido a la cesta",
                        Toast.LENGTH_SHORT
                    ).show()

                    context.startActivity(
                        Intent(context, CestaActivity::class.java)
                    )

                    dialog.dismiss()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        context,
                        "Error Firebase: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
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

    fun setFilteredList(filteredList: List<Juego>) {
        listaJuego = filteredList
        notifyDataSetChanged()
    }
}
