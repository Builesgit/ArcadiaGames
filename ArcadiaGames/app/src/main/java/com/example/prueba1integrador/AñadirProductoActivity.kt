package com.example.prueba1integrador

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class AnadirProductoActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_PICK = 2
        private const val REQUEST_PERMISSION_CAMERA = 101
        private const val REQUEST_PERMISSION_READ_STORAGE = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anadir_producto)

        // Configuración de Spinners
        val spinnerCategoria = findViewById<Spinner>(R.id.spinner_categoria)
        val spinnerPlataforma = findViewById<Spinner>(R.id.spinner_plataforma)
        val spinnerStock = findViewById<Spinner>(R.id.spinner_stock)

        val categorias = listOf("Selecciona una categoría", "Acción", "Aventura", "Deportes", "Estrategia", "RPG", "Simulación", "Otros")
        val plataformas = listOf("Selecciona una plataforma", "PlayStation", "Nintendo Switch", "Xbox Series X", "PC")
        val stock = listOf("Selecciona el stock", "En stock", "No hay stock")

        // Aplicar adaptadores
        setupCustomSpinner(spinnerCategoria, categorias)
        setupCustomSpinner(spinnerPlataforma, plataformas)
        setupCustomSpinner(spinnerStock, stock)

        // Botón Galería
        findViewById<Button>(R.id.btn_subir_imagen).setOnClickListener {
            checkStoragePermission()
        }

        // Botón Cámara
        findViewById<ImageButton>(R.id.btn_camara).setOnClickListener {
            checkCameraPermission()
        }

        // Botón Confirmar
        findViewById<Button>(R.id.btn_confirmar).setOnClickListener {
            Toast.makeText(this, "Producto confirmado", Toast.LENGTH_SHORT).show()
            finish() // Cierra la actividad al terminar
        }
    }

    private fun setupCustomSpinner(spinner: Spinner, items: List<String>) {
        val adapter = object : ArrayAdapter<String>(this, R.layout.spinner_item_layout, items) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).setTextColor(Color.WHITE)
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as TextView).apply {
                    setTextColor(Color.WHITE)
                    background = ContextCompat.getDrawable(context, R.drawable.spinner_dropdown_background)
                }
                return view
            }
        }
        spinner.adapter = adapter
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), REQUEST_PERMISSION_CAMERA)
        } else {
            abrirCamara()
        }
    }

    private fun checkStoragePermission() {
        // En versiones modernas de Android (API 33+) se usa READ_MEDIA_IMAGES
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
            android.Manifest.permission.READ_MEDIA_IMAGES
        else android.Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_PERMISSION_READ_STORAGE)
        } else {
            abrirGaleria()
        }
    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    private fun abrirCamara() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> Toast.makeText(this, "Imagen capturada", Toast.LENGTH_SHORT).show()
                REQUEST_IMAGE_PICK -> Toast.makeText(this, "Imagen seleccionada", Toast.LENGTH_SHORT).show()
            }
        }
    }
}