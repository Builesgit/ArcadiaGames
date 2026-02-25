package com.example.prueba1integrador

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class PagoActivity : BaseActivity() {

    private lateinit var listaProductos: ArrayList<Juego>
    private lateinit var compraId: String
    private val inventoryManager = FirebaseInventoryManager()
    private var nombreClienteTemp = ""
    private var esEntregaFisica = true

    private val guardarFacturaLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        uri?.let { crearPDF_Factura(it) }
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pago)

        // 1. RECOGER DATOS
        listaProductos = intent.getSerializableExtra("LISTA_PRODUCTOS") as? ArrayList<Juego> ?: arrayListOf()

        // 2. LÓGICA ANTI-CERO
        var totalRecibido = intent.getStringExtra("TOTAL_PAGO")
        if (totalRecibido == null || totalRecibido == "0.00 €") {
            val suma = listaProductos.sumOf {
                it.precio.replace("€", "").replace(",", ".").trim().toDoubleOrNull() ?: 0.0
            }
            totalRecibido = String.format("%.2f €", suma)
        }

        compraId = FirebaseDatabase.getInstance().reference.push().key ?: UUID.randomUUID().toString()

        // 3. VINCULAR VISTAS
        val tvImporte = findViewById<TextView>(R.id.tvImporteOperacion)
        val tvPedido = findViewById<TextView>(R.id.tvPedidoOperacion)
        val tvFecha = findViewById<TextView>(R.id.tvFechaOperacion)
        val tvTotalPie = findViewById<TextView>(R.id.tvTotal)
        val etTarjeta = findViewById<EditText>(R.id.etCardNumber)
        val etFechaExp = findViewById<EditText>(R.id.etExpiration)

        // 4. ASIGNAR VALORES
        tvImporte.text = totalRecibido
        tvTotalPie.text = "Total: $totalRecibido"
        tvPedido.text = listaProductos.joinToString(", ") { it.nombre }
        tvFecha.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        // 5. CONFIGURAR FILTROS Y FORMATEADORES
        etTarjeta.filters = arrayOf(InputFilter.LengthFilter(19))
        setupFormateadores(etTarjeta, etFechaExp)

        findViewById<Button>(R.id.btnCheckout).setOnClickListener {
            if (validarCampos()) {
                val rgEntrega = findViewById<RadioGroup>(R.id.rgTipoEntrega)
                esEntregaFisica = rgEntrega.checkedRadioButtonId == R.id.rbFisico
                nombreClienteTemp = findViewById<EditText>(R.id.etName).text.toString()
                procesarFinalizacionPago(nombreClienteTemp)
            }
        }
    }

    private fun setupFormateadores(etTarjeta: EditText, etFecha: EditText) {
        etTarjeta.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                isUpdating = true
                val original = s.toString().replace(" ", "")
                val formatted = StringBuilder()
                for (i in original.indices) {
                    formatted.append(original[i])
                    if ((i + 1) % 4 == 0 && (i + 1) < original.length) {
                        formatted.append(" ")
                    }
                }
                s?.replace(0, s.length, formatted.toString())
                isUpdating = false
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        etFecha.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 2 && !s.contains("/")) s.append("/")
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun validarCampos(): Boolean {
        val tarjeta = findViewById<EditText>(R.id.etCardNumber).text.toString().replace(" ", "")
        val nombre = findViewById<EditText>(R.id.etName).text.toString()
        if (nombre.isEmpty()) {
            Toast.makeText(this, "Introduce tu nombre", Toast.LENGTH_SHORT).show()
            return false
        }
        if (tarjeta.length != 16) {
            Toast.makeText(this, "Número de tarjeta incompleto", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun procesarFinalizacionPago(cliente: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().reference
        val infoCompra = hashMapOf("cliente" to cliente, "fecha" to System.currentTimeMillis(), "modalidad" to if (esEntregaFisica) "FÍSICO" else "DIGITAL")

        db.child("compras").child(uid).child(compraId).updateChildren(infoCompra as Map<String, Any>)
            .addOnSuccessListener {
                for (juego in listaProductos) {
                    val codigo = if (!esEntregaFisica) generarCodigoDigital() else ""
                    val itemVenta = hashMapOf(
                        "id" to juego.id,
                        "nombre" to juego.nombre,
                        "precio" to juego.precio,
                        "codigoDigital" to codigo,
                        "plataforma" to juego.plataforma // Importante para el historial
                    )
                    db.child("compras").child(uid).child(compraId).child("juegos").child(juego.id).setValue(itemVenta)

                    // LLAMADA MEJORADA AL STOCK
                    actualizarStockFirebase(juego)

                    inventoryManager.registrarVentaMecanica(juego.id, 1)
                    inventoryManager.registrarEnHistorial(cliente, "Compró ${juego.nombre}", juego.nombre, 1)
                }
                db.child("cesta").child(uid).removeValue()
                guardarFacturaLauncher.launch("Factura_Arcadia_${System.currentTimeMillis()}.pdf")
            }
    }

    private fun generarCodigoDigital() = (1..3).joinToString("-") { (1..5).map { "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".random() }.joinToString("") }

    private fun crearPDF_Factura(uri: Uri) {
        val pdfDocument = PdfDocument()
        val page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
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
        canvas.drawText("ID: $compraId", 50f, 105f, paint)
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
            canvas.drawText("Presente este vale y su DNI para la entrega.", 70f, y + 75f, paint.apply { isFakeBoldText = false })
        }

        pdfDocument.finishPage(page)
        contentResolver.openOutputStream(uri)?.use { pdfDocument.writeTo(it) }
        pdfDocument.close()
    }

    private fun actualizarStockFirebase(juego: Juego) {
        val dbRef = FirebaseDatabase.getInstance().getReference("productos").child(juego.id)

        // 1. Obtenemos los datos actuales directamente del servidor antes de restar
        dbRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Obtenemos el mapa de stock actual del servidor
                val detalleStockServer = snapshot.child("detalle_stock").value as? Map<String, Long> ?: emptyMap()
                val nuevoMapaStock = detalleStockServer.toMutableMap()

                val platKey = juego.plataforma.lowercase().trim()
                val stockActualPlataforma = nuevoMapaStock[platKey] ?: 0L

                if (stockActualPlataforma > 0) {
                    // 2. Restamos 1 a la plataforma específica
                    nuevoMapaStock[platKey] = stockActualPlataforma - 1

                    // 3. Recalculamos el total sumando todos los valores del mapa
                    val nuevoTotalGlobal = nuevoMapaStock.values.sum()

                    // 4. Actualizamos en Firebase
                    val actualizaciones = hashMapOf<String, Any>(
                        "detalle_stock" to nuevoMapaStock,
                        "stock" to nuevoTotalGlobal
                    )

                    dbRef.updateChildren(actualizaciones).addOnSuccessListener {
                        // Opcional: Log para confirmar en consola
                        println("Stock actualizado correctamente para ${juego.nombre}")
                    }
                }
            }
        }
    }
}