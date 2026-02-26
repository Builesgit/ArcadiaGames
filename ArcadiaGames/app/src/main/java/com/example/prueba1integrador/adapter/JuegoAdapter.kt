package com.example.prueba1integrador.adapter

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
import com.example.prueba1integrador.model.Juego
import com.example.prueba1integrador.R
import com.example.prueba1integrador.activity.AlquilarJuegoActivity
import com.example.prueba1integrador.activity.HomeActivity
import com.example.prueba1integrador.databinding.DialogoDetalleJuegoBinding
import com.example.prueba1integrador.manager.FirebaseInventoryManager
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
                // Registro de la vista en estadísticas al abrir el detalle
                inventoryManager.registrarVista(juego.id)
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
                holder.tvPlataformas.text = juego.plataforma
            }
        }
    }

    private fun mostrarDialogoDetalle(view: View, juego: Juego) {
        val context = view.context
        val dialogBinding = DialogoDetalleJuegoBinding.inflate(LayoutInflater.from(context))

        val builder = AlertDialog.Builder(context)
        builder.setView(dialogBinding.root)
        builder.setCancelable(true)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.tvDetalleNombre.text = juego.nombre
        dialogBinding.tvDetalleDescripcion.text = juego.descripcion
        dialogBinding.tvDetallePrecio.text = juego.precio
        dialogBinding.tvDetalleCategoria.text = juego.categoria

        // --- LÓGICA DE STOCK Y BLOQUEO DE BOTONES ---
        val hayStockTotal = (juego.stock ?: 0) > 0

        if (!hayStockTotal) {
            dialogBinding.tvDetalleStock.visibility = View.VISIBLE
            dialogBinding.tvDetalleStock.text = "AGOTADO"
            dialogBinding.tvDetalleStock.setTextColor(Color.parseColor("#FF5252"))

            dialogBinding.btnComprar.isEnabled = false
            dialogBinding.btnComprar.alpha = 0.5f
            dialogBinding.btnAlquilar.isEnabled = false
            dialogBinding.btnAlquilar.alpha = 0.5f
        } else {
            dialogBinding.tvDetalleStock.visibility = View.GONE
            dialogBinding.btnComprar.isEnabled = true
            dialogBinding.btnComprar.alpha = 1.0f
            dialogBinding.btnAlquilar.isEnabled = true
            dialogBinding.btnAlquilar.alpha = 1.0f
        }

        // 1. GENERACIÓN DE CHIPS DE PLATAFORMA
        dialogBinding.chipGroupPlataformasDetalle.removeAllViews()
        val plataformasArray = juego.plataforma.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        plataformasArray.forEachIndexed { index, plat ->
            val chip = Chip(context)
            chip.text = plat
            chip.isCheckable = true
            chip.isCheckedIconVisible = false
            chip.setTextColor(Color.WHITE)
            chip.setChipBackgroundColorResource(R.color.chip_selector_azul)
            dialogBinding.chipGroupPlataformasDetalle.addView(chip)

            // Seleccionar la primera por defecto si hay stock
            if (index == 0 && hayStockTotal) chip.isChecked = true
        }

        Glide.with(context).load(juego.imagenUrl).into(dialogBinding.ivDetalleImagen)

        // 2. BOTÓN COMPRAR
        dialogBinding.btnComprar.setOnClickListener {
            if (!hayStockTotal) {
                Toast.makeText(context, "Producto agotado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            if (uid.isEmpty()) {
                Toast.makeText(context, "Inicia sesión para comprar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedId = dialogBinding.chipGroupPlataformasDetalle.checkedChipId
            if (selectedId != View.NO_ID) {
                val plataformaElegida = dialogBinding.chipGroupPlataformasDetalle.findViewById<Chip>(selectedId).text.toString()
                val juegoCompra = juego.copy(plataforma = plataformaElegida)

                FirebaseDatabase.getInstance().getReference("cesta").child(uid).child(juego.id)
                    .setValue(juegoCompra)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Añadido: $plataformaElegida", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
            } else {
                Toast.makeText(context, "Selecciona una plataforma", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. BOTÓN ALQUILAR
        dialogBinding.btnAlquilar.setOnClickListener {
            if (!hayStockTotal) return@setOnClickListener
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