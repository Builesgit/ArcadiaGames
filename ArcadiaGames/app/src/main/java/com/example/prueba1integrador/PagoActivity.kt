package com.example.prueba1integrador

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class PagoActivity : BaseActivity() {

    private lateinit var listaProductos: ArrayList<Juego>
    private lateinit var compraId: String
    private var fechaActualMillis: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pago)

        val total = intent.getDoubleExtra("PRECIO_TOTAL", 0.0)

        listaProductos =
            intent.getSerializableExtra("LISTA_PRODUCTOS") as? ArrayList<Juego>
                ?: arrayListOf()

        findViewById<TextView>(R.id.tvTotal).text =
            "TOTAL: €" + String.format("%.2f", total)

        // ===============================
        // GENERAR PEDIDO REAL ANTES DE PAGAR
        // ===============================
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid

        if (uid != null) {
            val db = FirebaseDatabase.getInstance().reference
            compraId = db.child("compras").child(uid).push().key ?: UUID.randomUUID().toString()
        } else {
            compraId = UUID.randomUUID().toString()
        }

        fechaActualMillis = System.currentTimeMillis()
        val fechaFormateada = SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            Locale.getDefault()
        ).format(Date(fechaActualMillis))

        findViewById<TextView>(R.id.tvImporteOperacion).text =
            String.format("%.2f €", total)

        findViewById<TextView>(R.id.tvPedidoOperacion).text =
            "Pedido: $compraId"

        findViewById<TextView>(R.id.tvFechaOperacion).text =
            "Fecha: $fechaFormateada"

        val etCardNumber = findViewById<EditText>(R.id.etCardNumber)
        val etExpiry = findViewById<EditText>(R.id.etExpiration)

        // ===============================
        // AUTOFORMATO TARJETA
        // ===============================
        etCardNumber.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s == null) return
                isFormatting = true

                val digitsOnly = s.toString().replace(" ", "")
                val trimmed = if (digitsOnly.length > 16) {
                    digitsOnly.substring(0, 16)
                } else {
                    digitsOnly
                }

                val formatted = StringBuilder()
                for (i in trimmed.indices) {
                    formatted.append(trimmed[i])
                    if ((i + 1) % 4 == 0 && i != trimmed.lastIndex) {
                        formatted.append(" ")
                    }
                }

                etCardNumber.setText(formatted.toString())
                etCardNumber.setSelection(formatted.length)

                isFormatting = false
            }
        })

        // ===============================
        // AUTOFORMATO FECHA
        // ===============================
        etExpiry.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s == null) return
                isFormatting = true

                val digits = s.toString().replace("/", "")
                if (digits.length >= 2) {
                    val mes = digits.substring(0, 2)
                    val resto = digits.drop(2)
                    s.replace(0, s.length, if (resto.isNotEmpty()) "$mes/$resto" else mes)
                }

                isFormatting = false
            }
        })

        // ===============================
        // BOTÓN PAGAR
        // ===============================
        findViewById<Button>(R.id.btnCheckout).setOnClickListener {

            val name = findViewById<EditText>(R.id.etName).text.toString().trim()
            val cardNumberRaw = etCardNumber.text.toString().replace(" ", "")
            val expiry = etExpiry.text.toString().trim()
            val cvv = findViewById<EditText>(R.id.etCvv).text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Nombre obligatorio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cardNumberRaw.length != 16 || !cardNumberRaw.all { it.isDigit() }) {
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

            procesarFinalizacionPago(name, total)
        }
    }

    private fun validarFecha(fecha: String): Boolean {
        return Regex("^(0[1-9]|1[0-2])/[0-9]{2}$").matches(fecha)
    }

    private fun procesarFinalizacionPago(cliente: String, totalCompra: Double) {

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_LONG).show()
            return
        }

        val uid = user.uid
        val db = FirebaseDatabase.getInstance().reference

        val compraMap = hashMapOf(
            "cliente" to cliente,
            "fecha" to fechaActualMillis,
            "total" to totalCompra
        )

        db.child("compras")
            .child(uid)
            .child(compraId)
            .setValue(compraMap)
            .addOnCompleteListener { task ->

                if (!task.isSuccessful) {
                    Toast.makeText(this, "Error guardando compra", Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }

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

                    val nuevoStock = (juego.stock - 1).coerceAtLeast(0)
                    db.child("productos")
                        .child(juego.id)
                        .child("stock")
                        .setValue(nuevoStock)
                }

                db.child("cesta")
                    .child(uid)
                    .removeValue()

                Toast.makeText(this, "¡Compra realizada correctamente!", Toast.LENGTH_LONG).show()

                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
    }
}
