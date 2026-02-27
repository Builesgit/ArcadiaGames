package com.example.prueba1integrador.activity

import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prueba1integrador.R
import com.example.prueba1integrador.manager.FirebaseInventoryManager
import com.example.prueba1integrador.adapter.GestionarAdapter
import com.example.prueba1integrador.model.ItemInventario
import com.example.prueba1integrador.model.Juego
import com.example.prueba1integrador.databinding.ActivityGestionarInventarioBinding

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

        binding.btnExportarInventario.setOnClickListener {
            if (listaVisualInventario.isEmpty()) {
                Toast.makeText(this, "No hay datos para exportar", Toast.LENGTH_SHORT).show()
            } else {
                mostrarDialogoExportar()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cargarDatos()
    }

    private fun setupRecyclerView() {
        binding.rvInventarioGestion.layoutManager = LinearLayoutManager(this)
        // Asegúrate de que en tu adaptador, cuando edites stock, llames a:
        // inventoryManager.verificarStockYRegistrar(juego, nuevoStockTotal)
        adapter = GestionarAdapter(
            listaInventario = listaVisualInventario,
            onEditClick = { },
            onDataChanged = { cargarDatos() }
        )
        binding.rvInventarioGestion.adapter = adapter
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
            val juegoRepresentante = listaDeEsteJuego.first()

            val stockPS = listaDeEsteJuego.sumOf { it.detalle_stock?.get("playstation") ?: 0 }
            val stockXB = listaDeEsteJuego.sumOf { it.detalle_stock?.get("xbox") ?: 0 }
            val stockNI = listaDeEsteJuego.sumOf { it.detalle_stock?.get("nintendo") ?: 0 }
            val stockPC = listaDeEsteJuego.sumOf { it.detalle_stock?.get("pc") ?: 0 }

            ItemInventario(
                juego = juegoRepresentante,
                cantidad = stockPS + stockXB + stockNI + stockPC,
                idsAgrupados = listaDeEsteJuego.map { it.id },
                ps = stockPS,
                xb = stockXB,
                ni = stockNI,
                pc = stockPC,
                totalVentas = listaDeEsteJuego.sumOf { it.rendimiento_ventas }
            )
        }
        adapter.actualizarLista(listaVisualInventario)
    }

    private fun mostrarDialogoExportar() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialogo_exportar, null)
        builder.setView(view)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnCSV = view.findViewById<android.widget.Button>(R.id.btnExportarCSV_Pop)
        val btnPDF = view.findViewById<android.widget.Button>(R.id.btnExportarPDF_Pop)

        val timestamp = System.currentTimeMillis()

        btnCSV.setOnClickListener {
            exportarCSVLauncher.launch("Inventario_Arcadia_$timestamp.csv")
            dialog.dismiss()
        }

        btnPDF.setOnClickListener {
            exportarPDFLauncher.launch("Inventario_Arcadia_$timestamp.pdf")
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun generarArchivoCSV(uri: Uri) {
        try {
            contentResolver.openOutputStream(uri)?.use { output ->
                val sb = StringBuilder()
                sb.append("Juego,Precio,Stock Total,Ventas\n")
                listaVisualInventario.forEach { item ->
                    sb.append("${item.juego.nombre},${item.juego.precio},${item.cantidad},${item.totalVentas}\n")
                }
                output.write(sb.toString().toByteArray())
                Toast.makeText(this, "CSV exportado correctamente", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al exportar CSV", Toast.LENGTH_SHORT).show()
        }
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

        fun dibujarEncabezado(canvas: android.graphics.Canvas) {
            titlePaint.color = Color.parseColor("#FFC107")
            titlePaint.textSize = 24f
            titlePaint.isFakeBoldText = true
            canvas.drawText("ARCADIA GAMES - REPORTE DE STOCK", 50f, 60f, titlePaint)

            paint.textSize = 10f
            paint.color = Color.BLACK
            val fecha = java.text.DateFormat.getDateTimeInstance().format(java.util.Date())
            canvas.drawText("Generado: $fecha", 50f, 80f, paint)
            canvas.drawLine(50f, 95f, 545f, 95f, paint)
        }

        dibujarEncabezado(canvas)
        var yPos = 130f

        // 1. LISTADO DE INVENTARIO
        listaVisualInventario.forEach { item ->
            if (yPos > 700) {
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

            val listaPlat = mutableListOf<String>()
            if (item.ps > 0) listaPlat.add("PS: ${item.ps}")
            if (item.xb > 0) listaPlat.add("XB: ${item.xb}")
            if (item.ni > 0) listaPlat.add("NI: ${item.ni}")
            if (item.pc > 0) listaPlat.add("PC: ${item.pc}")

            val detalleStr = if (listaPlat.isEmpty()) "Sin stock disponible" else listaPlat.joinToString(" | ")
            canvas.drawText(detalleStr, 60f, yPos, paint)
            canvas.drawText("Ventas: ${item.totalVentas}", 350f, yPos, paint)

            paint.color = Color.BLACK
            yPos += 12f
            paint.alpha = 40
            canvas.drawLine(50f, yPos, 545f, yPos, paint)
            paint.alpha = 255
            yPos += 25f
        }

        // 2. SECCIÓN TOP 5 MÁS VENDIDOS
        yPos += 30f
        if (yPos > 650) {
            pdfDocument.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            dibujarEncabezado(canvas)
            yPos = 130f
        }

        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("TOP 5 JUEGOS MÁS VENDIDOS", 50f, yPos, paint)
        yPos += 25f

        val top5 = listaVisualInventario.sortedByDescending { it.totalVentas }.take(5)
        paint.textSize = 11f
        paint.isFakeBoldText = false

        top5.forEachIndexed { index, item ->
            canvas.drawText("${index + 1}. ${item.juego.nombre}", 60f, yPos, paint)
            canvas.drawText("${item.totalVentas} unidades", 450f, yPos, paint)
            yPos += 20f
        }

        pdfDocument.finishPage(page)

        try {
            contentResolver.openOutputStream(uri)?.use { os ->
                pdfDocument.writeTo(os)
            }
            Toast.makeText(this, "PDF exportado correctamente", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al generar PDF", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }
}