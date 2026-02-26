package com.example.prueba1integrador.activity

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
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

    private var nombreClienteTemp = ""
    private var esEntregaFisica = true
    private var totalCompraDouble = 0.0

    private val guardarFacturaLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        uri?.let { crearPDF_Factura(it) }
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pago)

        totalCompraDouble = intent.getDoubleExtra("PRECIO_TOTAL", 0.0)
        listaProductos = intent.getSerializableExtra("LISTA_PRODUCTOS") as? ArrayList<Juego> ?: arrayListOf()

        fechaActualMillis = System.currentTimeMillis()
        compraId = FirebaseDatabase.getInstance().reference.push().key ?: UUID.randomUUID().toString().substring(0, 8)

        vincularInterfaz()
    }

    private fun vincularInterfaz() {
        val tvImporte = findViewById<TextView>(R.id.tvImporteOperacion)
        val tvPedido = findViewById<TextView>(R.id.tvPedidoOperacion)
        val tvFecha = findViewById<TextView>(R.id.tvFechaOperacion)
        val tvTotalPie = findViewById<TextView>(R.id.tvTotal)
        val etTarjeta = findViewById<EditText>(R.id.etCardNumber)
        val etExp = findViewById<EditText>(R.id.etExpiration)

        tvImporte.text = String.format("%.2f €", totalCompraDouble)
        tvTotalPie.text = "TOTAL: €${String.format("%.2f", totalCompraDouble)}"
        tvPedido.text = "Pedido: $compraId"
        tvFecha.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(fechaActualMillis))

        // 1. Formateador de tarjeta (espacios cada 4 números)
        etTarjeta.addTextChangedListener(object : TextWatcher {
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
                etTarjeta.setText(formatted.toString())
                etTarjeta.setSelection(formatted.length)
                isFormatting = false
            }
        })

        // 2. Formateador de fecha (añade / automáticamente)
        etExp.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s == null) return
                isFormatting = true
                val input = s.toString().replace("/", "")
                if (input.length >= 2) {
                    val mm = input.substring(0, 2)
                    val yy = if (input.length > 2) input.substring(2) else ""
                    val res = if (yy.isNotEmpty()) "$mm/$yy" else "$mm/"
                    etExp.setText(res)
                    etExp.setSelection(res.length)
                }
                isFormatting = false
            }
        })

        findViewById<Button>(R.id.btnCheckout).setOnClickListener {
            if (validarCamposAntiguo()) {
                val rgEntrega = findViewById<RadioGroup>(R.id.rgTipoEntrega)
                esEntregaFisica = rgEntrega.checkedRadioButtonId == R.id.rbFisico
                nombreClienteTemp = findViewById<EditText>(R.id.etName).text.toString().trim()
                procesarFinalizacionPago(nombreClienteTemp, totalCompraDouble)
            }
        }
    }

    private fun validarCamposAntiguo(): Boolean {
        val etTarjeta = findViewById<EditText>(R.id.etCardNumber)
        val etNombre = findViewById<EditText>(R.id.etName)
        val etExp = findViewById<EditText>(R.id.etExpiration)
        val etCvv = findViewById<EditText>(R.id.etCvv)

        val tarjeta = etTarjeta.text.toString().replace(" ", "")
        val nombre = etNombre.text.toString().trim()
        val exp = etExp.text.toString().trim()
        val cvv = etCvv.text.toString().trim()

        if (nombre.isEmpty() || tarjeta.isEmpty() || exp.isEmpty() || cvv.isEmpty()) {
            Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
            return false
        }
        if (tarjeta.length < 16) {
            Toast.makeText(this, "Número de tarjeta no válido", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!exp.contains("/")) {
            Toast.makeText(this, "Formato de fecha incorrecto (MM/YY)", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun procesarFinalizacionPago(cliente: String, totalCompra: Double) {

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_LONG).show()
            return
        }

        val uid = user.uid
        val db = FirebaseDatabase.getInstance().reference

        // Añadimos la modalidad al mapa para que el PDF sepa si imprimir el código o el vale de recogida
        val compraMap = hashMapOf(
            "cliente" to cliente,
            "fecha" to fechaActualMillis,
            "total" to totalCompra,
            "modalidad" to if (esEntregaFisica) "FÍSICO" else "DIGITAL"
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
                    // 1. Guardar el juego en el historial de compras del usuario
                    val juegoMap = hashMapOf(
                        "id" to juego.id,
                        "nombre" to juego.nombre,
                        "plataforma" to juego.plataforma,
                        "precio" to juego.precio,
                        "imagenUrl" to juego.imagenUrl,
                        "categoria" to juego.categoria,
                        "codigoDigital" to if (!esEntregaFisica) generarCodigoDigital() else ""
                    )

                    db.child("compras").child(uid).child(compraId).child("juegos").child(juego.id)
                        .setValue(juegoMap)

                    // 2. RESTRICCIÓN DE STOCK DETALLADO (Lógica corregida)
                    val productoRef = db.child("productos").child(juego.id)

                    productoRef.get().addOnSuccessListener { snapshot ->
                        val stockActualTotal =
                            snapshot.child("stock").getValue(Int::class.java) ?: 0

                        val plataformaKey = juego.plataforma.lowercase().trim()

                        val stockPlataformaActual =
                            snapshot.child("detalle_stock").child(plataformaKey)
                                .getValue(Int::class.java) ?: 0

                        val updates = hashMapOf<String, Any>(
                            "stock" to (stockActualTotal - 1).coerceAtLeast(0),
                            "detalle_stock/$plataformaKey" to (stockPlataformaActual - 1).coerceAtLeast(0)
                        )

                        productoRef.updateChildren(updates)

                        val invManager = com.example.prueba1integrador.manager.FirebaseInventoryManager()
                        invManager.registrarEnHistorial(cliente, "compró", juego.nombre, 1)
                    }
                }

                // 3. Limpiar la cesta del usuario tras la compra
                db.child("cesta").child(uid).removeValue()

                Toast.makeText(this, "Generando factura...", Toast.LENGTH_SHORT).show()

                // 4. LANZAR FACTURA (Sustituye al Intent directo)
                // El launcher ya se encarga de ir al HomeActivity al terminar el PDF
                guardarFacturaLauncher.launch("Factura_Arcadia_${System.currentTimeMillis()}.pdf")
            }
    }

    private fun restarStockReal(juego: Juego) {
        val dbRef = FirebaseDatabase.getInstance().getReference("productos").child(juego.id)
        dbRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val detalleStock = snapshot.child("detalle_stock").value as? Map<String, Long> ?: emptyMap()
                val nuevoMapa = detalleStock.toMutableMap()
                val platKey = juego.plataforma.lowercase().trim()

                val actualPlat = nuevoMapa[platKey] ?: 0L
                if (actualPlat > 0) {
                    nuevoMapa[platKey] = actualPlat - 1
                    val nuevoTotal = nuevoMapa.values.sum()
                    dbRef.updateChildren(mapOf("detalle_stock" to nuevoMapa, "stock" to nuevoTotal))
                }
            }
        }
    }

    private fun generarCodigoDigital() = (1..3).joinToString("-") {
        (1..5).map { "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".random() }.joinToString("")
    }


    private fun crearPDF_Factura(uri: Uri) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        paint.isFakeBoldText = true
        paint.textSize = 26f
        paint.color = Color.parseColor("#FFC107")
        canvas.drawText("ARCADIA GAMES - FACTURA", 50f, 60f, paint)

        paint.isFakeBoldText = false
        paint.textSize = 12f
        paint.color = Color.BLACK
        canvas.drawText("Cliente: $nombreClienteTemp", 50f, 90f, paint)
        canvas.drawText("ID Pedido: $compraId", 50f, 105f, paint)
        canvas.drawLine(50f, 120f, 545f, 120f, paint)

        var y = 160f
        listaProductos.forEach { juego ->
            paint.isFakeBoldText = true
            canvas.drawText(juego.nombre.uppercase(), 50f, y, paint)
            canvas.drawText(juego.precio, 450f, y, paint)
            y += 20f
            if (!esEntregaFisica) {
                paint.color = Color.parseColor("#4CAF50")
                canvas.drawText("CÓDIGO DIGITAL: ${generarCodigoDigital()}", 60f, y, paint)
                paint.color = Color.BLACK
                y += 20f
            }
            y += 15f
        }

        if (esEntregaFisica) {
            paint.style = Paint.Style.STROKE
            canvas.drawRect(50f, y + 20f, 545f, y + 100f, paint)
            paint.style = Paint.Style.FILL
            canvas.drawText("VALE DE RECOGIDA EN TIENDA", 70f, y + 55f, paint.apply { isFakeBoldText = true })
            canvas.drawText("Presente este vale y su DNI para la entrega física.", 70f, y + 75f, paint.apply { isFakeBoldText = false })
        }

        pdfDocument.finishPage(page)
        contentResolver.openOutputStream(uri)?.use { pdfDocument.writeTo(it) }
        pdfDocument.close()
    }
}