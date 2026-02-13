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

        // Precio bloqueado inicialmente
        binding.etPrecio.isEnabled = false
        binding.etPrecio.setText("")

        binding.tvPlataformasSeleccionadas.setOnClickListener {
            if (juegoAEditar == null) {
                mostrarDialogoPlataformas(it as TextView)
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
        val adapter = ArrayAdapter(this, R.layout.spinner_item, categorias)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerCategoria.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnSubirImagen.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
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

        if (nombre.isEmpty() || desc.isEmpty() || precioFormateado.isEmpty() || categoria == categorias[0]) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
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
                        guardarProductoFinal(nombre, desc, precioFormateado, url, categoria, plataformaTexto, stockFinal)
                    }

                    override fun onError(mensaje: String) {
                        setLoading(false)
                        Toast.makeText(this@AnadirProductoActivity, mensaje, Toast.LENGTH_LONG).show()
                    }
                })
        } else {
            guardarProductoFinal(nombre, desc, precioFormateado, juegoAEditar?.imagenUrl ?: "", categoria, plataformaTexto, stockFinal)
        }
    }

    private fun guardarProductoFinal(
        nombre: String, desc: String, precio: String, urlImagen: String, cat: String, plat: String, stock: Int
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

        inventoryManager.subirProducto(nuevoJuego, object : FirebaseInventoryManager.ProductSaveCallback {
            override fun onSaveComplete(exito: Boolean) {
                setLoading(false)
                if (exito) {
                    inventoryManager.registrarEnHistorial("Admin", accionHistorial, nombre, stock)
                    Toast.makeText(this@AnadirProductoActivity, if (esNuevo) "¡Producto añadido!" else "¡Producto actualizado!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    if (esNuevo) {
                        Toast.makeText(this@AnadirProductoActivity, "ERROR: El nombre '$nombre' ya existe. Edítalo en el inventario.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@AnadirProductoActivity, "Error al guardar el producto", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun mostrarDialogoPlataformas(textView: TextView) {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        builder.setTitle("Selecciona Plataformas")
        builder.setCancelable(false)

        // CORRECCIÓN AQUÍ: Añadida la lógica dentro del listener de selección múltiple
        builder.setMultiChoiceItems(plataformasArray, seleccionados) { _, which, isChecked ->
            seleccionados[which] = isChecked
        }

        builder.setPositiveButton("Aceptar") { _, _ ->
            listaPlataformasElegidas.clear()
            for (i in seleccionados.indices) {
                if (seleccionados[i]) listaPlataformasElegidas.add(i)
            }

            if (listaPlataformasElegidas.isEmpty()) {
                textView.text = "Selecciona plataforma"
                textView.setTextColor(Color.parseColor("#AAAAAA"))
                binding.etPrecio.setText("")
            } else {
                val plataformasSeleccionadas = listaPlataformasElegidas.sorted().joinToString(", ") { plataformasArray[it] }
                textView.text = plataformasSeleccionadas
                textView.setTextColor(Color.WHITE)

                val precioAutomatico = when {
                    plataformasSeleccionadas.contains("PlayStation", true) || plataformasSeleccionadas.contains("PC", true) -> "69.99 €"
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
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.Amarillo))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE)
    }

    private fun mostrarPrevisualizacion(uri: Uri) {
        binding.ivPreview.visibility = View.VISIBLE
        Glide.with(this).load(uri).into(binding.ivPreview)
    }

    private fun setupModoEdicion() {
        val j = juegoAEditar ?: return
        binding.titulo.text = "DETALLES DEL PRODUCTO"
        binding.btnConfirmar.text = "VOLVER"
        binding.etNombreJuego.setText(j.nombre)
        binding.etNombreJuego.isEnabled = false
        binding.etDescripcion.setText(j.descripcion)
        binding.etDescripcion.isEnabled = false
        binding.etPrecio.setText(j.precio)
        binding.spinnerCategoria.isEnabled = false
        binding.btnSubirImagen.visibility = View.GONE

        val indexCat = categorias.indexOf(j.categoria)
        if (indexCat >= 0) binding.spinnerCategoria.setSelection(indexCat)

        if (j.imagenUrl.isNotEmpty()) {
            binding.ivPreview.visibility = View.VISIBLE
            Glide.with(this).load(j.imagenUrl).into(binding.ivPreview)
        }
        binding.btnConfirmar.setOnClickListener { finish() }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnConfirmar.isEnabled = !loading
    }
}