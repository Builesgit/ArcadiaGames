package com.example.prueba1integrador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class JuegoAdapter(
    private var listaJuego: List<Juego>,
    private val esCarousel: Boolean = false
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_LISTA = 1
        private const val VIEW_TYPE_CAROUSEL = 2
        // Usamos un valor grande pero no infinito (Int.MAX_VALUE) para evitar OutOfMemory
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
        // Obtenemos la posición real dentro de tu lista de juegos
        val actualPosition = if (esCarousel && listaJuego.isNotEmpty()) {
            position % listaJuego.size
        } else {
            position
        }

        val juego = listaJuego[actualPosition]

        when (holder) {
            is CarouselViewHolder -> {
                holder.tvTitulo.text = juego.nombre
                holder.tvPrecio.text = juego.precio // Aquí ya saldrá el precio corregido
                holder.ivPortada.setImageResource(juego.imagenResId)
            }
            is ListaViewHolder -> {
                holder.tvTitulo.text = juego.nombre
                holder.tvPrecio.text = juego.precio
                holder.tvDescripcion.text = juego.descripcion
                holder.ivPortada.setImageResource(juego.imagenResId)
                holder.tvTags.text = juego.tags.joinToString(" • ")
            }
        }
    }

    override fun getItemCount(): Int {
        return if (esCarousel && listaJuego.isNotEmpty()) INFINITE_COUNT else listaJuego.size
    }

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
        val tvTags: TextView = view.findViewById(R.id.tv_juego_tags)
    }

    fun setFilteredList(filteredList: List<Juego>) {
        this.listaJuego = filteredList
        notifyDataSetChanged()
    }
}