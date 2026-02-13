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

        listaProductos.forEach { juego ->
            val nuevoStock = (juego.stock - 1).coerceAtLeast(0)
            db.getReference("productos").child(juego.id).child("stock").setValue(nuevoStock)

            inventoryManager.registrarEnHistorial(
                nombreUser = cliente,
                accion = "compró",
                producto = juego.nombre,
                cant = 1
            )
        }

        // Una vez vaciada la cesta, redirigimos a HomeActivity
        db.getReference("cesta").child(uid).removeValue().addOnSuccessListener {
            Toast.makeText(this, "¡Compra finalizada con éxito!", Toast.LENGTH_LONG).show()

            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)

            finish() // Cerramos PagoActivity
        }
    }
}