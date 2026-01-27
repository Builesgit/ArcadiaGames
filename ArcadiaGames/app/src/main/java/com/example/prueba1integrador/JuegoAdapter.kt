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
    }

    override fun getItemViewType(position: Int): Int = if (esCarousel) VIEW_TYPE_CAROUSEL else VIEW_TYPE_LISTA

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_CAROUSEL) {
            // Infla el diseño del carrusel (el de 180dp de ancho)
            CarouselViewHolder(inflater.inflate(R.layout.item_novedades_carrousel, parent, false))
        } else {
            // Infla el diseño del catálogo (el de toda la pantalla)
            ListaViewHolder(inflater.inflate(R.layout.item_juego_catalogo_u, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val juego = listaJuego[position]
        when (holder) {
            is CarouselViewHolder -> {
                holder.tvTitulo.text = juego.nombre
                holder.tvPrecio.text = juego.precio
                holder.ivPortada.setImageResource(juego.imagenResId)
            }
            is ListaViewHolder -> {
                holder.tvTitulo.text = juego.nombre
                holder.tvPrecio.text = juego.precio
                holder.tvDescripcion.text = juego.descripcion
                holder.ivPortada.setImageResource(juego.imagenResId)
                // Usamos joinToString para que los tags se vean bonitos: "PC • Xbox"
                holder.tvTags.text = juego.tags.joinToString(" • ")
            }
        }
    }

    override fun getItemCount(): Int = listaJuego.size

    // --- VIEW HOLDERS SEPARADOS PARA EVITAR ERRORES DE ID ---

    // Este solo busca los IDs de item_novedades_carrousel.xml
    class CarouselViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPortada: ImageView = view.findViewById(R.id.img_juego) // ID correcto del carrusel
        val tvTitulo: TextView = view.findViewById(R.id.tv_nombre_jc) // ID correcto del carrusel
        val tvPrecio: TextView = view.findViewById(R.id.tv_precio_jc) // ID correcto del carrusel
    }

    // Este solo busca los IDs de item_juego_catalogo_u.xml
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