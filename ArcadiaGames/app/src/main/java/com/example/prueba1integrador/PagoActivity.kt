package com.example.prueba1integrador

import android.content.Intent
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
        val serializable = intent.getSerializableExtra("LISTA_PRODUCTOS")
        listaProductos = if (serializable is List<*>) serializable as List<Juego> else emptyList()

        findViewById<TextView>(R.id.tvTotal).text = "TOTAL: €" + String.format("%.2f", total)

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

        // Bucle tradicional para actualizar stock
        for (i in 0 until listaProductos.size) {
            val juego = listaProductos[i]
            var nuevoStock = juego.stock - 1
            if (nuevoStock < 0) nuevoStock = 0

            db.getReference("productos").child(juego.id).child("stock").setValue(nuevoStock)

            inventoryManager.registrarEnHistorial(cliente, "compró", juego.nombre, nuevoStock)

            if (nuevoStock == 0) {
                inventoryManager.registrarEnHistorial("Sistema", "Agotado el stock", juego.nombre, 0)
            }
        }

        db.getReference("cesta").child(uid).removeValue().addOnSuccessListener {
            Toast.makeText(this, "¡Compra finalizada!", Toast.LENGTH_LONG).show()
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }
}