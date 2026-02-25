package com.example.prueba1integrador

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prueba1integrador.databinding.ActivityGestionarInventarioBinding
import java.io.FileOutputStream
import java.text.DateFormat
import java.util.*

class GestionarInventarioActivity : BaseActivity() {

    private lateinit var binding: ActivityGestionarInventarioBinding
    private val inventoryManager = FirebaseInventoryManager()
    private lateinit var adapter: GestionarAdapter
    private var listaVisualInventario: List<ItemInventario> = emptyList()

    private val exportarCSVLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let { generarArchivoCSV(it) }
    }

    private val exportarPDFLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        uri?.let { generarArchivoPDF(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestionarInventarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        cargarDatos() // Ahora con una sola llamada basta porque es ValueEventListener

        binding.btnExportarInventario.setOnClickListener {
            if (listaVisualInventario.isEmpty()) {
                Toast.makeText(this, "No hay datos para exportar", Toast.LENGTH_SHORT).show()
            } else {
                mostrarDialogoExportar()
            }
        }
    }

    private fun mostrarDialogoExportar() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialogo_exportar, null)
        builder.setView(view)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnCSV = view.findViewById<Button>(R.id.btnExportarCSV_Pop)
        val btnPDF = view.findViewById<Button>(R.id.btnExportarPDF_Pop)

        btnCSV.setOnClickListener {
            exportarCSVLauncher.launch("Inventario_Arcadia_${System.currentTimeMillis()}.csv")
            dialog.dismiss()
        }
        btnPDF.setOnClickListener {
            exportarPDFLauncher.launch("Inventario_Arcadia_${System.currentTimeMillis()}.pdf")
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun cargarDatos() {
        binding.progressBarGestion.visibility = View.VISIBLE
        inventoryManager.consultarInventario(object : FirebaseInventoryManager.InventoryCallback {
            override fun onDataLoaded(lista: List<Juego>) {
                binding.progressBarGestion.visibility = View.GONE
                procesarYMostrarLista(lista)
            }
        })
    }

    private fun procesarYMostrarLista(lista: List<Juego>) {
        val agrupados = lista.groupBy { it.nombre.trim().lowercase() }

        listaVisualInventario = agrupados.map { entry ->
            val listaDeEsteJuego = entry.value
            val juegoBase = listaDeEsteJuego.first()

            // Suma robusta plataforma por plataforma
            var sPS = 0; var sXB = 0; var sNI = 0; var sPC = 0

            listaDeEsteJuego.forEach { item ->
                sPS += item.detalle_stock?.get("playstation") ?: 0
                sXB += item.detalle_stock?.get("xbox") ?: 0
                sNI += item.detalle_stock?.get("nintendo") ?: 0
                sPC += item.detalle_stock?.get("pc") ?: 0
            }

            ItemInventario(
                juego = juegoBase,
                cantidad = sPS + sXB + sNI + sPC,
                idsAgrupados = listaDeEsteJuego.map { it.id },
                ps = sPS, xb = sXB, ni = sNI, pc = sPC,
                totalVistas = listaDeEsteJuego.sumOf { it.rendimiento_vistas },
                totalVentas = listaDeEsteJuego.sumOf { it.rendimiento_ventas }
            )
        }
        adapter.actualizarLista(listaVisualInventario)
    }

    private fun setupRecyclerView() {
        binding.rvInventarioGestion.layoutManager = LinearLayoutManager(this)
        // Pasamos cargarDatos como trailing lambda para refrescar tras acciones del adapter
        adapter = GestionarAdapter(listaVisualInventario, { /* Click */ }, { cargarDatos() })
        binding.rvInventarioGestion.adapter = adapter
    }

    // --- MÉTODOS DE EXPORTACIÓN (CSV Y PDF) ---
    private fun generarArchivoCSV(uri: Uri) {
        try {
            contentResolver.openOutputStream(uri)?.use { output ->
                val sb = StringBuilder()
                sb.append("Juego,Precio,Stock Total,PS,XB,NI,PC,Ventas,Vistas\n")
                listaVisualInventario.forEach { item ->
                    sb.append("${item.juego.nombre},${item.juego.precio},${item.cantidad},")
                    sb.append("${item.ps},${item.xb},${item.ni},${item.pc},")
                    sb.append("${item.totalVentas},${item.totalVistas}\n")
                }
                output.write(sb.toString().toByteArray())
                Toast.makeText(this, "CSV exportado", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) { Toast.makeText(this, "Error CSV", Toast.LENGTH_SHORT).show() }
    }

    private fun generarArchivoPDF(uri: Uri) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint()
        val pageWidth = 595
        val pageHeight = 842
        var pageNumber = 1

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        fun dibujarEncabezado(c: Canvas) {
            titlePaint.color = Color.parseColor("#FFC107")
            titlePaint.textSize = 24f
            titlePaint.isFakeBoldText = true
            c.drawText("ARCADIA GAMES - REPORTE CENTRAL", 50f, 60f, titlePaint)
            paint.textSize = 10f
            paint.color = Color.BLACK
            val fecha = DateFormat.getDateTimeInstance().format(Date())
            c.drawText("Inventario Global | $fecha", 50f, 80f, paint)
            c.drawLine(50f, 95f, 545f, 95f, paint)
        }

        dibujarEncabezado(canvas)
        var yPos = 130f

        listaVisualInventario.forEach { item ->
            if (yPos > 750) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                dibujarEncabezado(canvas)
                yPos = 130f
            }

            paint.textSize = 12f
            paint.isFakeBoldText = true
            canvas.drawText(item.juego.nombre.uppercase(), 50f, yPos, paint)
            canvas.drawText("Total: ${item.cantidad}", 450f, yPos, paint)

            yPos += 18f
            paint.textSize = 10f
            paint.isFakeBoldText = false
            paint.color = Color.DKGRAY
            canvas.drawText("PS: ${item.ps} | XB: ${item.xb} | NI: ${item.ni} | PC: ${item.pc}", 60f, yPos, paint)
            canvas.drawText("Ventas: ${item.totalVentas} | Vistas: ${item.totalVistas}", 300f, yPos, paint)

            paint.color = Color.BLACK
            yPos += 12f
            paint.alpha = 30
            canvas.drawLine(50f, yPos, 545f, yPos, paint)
            paint.alpha = 255
            yPos += 25f
        }

        pdfDocument.finishPage(page)
        try {
            contentResolver.openOutputStream(uri)?.use { os -> pdfDocument.writeTo(os) }
            Toast.makeText(this, "PDF exportado", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) { Toast.makeText(this, "Error PDF", Toast.LENGTH_SHORT).show() }
        finally { pdfDocument.close() }
    }
}