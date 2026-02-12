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

        // Lógica visual: Verde si hay stock, Rojo si está agotado
        if (item.cantidad <= 0) {
            holder.tvCantidad.text = "AGOTADO (0)"
            holder.tvCantidad.setTextColor(Color.parseColor("#EF5350")) // Rojo
        } else {
            holder.tvCantidad.text = "Stock: ${item.cantidad}"
            holder.tvCantidad.setTextColor(Color.parseColor("#4CAF50")) // Verde
        }

        Glide.with(holder.itemView.context)
            .load(juego.imagenUrl)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(holder.ivImagen)

        holder.btnEditar.setOnClickListener { onEditClick(juego) }

        holder.btnEliminar.setOnClickListener {
            mostrarDialogoEliminarPro(item, holder.itemView.context)
        }
    }

    override fun getItemCount(): Int = listaInventario.size

    fun actualizarLista(nuevaLista: List<ItemInventario>) {
        this.listaInventario = nuevaLista
        notifyDataSetChanged()
    }

    private fun mostrarDialogoEliminarPro(item: ItemInventario, context: Context) {
        val juego = item.juego
        val stockActual = item.cantidad

        val builder = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialogo_eliminar_producto, null)
        builder.setView(view)

        val tvTitulo = view.findViewById<TextView>(R.id.tvTituloEliminar)
        tvTitulo.text = "GESTIONAR: ${juego.nombre.uppercase()}"

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etCantidad = view.findViewById<EditText>(R.id.etCantidadEliminar)
        val btnRetirarLote = view.findViewById<Button>(R.id.btnRetirarCantidad)
        val btnRetirarUno = view.findViewById<Button>(R.id.btnRetirarUno)
        val btnEliminarTodo = view.findViewById<Button>(R.id.btnEliminarTodo)

        // BAJA UNITARIA (-1)
        btnRetirarUno.setOnClickListener {
            val nuevaCant = (stockActual - 1).coerceAtLeast(0)
            ejecutarActualizacion(juego, nuevaCant, "redujo stock (-1)")
            dialog.dismiss()
        }

        // BAJA POR LOTE
        btnRetirarLote.setOnClickListener {
            val cantStr = etCantidad.text.toString()
            if (cantStr.isNotEmpty()) {
                val cantARetirar = cantStr.toInt()
                if (cantARetirar > stockActual) {
                    Toast.makeText(context, "No hay suficiente stock", Toast.LENGTH_SHORT).show()
                } else {
                    val nuevaCant = stockActual - cantARetirar
                    ejecutarActualizacion(juego, nuevaCant, "redujo stock (-$cantARetirar)")
                    dialog.dismiss()
                }
            }
        }

        // ELIMINAR TODO EL GRUPO (Ahora solo pone stock a 0)
        btnEliminarTodo.setOnClickListener {
            val dbRef = FirebaseDatabase.getInstance().getReference("productos")
            // No usamos removeValue(), actualizamos el campo stock a 0 para todos los IDs
            item.idsAgrupados.forEach { id ->
                dbRef.child(id).child("stock").setValue(0)
            }
            FirebaseInventoryManager().registrarEnHistorial(
                nombreUser = "Admin",
                accion = "vació stock (Borrado lógico)",
                producto = juego.nombre,
                cant = 0
            )
            Toast.makeText(context, "Producto marcado como agotado", Toast.LENGTH_SHORT).show()
            onDataChanged()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun ejecutarActualizacion(juego: Juego, nuevaCant: Int, msgLog: String) {
        val ref = FirebaseDatabase.getInstance().getReference("productos").child(juego.id)

        // Siempre usamos setValue para mantener el registro vivo en la DB
        ref.child("stock").setValue(nuevaCant).addOnSuccessListener {
            FirebaseInventoryManager().registrarEnHistorial("Admin", msgLog, juego.nombre, nuevaCant)
            onDataChanged()
        }
    }
}