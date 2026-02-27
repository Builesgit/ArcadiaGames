package com.example.prueba1integrador.adapter

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.prueba1integrador.model.ItemInventario
import com.example.prueba1integrador.model.Juego
import com.example.prueba1integrador.R
import com.example.prueba1integrador.manager.FirebaseInventoryManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class GestionarAdapter(
    private var listaInventario: List<ItemInventario>,
    private val onEditClick: (Juego) -> Unit,
    private val onDataChanged: () -> Unit
) : RecyclerView.Adapter<GestionarAdapter.GestionViewHolder>() {

    private val inventoryManager = FirebaseInventoryManager()
    private val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: "Admin"

    class GestionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImagen: ImageView = view.findViewById(R.id.iv_juego_mgmt)
        val tvNombre: TextView = view.findViewById(R.id.tv_nombre_mgmt)
        val tvPrecio: TextView = view.findViewById(R.id.tv_precio_mgmt)
        val tvCantidad: TextView = view.findViewById(R.id.tv_cantidad_mgmt)
        val tvDetalle: TextView = view.findViewById(R.id.tv_detalle_plataformas)
        val ivFlecha: ImageView = view.findViewById(R.id.iv_flecha_despliegue)
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
        holder.tvDetalle.visibility = View.GONE
        holder.ivFlecha.rotation = 0f

        if (item.cantidad <= 0) {
            holder.tvCantidad.text = "AGOTADO (0)"
            holder.tvCantidad.setTextColor(Color.parseColor("#EF5350"))
            holder.ivFlecha.setColorFilter(Color.parseColor("#EF5350"))
        } else {
            holder.tvCantidad.text = "Stock Total: ${item.cantidad}"
            holder.tvCantidad.setTextColor(Color.parseColor("#4CAF50"))
            holder.ivFlecha.setColorFilter(Color.parseColor("#4CAF50"))
        }

        Glide.with(holder.itemView.context)
            .load(juego.imagenUrl)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(holder.ivImagen)

        holder.itemView.setOnClickListener {
            if (holder.tvDetalle.visibility == View.GONE) {
                holder.ivFlecha.animate().rotation(90f).setDuration(200).start()
                val builder = SpannableStringBuilder()

                if (tienePlataforma(juego, "PlayStation")) appendPlataforma(builder, "PlayStation", item.ps)
                if (tienePlataforma(juego, "Xbox")) appendPlataforma(builder, "Xbox", item.xb)
                if (tienePlataforma(juego, "Nintendo")) appendPlataforma(builder, "Nintendo", item.ni)
                if (tienePlataforma(juego, "PC")) appendPlataforma(builder, "PC", item.pc)

                holder.tvDetalle.text = if (builder.isEmpty()) "Sin plataformas" else builder
                holder.tvDetalle.visibility = View.VISIBLE
            } else {
                holder.ivFlecha.animate().rotation(0f).setDuration(200).start()
                holder.tvDetalle.visibility = View.GONE
            }
        }

        holder.btnEditar.setOnClickListener { mostrarDialogoAnadirStock(item, holder.itemView.context) }
        holder.btnEliminar.setOnClickListener { mostrarDialogoEliminarPro(item, holder.itemView.context) }
    }

    private fun tienePlataforma(juego: Juego, nombrePlat: String): Boolean {
        return juego.plataforma.split(",")
            .map { it.trim().lowercase() }
            .contains(nombrePlat.lowercase())
    }

    private fun appendPlataforma(builder: SpannableStringBuilder, nombre: String, cant: Int) {
        val textoLinea = "$nombre: $cant"
        if (textoLinea.isBlank()) return
        if (builder.isNotEmpty()) builder.append("\n")
        val inicio = builder.length
        builder.append(textoLinea)
        val color = if (cant <= 0) Color.parseColor("#EF5350") else Color.parseColor("#4CAF50")
        if (builder.length > inicio) {
            builder.setSpan(ForegroundColorSpan(color), inicio, builder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    override fun getItemCount(): Int = listaInventario.size

    private fun mostrarDialogoAnadirStock(item: ItemInventario, context: Context) {
        val builder = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialogo_anadir_stock, null)
        builder.setView(view)

        val etPS = view.findViewById<EditText>(R.id.etStockPS)
        val etXbox = view.findViewById<EditText>(R.id.etStockXbox)
        val etNintendo = view.findViewById<EditText>(R.id.etStockNintendo)
        val etPC = view.findViewById<EditText>(R.id.etStockPC)
        val btnConfirmar = view.findViewById<Button>(R.id.btnConfirmarSuma)

        etPS.visibility = if (tienePlataforma(item.juego, "PlayStation")) View.VISIBLE else View.GONE
        etXbox.visibility = if (tienePlataforma(item.juego, "Xbox")) View.VISIBLE else View.GONE
        etNintendo.visibility = if (tienePlataforma(item.juego, "Nintendo")) View.VISIBLE else View.GONE
        etPC.visibility = if (tienePlataforma(item.juego, "PC")) View.VISIBLE else View.GONE

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnConfirmar.setOnClickListener {
            val psNuevos = etPS.text.toString().toIntOrNull() ?: 0
            val xbNuevos = etXbox.text.toString().toIntOrNull() ?: 0
            val niNuevos = etNintendo.text.toString().toIntOrNull() ?: 0
            val pcNuevos = etPC.text.toString().toIntOrNull() ?: 0
            val totalNuevasUnidades = psNuevos + xbNuevos + niNuevos + pcNuevos

            if (totalNuevasUnidades > 0) {
                val nuevoDetalle = mutableMapOf<String, Int>()
                if (etPS.visibility == View.VISIBLE) nuevoDetalle["playstation"] = item.ps + psNuevos
                if (etXbox.visibility == View.VISIBLE) nuevoDetalle["xbox"] = item.xb + xbNuevos
                if (etNintendo.visibility == View.VISIBLE) nuevoDetalle["nintendo"] = item.ni + niNuevos
                if (etPC.visibility == View.VISIBLE) nuevoDetalle["pc"] = item.pc + pcNuevos

                val nuevoTotal = item.cantidad + totalNuevasUnidades

                // --- LOG AUDITORÍA Y ALERTA ---
                inventoryManager.registrarEnHistorial(currentUserEmail, "Añadió stock", item.juego.nombre, totalNuevasUnidades)
                inventoryManager.verificarStockYRegistrar(item.juego, nuevoTotal)

                actualizarFirebaseConDetalle(item.juego.id, nuevoTotal, nuevoDetalle)
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Ingresa cantidades a sumar", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun mostrarDialogoEliminarPro(item: ItemInventario, context: Context) {
        val builder = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialogo_eliminar_producto, null)
        builder.setView(view)

        val etPS = view.findViewById<EditText>(R.id.etEliminarPS)
        val etXB = view.findViewById<EditText>(R.id.etEliminarXB)
        val etNI = view.findViewById<EditText>(R.id.etEliminarNI)
        val etPC = view.findViewById<EditText>(R.id.etEliminarPC)
        val btnConfirmar = view.findViewById<Button>(R.id.btnConfirmarRetiro)
        val btnVaciarTodo = view.findViewById<Button>(R.id.btnEliminarTodo)

        etPS.visibility = if (tienePlataforma(item.juego, "PlayStation")) View.VISIBLE else View.GONE
        etXB.visibility = if (tienePlataforma(item.juego, "Xbox")) View.VISIBLE else View.GONE
        etNI.visibility = if (tienePlataforma(item.juego, "Nintendo")) View.VISIBLE else View.GONE
        etPC.visibility = if (tienePlataforma(item.juego, "PC")) View.VISIBLE else View.GONE

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnConfirmar.setOnClickListener {
            val psRetirar = etPS.text.toString().toIntOrNull() ?: 0
            val xbRetirar = etXB.text.toString().toIntOrNull() ?: 0
            val niRetirar = etNI.text.toString().toIntOrNull() ?: 0
            val pcRetirar = etPC.text.toString().toIntOrNull() ?: 0

            if (psRetirar <= item.ps && xbRetirar <= item.xb && niRetirar <= item.ni && pcRetirar <= item.pc) {
                val sumaRetiro = psRetirar + xbRetirar + niRetirar + pcRetirar
                if (sumaRetiro > 0) {
                    val nuevoMapa = mutableMapOf<String, Int>()
                    if (etPS.visibility == View.VISIBLE) nuevoMapa["playstation"] = item.ps - psRetirar
                    if (etXB.visibility == View.VISIBLE) nuevoMapa["xbox"] = item.xb - xbRetirar
                    if (etNI.visibility == View.VISIBLE) nuevoMapa["nintendo"] = item.ni - niRetirar
                    if (etPC.visibility == View.VISIBLE) nuevoMapa["pc"] = item.pc - pcRetirar

                    val nuevoTotal = item.cantidad - sumaRetiro

                    // --- LOG AUDITORÍA Y ALERTA ---
                    inventoryManager.registrarEnHistorial(currentUserEmail, "Eliminó stock", item.juego.nombre, sumaRetiro)
                    inventoryManager.verificarStockYRegistrar(item.juego, nuevoTotal)

                    actualizarFirebaseConDetalle(item.juego.id, nuevoTotal, nuevoMapa)
                    dialog.dismiss()
                }
            } else {
                Toast.makeText(context, "No puedes retirar más de lo disponible", Toast.LENGTH_SHORT).show()
            }
        }

        btnVaciarTodo.setOnClickListener {
            val nuevoMapa = mutableMapOf<String, Int>()
            if (etPS.visibility == View.VISIBLE) nuevoMapa["playstation"] = 0
            if (etXB.visibility == View.VISIBLE) nuevoMapa["xbox"] = 0
            if (etNI.visibility == View.VISIBLE) nuevoMapa["nintendo"] = 0
            if (etPC.visibility == View.VISIBLE) nuevoMapa["pc"] = 0

            // --- LOG AUDITORÍA ---
            inventoryManager.registrarEnHistorial(currentUserEmail, "Vació todo el stock", item.juego.nombre, item.cantidad)
            inventoryManager.verificarStockYRegistrar(item.juego, 0)

            actualizarFirebaseConDetalle(item.juego.id, 0, nuevoMapa)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun actualizarFirebaseConDetalle(id: String, total: Int, detalle: Map<String, Int>) {
        val dbRef = FirebaseDatabase.getInstance().getReference("productos").child(id)
        val updates = hashMapOf<String, Any>("stock" to total, "detalle_stock" to detalle)
        dbRef.updateChildren(updates).addOnSuccessListener { onDataChanged() }
    }

    fun actualizarLista(nuevaLista: List<ItemInventario>) {
        this.listaInventario = nuevaLista
        notifyDataSetChanged()
    }
}