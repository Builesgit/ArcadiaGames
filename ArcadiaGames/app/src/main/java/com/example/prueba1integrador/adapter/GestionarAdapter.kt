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
import com.example.prueba1integrador.manager.FirebaseInventoryManager
import com.example.prueba1integrador.model.ItemInventario
import com.example.prueba1integrador.model.Juego
import com.example.prueba1integrador.R
import com.google.firebase.database.FirebaseDatabase

class GestionarAdapter(
    private var listaInventario: List<ItemInventario>,
    private val onEditClick: (Juego) -> Unit,
    private val onDataChanged: () -> Unit
) : RecyclerView.Adapter<GestionarAdapter.GestionViewHolder>() {

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

        // Reseteo de estado
        holder.tvDetalle.visibility = View.GONE
        holder.ivFlecha.rotation = 0f

        // Lógica de Stock Total y Color del Triángulo
        if (item.cantidad <= 0) {
            holder.tvCantidad.text = "AGOTADO (0)"
            holder.tvCantidad.setTextColor(Color.parseColor("#EF5350"))
            holder.ivFlecha.setColorFilter(Color.parseColor("#EF5350")) // Triángulo Rojo si está agotado
        } else {
            holder.tvCantidad.text = "Stock Total: ${item.cantidad}"
            holder.tvCantidad.setTextColor(Color.parseColor("#4CAF50"))
            holder.ivFlecha.setColorFilter(Color.parseColor("#4CAF50")) // Triángulo Verde si hay stock
        }

        Glide.with(holder.itemView.context)
            .load(juego.imagenUrl)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(holder.ivImagen)

        holder.itemView.setOnClickListener {
            if (holder.tvDetalle.visibility == View.GONE) {
                holder.ivFlecha.animate().rotation(90f).setDuration(200).start()

                // Construimos el texto con colores dinámicos por línea
                val builder = SpannableStringBuilder()

                appendPlataforma(builder, "PlayStation", item.ps)
                builder.append("\n")
                appendPlataforma(builder, "Xbox", item.xb)
                builder.append("\n")
                appendPlataforma(builder, "Nintendo", item.ni)
                builder.append("\n")
                appendPlataforma(builder, "PC", item.pc)

                holder.tvDetalle.text = builder
                holder.tvDetalle.visibility = View.VISIBLE
            } else {
                holder.ivFlecha.animate().rotation(0f).setDuration(200).start()
                holder.tvDetalle.visibility = View.GONE
            }
        }

        holder.btnEditar.setOnClickListener { mostrarDialogoAnadirStock(item, holder.itemView.context) }
        holder.btnEliminar.setOnClickListener { mostrarDialogoEliminarPro(item, holder.itemView.context) }
    }

    // Función auxiliar para pintar cada línea de plataforma según su stock
    private fun appendPlataforma(builder: SpannableStringBuilder, nombre: String, cant: Int) {
        val inicio = builder.length
        val textoLinea = "$nombre: $cant"
        builder.append(textoLinea)

        val color = if (cant <= 0) Color.parseColor("#EF5350") else Color.parseColor("#4CAF50")

        builder.setSpan(
            ForegroundColorSpan(color),
            inicio,
            builder.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
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

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnConfirmar.setOnClickListener {
            val psNuevos = etPS.text.toString().toIntOrNull() ?: 0
            val xbNuevos = etXbox.text.toString().toIntOrNull() ?: 0
            val niNuevos = etNintendo.text.toString().toIntOrNull() ?: 0
            val pcNuevos = etPC.text.toString().toIntOrNull() ?: 0

            val totalNuevasUnidades = psNuevos + xbNuevos + niNuevos + pcNuevos

            if (totalNuevasUnidades > 0) {
                val nuevoDetalle = mapOf(
                    "playstation" to (item.ps + psNuevos),
                    "xbox" to (item.xb + xbNuevos),
                    "nintendo" to (item.ni + niNuevos),
                    "pc" to (item.pc + pcNuevos)
                )

                val nuevoStockTotal = item.cantidad + totalNuevasUnidades
                actualizarFirebaseConDetalle(item.juego.id, nuevoStockTotal, nuevoDetalle)
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Ingresa cantidades a sumar", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun actualizarFirebaseConDetalle(id: String, total: Int, detalle: Map<String, Int>) {
        val dbRef = FirebaseDatabase.getInstance().getReference("productos").child(id)
        val updates = hashMapOf<String, Any>("stock" to total, "detalle_stock" to detalle)

        dbRef.updateChildren(updates).addOnSuccessListener {
            onDataChanged()
        }.addOnFailureListener {
            Toast.makeText(null, "Error al guardar detalle", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDialogoEliminarPro(item: ItemInventario, context: Context) {
        val juego = item.juego
        val stockActual = item.cantidad
        val builder = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialogo_eliminar_producto, null)
        builder.setView(view)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etCantidad = view.findViewById<EditText>(R.id.etCantidadEliminar)
        val btnRetirarLote = view.findViewById<Button>(R.id.btnRetirarCantidad)
        val btnRetirarUno = view.findViewById<Button>(R.id.btnRetirarUno)
        val btnEliminarTodo = view.findViewById<Button>(R.id.btnEliminarTodo)

        btnRetirarUno.setOnClickListener {
            ejecutarActualizacion(juego, (stockActual - 1).coerceAtLeast(0), "redujo stock (-1)")
            dialog.dismiss()
        }

        btnRetirarLote.setOnClickListener {
            val cant = etCantidad.text.toString().toIntOrNull() ?: 0
            if (cant in 1..stockActual) {
                ejecutarActualizacion(juego, stockActual - cant, "redujo stock (-$cant)")
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Cantidad no válida", Toast.LENGTH_SHORT).show()
            }
        }

        btnEliminarTodo.setOnClickListener {
            val dbRef = FirebaseDatabase.getInstance().getReference("productos")
            item.idsAgrupados.forEach { dbRef.child(it).child("stock").setValue(0) }
            FirebaseInventoryManager().registrarEnHistorial("Admin", "borró el producto", juego.nombre, 0)
            onDataChanged()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun ejecutarActualizacion(juego: Juego, nuevaCant: Int, msgLog: String) {
        FirebaseDatabase.getInstance().getReference("productos").child(juego.id)
            .child("stock").setValue(nuevaCant).addOnSuccessListener {
                FirebaseInventoryManager().registrarEnHistorial("Admin", msgLog, juego.nombre, nuevaCant)
                onDataChanged()
            }
    }

    fun actualizarLista(nuevaLista: List<ItemInventario>) {
        this.listaInventario = nuevaLista
        notifyDataSetChanged()
    }
}