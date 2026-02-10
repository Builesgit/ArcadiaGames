package com.example.prueba1integrador

import android.app.AlertDialog
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.prueba1integrador.databinding.ActivityAnadirProductoBinding

class AnadirProductoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnadirProductoBinding
    private val inventoryManager = FirebaseInventoryManager()

    private var imagenSeleccionadaUri: Uri? = null
    private var juegoAEditar: Juego? = null

    private val plataformasArray = arrayOf("PlayStation", "Nintendo", "Xbox", "PC")
    private val seleccionados = BooleanArray(plataformasArray.size)
    private val listaPlataformasElegidas = mutableListOf<Int>()

    private val categorias = listOf(
        "Selecciona una categoría",
        "Acción",
        "Aventura",
        "Deportes",
        "Estrategia",
        "RPG",
        "Simulación",
        "Otros"
    )

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                imagenSeleccionadaUri = uri
                mostrarPrevisualizacion(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnadirProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tvPlataforma = findViewById<TextView>(R.id.tv_plataformas_seleccionadas)
        tvPlataforma.setOnClickListener {
            mostrarDialogoPlataformas(tvPlataforma)
        }

        setupSpinners()

        if (intent.hasExtra("JUEGO_A_EDITAR")) {
            juegoAEditar = intent.getSerializableExtra("JUEGO_A_EDITAR") as? Juego
        }

        if (juegoAEditar != null) {
            setupModoEdicion()
        }

        setupListeners()
    }

    private fun setupSpinners() {
        setupCustomSpinner(binding.spinnerCategoria, categorias)
    }

    private fun setupCustomSpinner(spinner: Spinner, items: List<String>) {
        val adapter = ArrayAdapter(this, R.layout.spinner_item, items)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun setupModoEdicion() {
        val juego = juegoAEditar ?: return

        binding.titulo.text = "EDITAR PRODUCTO"
        binding.btnConfirmar.text = "ACTUALIZAR"

        binding.etNombreJuego.setText(juego.nombre)
        binding.etDescripcion.setText(juego.descripcion)
        binding.etPrecio.setText(juego.precio)

        selectSpinnerItem(binding.spinnerCategoria, categorias, juego.categoria)

        if (juego.imagenUrl.isNotEmpty()) {
            binding.ivPreview.visibility = View.VISIBLE
            Glide.with(this).load(juego.imagenUrl).into(binding.ivPreview)
        }
    }

    private fun selectSpinnerItem(spinner: Spinner, items: List<String>, value: String) {
        val index = items.indexOf(value)
        if (index >= 0) spinner.setSelection(index)
    }

    private fun setupListeners() {
        binding.btnSubirImagen.setOnClickListener {
            pickMedia.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.btnConfirmar.setOnClickListener {
            validarYSubir()
        }
    }

    private fun mostrarDialogoPlataformas(textView: TextView) {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        builder.setTitle("Selecciona Plataformas")
        builder.setCancelable(false)

        builder.setMultiChoiceItems(plataformasArray, seleccionados) { _, which, isChecked ->
            if (isChecked) {
                if (!listaPlataformasElegidas.contains(which)) {
                    listaPlataformasElegidas.add(which)
                }
            } else {
                listaPlataformasElegidas.remove(which)
            }
        }

        builder.setPositiveButton("Aceptar") { _, _ ->
            if (listaPlataformasElegidas.isEmpty()) {
                textView.text = "Selecciona plataforma"
                textView.setTextColor(Color.parseColor("#AAAAAA"))
            } else {
                val plataformasTexto = listaPlataformasElegidas.sorted()
                    .joinToString(", ") { plataformasArray[it] }
                textView.text = plataformasTexto
                textView.setTextColor(Color.WHITE)
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }

        val dialog = builder.create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(ContextCompat.getColor(this, R.color.Amarillo))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            .setTextColor(Color.WHITE)
    }

    private fun mostrarPrevisualizacion(uri: Uri) {
        binding.ivPreview.visibility = View.VISIBLE
        Glide.with(this).load(uri).into(binding.ivPreview)
    }

    private fun validarYSubir() {
        val nombre = binding.etNombreJuego.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()
        val precioInput = binding.etPrecio.text.toString().trim()
        val categoria = binding.spinnerCategoria.selectedItem.toString()

        if (nombre.isEmpty() || descripcion.isEmpty() || precioInput.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (listaPlataformasElegidas.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos una plataforma", Toast.LENGTH_SHORT).show()
            return
        }

        val precioFormateado = try {
            val numero = precioInput.replace("€", "").trim().toDouble()
            "%.2f €".format(numero)
        } catch (e: Exception) {
            Toast.makeText(this, "Precio no válido", Toast.LENGTH_SHORT).show()
            return
        }

        if (imagenSeleccionadaUri == null && juegoAEditar == null) {
            Toast.makeText(this, "Debes seleccionar una imagen", Toast.LENGTH_SHORT).show()
            return
        }

        val plataformaTexto = listaPlataformasElegidas.sorted()
            .joinToString(", ") { plataformasArray[it] }

        setLoading(true)

        if (imagenSeleccionadaUri != null) {
            inventoryManager.subirImagen(imagenSeleccionadaUri!!, object :
                FirebaseInventoryManager.ImageUploadCallback {
                override fun onUrlLoaded(url: String) {
                    guardarProductoFinal(
                        nombre,
                        descripcion,
                        precioFormateado,
                        url,
                        categoria,
                        plataformaTexto
                    )
                }

                override fun onError(mensaje: String) {
                    setLoading(false)
                    Toast.makeText(this@AnadirProductoActivity, mensaje, Toast.LENGTH_LONG).show()
                }
            })
        } else {
            guardarProductoFinal(
                nombre,
                descripcion,
                precioFormateado,
                juegoAEditar?.imagenUrl ?: "",
                categoria,
                plataformaTexto
            )
        }
    }

    private fun guardarProductoFinal(
        nombre: String,
        descripcion: String,
        precio: String,
        imagenUrl: String,
        categoria: String,
        plataforma: String
    ) {
        val nuevoJuego = Juego(
            id = juegoAEditar?.id ?: "",
            nombre = nombre,
            descripcion = descripcion,
            precio = precio,
            imagenUrl = imagenUrl,
            categoria = categoria,
            plataforma = plataforma
            // stock NO se toca aquí
        )

        inventoryManager.subirProducto(nuevoJuego, object :
            FirebaseInventoryManager.ProductSaveCallback {
            override fun onSaveComplete(exito: Boolean) {
                setLoading(false)
                if (exito) {
                    Toast.makeText(
                        this@AnadirProductoActivity,
                        if (juegoAEditar == null) "Producto añadido" else "Producto actualizado",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                } else {
                    Toast.makeText(this@AnadirProductoActivity, "Error al guardar", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnConfirmar.isEnabled = !loading
        binding.btnSubirImagen.isEnabled = !loading
    }
}
