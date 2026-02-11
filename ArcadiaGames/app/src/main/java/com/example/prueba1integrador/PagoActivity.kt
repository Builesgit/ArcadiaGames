package com.example.prueba1integrador

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class PagoActivity : AppCompatActivity() {

    private lateinit var listaProductos: List<Juego>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pago)

        val total = intent.getDoubleExtra("PRECIO_TOTAL", 0.0)
        // RECUPERAR LISTA: Obtenemos los juegos enviados desde CestaActivity
        listaProductos = intent.getSerializableExtra("LISTA_PRODUCTOS") as? List<Juego> ?: emptyList()

        findViewById<TextView>(R.id.tvTotal).text = "TOTAL: €%.2f".format(total)

        findViewById<Button>(R.id.btnCheckout).setOnClickListener {
            val name = findViewById<EditText>(R.id.etName).text.toString().trim()
            if (name.isNotEmpty()) {
                procesarFinalizacionPago(name)
            } else {
                Toast.makeText(this, "Nombre obligatorio", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun procesarFinalizacionPago(cliente: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance()
        val inventoryManager = FirebaseInventoryManager()

        // 1. ITERAR: Recorremos los juegos para restar stock y registrar historial
        listaProductos.forEach { juego ->
            // Restamos 1 al stock (evitando negativos con coerceAtLeast)
            val nuevoStock = (juego.stock - 1).coerceAtLeast(0)

            // Actualizamos en el nodo global 'productos'
            db.getReference("productos").child(juego.id).child("stock").setValue(nuevoStock)

            // Registramos la acción en el historial con el nombre del cliente
            inventoryManager.registrarEnHistorial(
                nombreUser = cliente,
                accion = "compró",
                producto = juego.nombre,
                cant = 1
            )
        }

        // 2. LIMPIAR CESTA: Una vez pagado, vaciamos el carrito del usuario
        db.getReference("cesta").child(uid).removeValue()

        Toast.makeText(this, "¡Compra finalizada con éxito! Inventario actualizado.", Toast.LENGTH_LONG).show()

        // Volver a la pantalla principal o cerrar flujo
        finish()
    }
}