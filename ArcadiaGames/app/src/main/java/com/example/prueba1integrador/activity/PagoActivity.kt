package com.example.prueba1integrador.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import com.example.prueba1integrador.manager.FirebaseInventoryManager
import com.example.prueba1integrador.model.Juego
import com.example.prueba1integrador.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class PagoActivity : BaseActivity() {

    private lateinit var listaProductos: ArrayList<Juego>
    private lateinit var compraId: String
    private var fechaActualMillis: Long = 0L
    private val inventoryManager = FirebaseInventoryManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pago)

        // 1. Recuperar datos del Intent
        val total = intent.getDoubleExtra("PRECIO_TOTAL", 0.0)
        listaProductos = intent.getSerializableExtra("LISTA_PRODUCTOS") as? ArrayList<Juego> ?: arrayListOf()

        // 2. Configurar Interfaz (Importe, Pedido, Fecha)
        fechaActualMillis = System.currentTimeMillis()
        compraId = FirebaseDatabase.getInstance().reference.push().key ?: UUID.randomUUID().toString().substring(0, 8)

        val fechaFormateada = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(fechaActualMillis))

        findViewById<TextView>(R.id.tvTotal).text = "TOTAL: €${String.format("%.2f", total)}"
        findViewById<TextView>(R.id.tvImporteOperacion).text = String.format("%.2f €", total)
        findViewById<TextView>(R.id.tvPedidoOperacion).text = "Pedido: $compraId"
        findViewById<TextView>(R.id.tvFechaOperacion).text = "Fecha: $fechaFormateada"

        val etCardNumber = findViewById<EditText>(R.id.etCardNumber)
        val etExpiry = findViewById<EditText>(R.id.etExpiration)
        val etCvv = findViewById<EditText>(R.id.etCvv)
        val etName = findViewById<EditText>(R.id.etName)

        // 3. Autoformato Tarjeta (Espacios cada 4 dígitos)
        etCardNumber.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s == null) return
                isFormatting = true
                val digits = s.toString().replace(" ", "")
                val formatted = StringBuilder()
                for (i in digits.indices) {
                    if (i > 0 && i % 4 == 0) formatted.append(" ")
                    formatted.append(digits[i])
                }
                etCardNumber.setText(formatted.toString())
                etCardNumber.setSelection(formatted.length)
                isFormatting = false
            }
        })

        // 4. Autoformato Fecha (Añadir /)
        etExpiry.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s == null) return
                isFormatting = true
                val input = s.toString().replace("/", "")
                if (input.length >= 2) {
                    val mm = input.substring(0, 2)
                    val yy = input.substring(2)
                    val res = if (yy.isNotEmpty()) "$mm/$yy" else mm
                    etExpiry.setText(res)
                    etExpiry.setSelection(res.length)
                }
                isFormatting = false
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 5. Botón Pagar con Validaciones
        findViewById<Button>(R.id.btnCheckout).setOnClickListener {
            val name = etName.text.toString().trim()
            val card = etCardNumber.text.toString().replace(" ", "")
            val exp = etExpiry.text.toString().trim()
            val cvv = etCvv.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Introduce el nombre del titular", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (card.length != 16) {
                Toast.makeText(this, "Tarjeta inválida (16 dígitos)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!Regex("^(0[1-9]|1[0-2])/[0-9]{2}$").matches(exp)) {
                Toast.makeText(this, "Fecha inválida (MM/YY)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (cvv.length != 3) {
                Toast.makeText(this, "CVV inválido (3 dígitos)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            procesarFinalizacionPago(name, total)
        }
    }

    private fun procesarFinalizacionPago(cliente: String, totalCompra: Double) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid
        val db = FirebaseDatabase.getInstance().reference

        val compraMap = hashMapOf(
            "cliente" to cliente,
            "fecha" to fechaActualMillis,
            "total" to totalCompra
        )

        db.child("compras").child(uid).child(compraId).setValue(compraMap)
            .addOnSuccessListener {
                for (juego in listaProductos) {
                    // Guardar juego en la compra
                    db.child("compras").child(uid).child(compraId).child("juegos").child(juego.id).setValue(juego)

                    // Restar Stock Detallado y General
                    val platKey = juego.plataforma.lowercase().trim()
                    val productoRef = db.child("productos").child(juego.id)

                    productoRef.get().addOnSuccessListener { snapshot ->
                        val stockTotalAnterior = snapshot.child("stock").getValue(Int::class.java) ?: 0
                        val stockPlatAnterior = snapshot.child("detalle_stock").child(platKey).getValue(Int::class.java) ?: 0

                        val updates = hashMapOf<String, Any>(
                            "stock" to (stockTotalAnterior - 1).coerceAtLeast(0),
                            "detalle_stock/$platKey" to (stockPlatAnterior - 1).coerceAtLeast(0)
                        )
                        productoRef.updateChildren(updates)

                        // Estadísticas y Historial
                        inventoryManager.registrarVentaMecanica(juego.id, 1)
                        inventoryManager.registrarEnHistorial(cliente, "compró", juego.nombre, 1)
                    }
                }

                db.child("cesta").child(uid).removeValue()
                Toast.makeText(this, "¡Compra realizada con éxito!", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                finish()
            }
    }
}