package com.example.prueba1integrador

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class PagoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pago)

        val total = intent.getDoubleExtra("PRECIO_TOTAL", 0.0)
        findViewById<TextView>(R.id.tvTotal).text = "TOTAL: €%.2f".format(total)

        findViewById<Button>(R.id.btnCheckout).setOnClickListener {
            val name = findViewById<EditText>(R.id.etName).text.toString()
            if (name.isNotEmpty()) {
                procesarFinalizacionPago(name)
            } else {
                Toast.makeText(this, "Nombre obligatorio", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun procesarFinalizacionPago(cliente: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // 1. Limpiar Cesta
        FirebaseDatabase.getInstance().getReference("cesta").child(uid).removeValue()

        // 2. Log de historial
        val log = AccionHistorial(
            usuarioNombre = cliente,
            accion = "compró",
            productoNombre = "Pedido Cesta",
            fecha = System.currentTimeMillis()
        )
        FirebaseDatabase.getInstance().getReference("historial").push().setValue(log)

        Toast.makeText(this, "¡Pago realizado con éxito!", Toast.LENGTH_LONG).show()
        finish()
    }
}