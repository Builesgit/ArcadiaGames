package com.example.prueba1integrador

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class PagoActivity : AppCompatActivity() {

    private lateinit var listaProductos: ArrayList<Juego>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pago)

        val total = intent.getDoubleExtra("PRECIO_TOTAL", 0.0)

        listaProductos =
            intent.getSerializableExtra("LISTA_PRODUCTOS") as? ArrayList<Juego>
                ?: arrayListOf()

        findViewById<TextView>(R.id.tvTotal).text =
            "TOTAL: €" + String.format("%.2f", total)

        // ✅ AUTOFORMATO FECHA: MM/YY (añade "/" al escribir 2 dígitos)
        val etExpiry = findViewById<EditText>(R.id.etExpiration)
        etExpiry.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s == null) return

                // Si llega a 2 caracteres y no tiene "/", lo añade
                if (s.length == 2 && !s.contains("/")) {
                    s.append("/")
                }

                // Si el usuario borra y queda "MM/", que permita borrar "/" fácil
                if (s.length == 3 && s[2] == '/' && s.substring(0, 2).any { !it.isDigit() }) {
                    s.clear()
                }
            }
        })

        findViewById<Button>(R.id.btnCheckout).setOnClickListener {

            val name = findViewById<EditText>(R.id.etName).text.toString().trim()
            val cardNumber = findViewById<EditText>(R.id.etCardNumber).text.toString().trim()
            val expiry = findViewById<EditText>(R.id.etExpiration).text.toString().trim()
            val cvv = findViewById<EditText>(R.id.etCvv).text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Nombre obligatorio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cardNumber.length != 16 || !cardNumber.all { it.isDigit() }) {
                Toast.makeText(this, "La tarjeta debe tener 16 números", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (!validarFecha(expiry)) {
                Toast.makeText(this, "Fecha inválida (formato MM/YY)", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (cvv.length != 3 || !cvv.all { it.isDigit() }) {
                Toast.makeText(this, "CVV inválido (3 números)", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (listaProductos.isEmpty()) {
                Toast.makeText(this, "No hay productos en la compra", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            procesarFinalizacionPago(name)
        }
    }

    private fun validarFecha(fecha: String): Boolean {
        if (!Regex("^(0[1-9]|1[0-2])/[0-9]{2}$").matches(fecha)) {
            return false
        }
        val partes = fecha.split("/")
        val mes = partes[0].toInt()
        if (mes !in 1..12) return false
        return true
    }

    private fun procesarFinalizacionPago(cliente: String) {

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_LONG).show()
            return
        }

        val uid = user.uid
        val db = FirebaseDatabase.getInstance().reference
        val totalCompra = intent.getDoubleExtra("PRECIO_TOTAL", 0.0)

        val compraId = db.child("compras").child(uid).push().key
        if (compraId == null) {
            Toast.makeText(this, "Error generando compra", Toast.LENGTH_LONG).show()
            return
        }

        val compraMap = hashMapOf(
            "cliente" to cliente,
            "fecha" to System.currentTimeMillis(),
            "total" to totalCompra
        )

        // 1️⃣ Guardar la compra
        db.child("compras")
            .child(uid)
            .child(compraId)
            .setValue(compraMap)
            .addOnCompleteListener { task ->

                if (!task.isSuccessful) {
                    Toast.makeText(this, "Error guardando compra", Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }

                // 2️⃣ Guardar juegos dentro de la compra
                for (juego in listaProductos) {

                    val juegoMap = hashMapOf(
                        "id" to juego.id,
                        "nombre" to juego.nombre,
                        "plataforma" to juego.plataforma,
                        "precio" to juego.precio,
                        "imagenUrl" to juego.imagenUrl,
                        "imagenResId" to juego.imagenResId,
                        "descripcion" to juego.descripcion,
                        "categoria" to juego.categoria,
                        "stock" to juego.stock,
                        "tags" to juego.tags
                    )



                    db.child("compras")
                        .child(uid)
                        .child(compraId)
                        .child("juegos")
                        .child(juego.id)
                        .setValue(juegoMap)

                    // 3️⃣ Actualizar stock
                    val nuevoStock = (juego.stock - 1).coerceAtLeast(0)
                    db.child("productos")
                        .child(juego.id)
                        .child("stock")
                        .setValue(nuevoStock)
                }

                // 4️⃣ Vaciar cesta
                db.child("cesta")
                    .child(uid)
                    .removeValue()

                // 5️⃣ Confirmación
                Toast.makeText(this, "¡Compra realizada correctamente!", Toast.LENGTH_LONG).show()

                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
    }


    private fun finalizarCompra(uid: String) {

        FirebaseDatabase.getInstance()
            .getReference("cesta")
            .child(uid)
            .removeValue()
            .addOnSuccessListener {

                Toast.makeText(this, "¡Compra finalizada!", Toast.LENGTH_LONG).show()

                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
    }
}
