package com.example.prueba1integrador

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
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

        if (item.cantidad <= 0) {
            holder.tvCantidad.text = "AGOTADO (0)"
            holder.tvCantidad.setTextColor(Color.parseColor("#EF5350"))
        } else {
            holder.tvCantidad.text = "Stock: ${item.cantidad}"
            holder.tvCantidad.setTextColor(Color.parseColor("#4CAF50"))
        }

        Glide.with(holder.itemView.context)
            .load(juego.imagenUrl)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(holder.ivImagen)

        holder.btnEditar.setOnClickListener { mostrarDialogoAnadirStock(item, holder.itemView.context) }
        holder.btnEliminar.setOnClickListener { mostrarDialogoEliminarPro(item, holder.itemView.context) }
    }

    override fun getItemCount(): Int = listaInventario.size

    private fun mostrarDialogoAnadirStock(item: ItemInventario, context: Context) {
        val juego = item.juego
        val stockActual = item.cantidad
        val builder = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialogo_anadir_stock, null)
        builder.setView(view)

        val tvTitulo = view.findViewById<TextView>(R.id.tvTituloAnadir)
        val tvStockActual = view.findViewById<TextView>(R.id.tvStockActual)
        val etCantidad = view.findViewById<EditText>(R.id.etCantidadAnadir)
        val btnConfirmar = view.findViewById<Button>(R.id.btnConfirmarSuma)

        tvTitulo.text = "REPOSICIÓN: ${juego.nombre.uppercase()}"
        tvStockActual.text = "Stock actual: $stockActual"

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnConfirmar.setOnClickListener {
            val cantStr = etCantidad.text.toString()
            if (cantStr.isNotEmpty()) {
                val cantASumar = cantStr.toInt()
                if (cantASumar > 0) {
                    ejecutarActualizacion(juego, stockActual + cantASumar, "Actualizó stock (+$cantASumar)")
                    dialog.dismiss()
                } else {
                    Toast.makeText(context, "Ingresa una cantidad válida", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
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

            // Forzamos el mensaje de agotado enviando un 0
            FirebaseInventoryManager().registrarEnHistorial("Admin", "borró el producto", juego.nombre, 0)
            onDataChanged()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun ejecutarActualizacion(juego: Juego, nuevaCant: Int, msgLog: String) {
        FirebaseDatabase.getInstance().getReference("productos").child(juego.id)
            .child("stock").setValue(nuevaCant).addOnSuccessListener {
                // Pasamos nuevaCant para que el Manager detecte si es 0
                FirebaseInventoryManager().registrarEnHistorial("Admin", msgLog, juego.nombre, nuevaCant)
                onDataChanged()
            }
    }

    fun actualizarLista(nuevaLista: List<ItemInventario>) {
        this.listaInventario = nuevaLista
        notifyDataSetChanged()
    }
}