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
import com.example.prueba1integrador.databinding.ActivityAnadirProductoBinding
import com.bumptech.glide.Glide

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

        // 🔥 Precio bloqueado pero vacío
        binding.etPrecio.isEnabled = false
        binding.etPrecio.setText("")

        val tvPlataforma = findViewById<TextView>(R.id.tv_plataformas_seleccionadas)
        tvPlataforma.setOnClickListener {
            if (juegoAEditar == null) {
                mostrarDialogoPlataformas(tvPlataforma)
            }
        }

        setupSpinners()

        if (intent.hasExtra("JUEGO_A_EDITAR")) {
            juegoAEditar = intent.getSerializableExtra("JUEGO_A_EDITAR") as? Juego
            if (juegoAEditar != null) {
                setupModoEdicion()
            }
        }

        setupListeners()
    }

    private fun setupSpinners() {
        setupCustomSpinner(binding.spinnerCategoria, categorias)
    }

    private fun setupListeners() {
        binding.btnSubirImagen.setOnClickListener {
            pickMedia.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }

        binding.btnConfirmar.setOnClickListener {
            validarYSubir()
        }
    }

    private fun validarYSubir() {

        val nombre = binding.etNombreJuego.text.toString().trim()
        val desc = binding.etDescripcion.text.toString().trim()
        val categoria = binding.spinnerCategoria.selectedItem.toString()
        val precioFormateado = binding.etPrecio.text.toString()

        if (nombre.isEmpty() || desc.isEmpty() || precioFormateado.isEmpty()) {
            Toast.makeText(this, "Por favor completa los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (listaPlataformasElegidas.isEmpty() && juegoAEditar == null) {
            Toast.makeText(this, "Selecciona al menos una plataforma", Toast.LENGTH_SHORT).show()
            return
        }

        val plataformaTexto = if (juegoAEditar != null) {
            juegoAEditar!!.plataforma
        } else {
            listaPlataformasElegidas.sorted().joinToString(", ") { plataformasArray[it] }
        }

        if (imagenSeleccionadaUri == null && juegoAEditar == null) {
            Toast.makeText(this, "Debes seleccionar una imagen", Toast.LENGTH_SHORT).show()
            return
        }

        val stockFinal = juegoAEditar?.stock ?: 1

        setLoading(true)

        if (imagenSeleccionadaUri != null) {
            inventoryManager.subirImagen(
                imagenSeleccionadaUri!!,
                object : FirebaseInventoryManager.ImageUploadCallback {
                    override fun onUrlLoaded(url: String) {
                        guardarProductoFinal(
                            nombre,
                            desc,
                            precioFormateado,
                            url,
                            categoria,
                            plataformaTexto,
                            stockFinal
                        )
                    }

                    override fun onError(mensaje: String) {
                        setLoading(false)
                        Toast.makeText(
                            this@AnadirProductoActivity,
                            mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
        } else {
            guardarProductoFinal(
                nombre,
                desc,
                precioFormateado,
                juegoAEditar?.imagenUrl ?: "",
                categoria,
                plataformaTexto,
                stockFinal
            )
        }
    }

    private fun guardarProductoFinal(
        nombre: String,
        desc: String,
        precio: String,
        urlImagen: String,
        cat: String,
        plat: String,
        stock: Int
    ) {

        val esNuevo = juegoAEditar == null
        val idProducto = if (esNuevo) "" else juegoAEditar!!.id
        val accionHistorial = if (esNuevo) "añadió" else "actualizó"

        val nuevoJuego = Juego(
            id = idProducto,
            nombre = nombre,
            descripcion = desc,
            precio = precio,
            imagenUrl = urlImagen,
            tags = listOf(cat, plat),
            categoria = cat,
            plataforma = plat,
            stock = stock
        )

        inventoryManager.subirProducto(
            nuevoJuego,
            object : FirebaseInventoryManager.ProductSaveCallback {
                override fun onSaveComplete(exito: Boolean) {
                    setLoading(false)
                    if (exito) {
                        inventoryManager.registrarEnHistorial(
                            "Admin",
                            accionHistorial,
                            nombre,
                            stock
                        )
                        Toast.makeText(
                            this@AnadirProductoActivity,
                            if (esNuevo) "¡Producto añadido!" else "¡Producto actualizado!",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                }
            })
    }

    private fun mostrarDialogoPlataformas(textView: TextView) {

        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        builder.setTitle("Selecciona Plataformas")
        builder.setCancelable(false)

        builder.setMultiChoiceItems(plataformasArray, seleccionados) { _, which, isChecked ->
            if (isChecked) {
                if (!listaPlataformasElegidas.contains(which))
                    listaPlataformasElegidas.add(which)
            } else {
                listaPlataformasElegidas.remove(which)
            }
        }

        builder.setPositiveButton("Aceptar") { _, _ ->

            if (listaPlataformasElegidas.isEmpty()) {
                textView.text = "Selecciona plataforma"
                textView.setTextColor(Color.parseColor("#AAAAAA"))
                binding.etPrecio.setText("")
            } else {

                val plataformasSeleccionadas =
                    listaPlataformasElegidas.sorted()
                        .joinToString(", ") { plataformasArray[it] }

                textView.text = plataformasSeleccionadas
                textView.setTextColor(Color.WHITE)

                // 🔥 PRECIO AUTOMÁTICO EN PANTALLA
                val precioAutomatico = when {
                    plataformasSeleccionadas.contains("PlayStation", true) ||
                            plataformasSeleccionadas.contains("PC", true) -> "69.99 €"

                    plataformasSeleccionadas.contains("Xbox", true) -> "59.99 €"

                    plataformasSeleccionadas.contains("Nintendo", true) -> "49.99 €"

                    else -> "0.00 €"
                }

                binding.etPrecio.setText(precioAutomatico)
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

    private fun setupModoEdicion() {
        val juego = juegoAEditar ?: return

        binding.titulo.text = "DETALLES DEL PRODUCTO"
        binding.btnConfirmar.text = "VOLVER"

        binding.etNombreJuego.setText(juego.nombre)
        binding.etNombreJuego.isEnabled = false

        binding.etDescripcion.setText(juego.descripcion)
        binding.etDescripcion.isEnabled = false

        binding.etPrecio.setText(juego.precio)
        binding.etPrecio.isEnabled = false

        binding.spinnerCategoria.isEnabled = false
        binding.btnSubirImagen.visibility = View.GONE

        val indexCat = categorias.indexOf(juego.categoria)
        if (indexCat >= 0)
            binding.spinnerCategoria.setSelection(indexCat)

        if (juego.imagenUrl.isNotEmpty()) {
            binding.ivPreview.visibility = View.VISIBLE
            Glide.with(this).load(juego.imagenUrl).into(binding.ivPreview)
        }

        binding.btnConfirmar.setOnClickListener { finish() }
    }

    private fun setupCustomSpinner(spinner: Spinner, items: List<String>) {
        val adapter = ArrayAdapter(this, R.layout.spinner_item, items)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility =
            if (loading) View.VISIBLE else View.GONE
        binding.btnConfirmar.isEnabled = !loading
        if (juegoAEditar == null)
            binding.btnSubirImagen.isEnabled = !loading
    }
}
