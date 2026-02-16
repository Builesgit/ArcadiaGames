package com.example.prueba1integrador

import android.app.AlertDialog
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.prueba1integrador.databinding.ActivityAnadirProductoBinding
import com.bumptech.glide.Glide

class AnadirProductoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnadirProductoBinding
    private val inventoryManager = FirebaseInventoryManager()
    private var imagenSeleccionadaUri: Uri? = null
    private var juegoAEditar: Juego? = null

    private val plataformasArray = arrayOf("PlayStation", "Nintendo", "Xbox", "PC")
    private val seleccionados = BooleanArray(plataformasArray.size)
    private val categorias = listOf("Selecciona una categoría", "Acción", "Aventura", "Deportes", "Estrategia", "RPG", "Simulación", "Otros")

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            imagenSeleccionadaUri = uri
            mostrarPrevisualizacion(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnadirProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etPrecio.isEnabled = false
        binding.tvPlataformasSeleccionadas.setOnClickListener {
            if (juegoAEditar == null) mostrarDialogoPlataformas(it as TextView)
        }

        val adapter = ArrayAdapter(this, R.layout.spinner_item, categorias)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerCategoria.adapter = adapter

        if (intent.hasExtra("JUEGO_A_EDITAR")) {
            juegoAEditar = intent.getSerializableExtra("JUEGO_A_EDITAR") as? Juego
            if (juegoAEditar != null) setupModoEdicion()
        }

        binding.btnSubirImagen.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.btnConfirmar.setOnClickListener { validarYSubir() }
    }

    private fun validarYSubir() {
        val nombre = binding.etNombreJuego.text.toString().trim()
        val desc = binding.etDescripcion.text.toString().trim()
        val cat = binding.spinnerCategoria.selectedItem.toString()
        val precio = binding.etPrecio.text.toString()

        if (nombre.isEmpty() || desc.isEmpty() || cat == categorias[0]) {
            Toast.makeText(this, "Completa los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val sb = StringBuilder()
        var alguno = false
        for (i in 0 until seleccionados.size) {
            if (seleccionados[i]) {
                if (alguno) sb.append(", ")
                sb.append(plataformasArray[i])
                alguno = true
            }
        }
        val platTexto = if (juegoAEditar != null) juegoAEditar!!.plataforma else sb.toString()

        if (platTexto.isEmpty()) {
            Toast.makeText(this, "Selecciona plataforma", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        if (imagenSeleccionadaUri != null) {
            inventoryManager.subirImagen(imagenSeleccionadaUri!!, object : FirebaseInventoryManager.ImageUploadCallback {
                override fun onUrlLoaded(url: String) {
                    enviarAFirebase(nombre, desc, precio, url, cat, platTexto)
                }
                override fun onError(m: String) { setLoading(false); Toast.makeText(this@AnadirProductoActivity, m, Toast.LENGTH_SHORT).show() }
            })
        } else {
            enviarAFirebase(nombre, desc, precio, juegoAEditar?.imagenUrl ?: "", cat, platTexto)
        }
    }

    private fun enviarAFirebase(n: String, d: String, p: String, u: String, c: String, pl: String) {
        val nuevo = Juego(id = juegoAEditar?.id ?: "", nombre = n, descripcion = d, precio = p, imagenUrl = u, categoria = c, plataforma = pl, stock = juegoAEditar?.stock ?: 1)
        inventoryManager.subirProducto(nuevo, object : FirebaseInventoryManager.ProductSaveCallback {
            override fun onSaveComplete(exito: Boolean) {
                setLoading(false)
                if (exito) finish() else Toast.makeText(this@AnadirProductoActivity, "Error al guardar", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun mostrarDialogoPlataformas(tv: TextView) {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        builder.setTitle("Plataformas")
        builder.setMultiChoiceItems(plataformasArray, seleccionados) { _, which, isChecked -> seleccionados[which] = isChecked }
        builder.setPositiveButton("Ok") { _, _ ->
            val sb = StringBuilder()
            var primero = true
            for (i in 0 until seleccionados.size) {
                if (seleccionados[i]) {
                    if (!primero) sb.append(", ")
                    sb.append(plataformasArray[i])
                    primero = false
                }
            }
            tv.text = if (sb.isEmpty()) "Selecciona plataforma" else sb.toString()
            binding.etPrecio.setText(if (sb.contains("PlayStation")) "69.99 €" else "49.99 €")
        }
        builder.show()
    }

    private fun mostrarPrevisualizacion(uri: Uri) {
        binding.ivPreview.visibility = View.VISIBLE
        Glide.with(this).load(uri).into(binding.ivPreview)
    }

    private fun setupModoEdicion() {
        val j = juegoAEditar!!
        binding.etNombreJuego.setText(j.nombre)
        binding.etDescripcion.setText(j.descripcion)
        binding.etPrecio.setText(j.precio)
        binding.btnConfirmar.text = "VOLVER"
        binding.btnConfirmar.setOnClickListener { finish() }
    }

    private fun setLoading(l: Boolean) {
        binding.progressBar.visibility = if (l) View.VISIBLE else View.GONE
        binding.btnConfirmar.isEnabled = !l
    }
}