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
    private val plataformasArray = arrayOf("PlayStation", "Nintendo", "Xbox", "PC") // Array para el spinner de plataformas y podamos seleccionar una o varias
    private val seleccionados = BooleanArray(plataformasArray.size)
    private val listaPlataformasElegidas = mutableListOf<Int>()


    // Listas de datos para spinners
    private val categorias = listOf("Selecciona una categoría", "Acción", "Aventura", "Deportes", "Estrategia", "RPG", "Simulación", "Otros")
    // Mantenemos los textos para la UI, pero los convertiremos a Int al guardar
    private val stockOpciones = listOf("En stock", "No hay stock")

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            imagenSeleccionadaUri = uri
            mostrarPrevisualizacion(uri)
        }
    }

    // Launcher para Intent de Cámara manual que permite extras
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? android.graphics.Bitmap
            if (imageBitmap != null) {
                binding.ivPreview.visibility = View.VISIBLE
                binding.ivPreview.setImageBitmap(imageBitmap)
                Toast.makeText(this, "Foto con cámara frontal capturada.", Toast.LENGTH_SHORT).show()
                // Nota: Para subir esto a Firebase, necesitaríamos convertir Bitmap a Uri o ByteStream,
                // pero por ahora solo se muestra la previsualización como solicitó el usuario.
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnadirProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tvPlataforma = findViewById<TextView>(R.id.tv_plataformas_seleccionadas) // Se crea un TextView para mostrar las plataformas seleccionadas

        tvPlataforma.setOnClickListener { // Se crea un listener para el TextView de plataformas
            mostrarDialogoPlataformas(tvPlataforma) // Se muestra el diálogo de plataformas
        }

        setupSpinners()

        // Verificar si es edición
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
        setupCustomSpinner(binding.spinnerStock, stockOpciones)
    }

    private fun mostrarDialogoPlataformas(textView: TextView) {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        builder.setTitle("Selecciona Plataformas")
        builder.setCancelable(false)

        // Salen los elementos del array de plataformas
        builder.setMultiChoiceItems(plataformasArray, seleccionados) { _, which, isChecked ->
            if (isChecked) {
                if (!listaPlataformasElegidas.contains(which)) listaPlataformasElegidas.add(which) // Si está seleccionado, se añade a la lista
            } else {
                listaPlataformasElegidas.remove(which) // Si no está seleccionado, se elimina de la lista
            }
        }

        // Funcionalidad del botón aceptar
        builder.setPositiveButton("Aceptar") { _, _ ->
            val stringBuilder = StringBuilder() // Se crea un string builder para guardar las plataformas seleccionadas
            listaPlataformasElegidas.sort() // Se ordenan las plataformas
            for (i in listaPlataformasElegidas.indices) {
                stringBuilder.append(plataformasArray[listaPlataformasElegidas[i]]) // Se añade la plataforma seleccionada al string builder
                if (i != listaPlataformasElegidas.size - 1) stringBuilder.append(", ") // Si no es la última plataforma, se añade una coma
            }

            if (listaPlataformasElegidas.isEmpty()) { // Si no se ha seleccionado ninguna plataforma
                textView.text = "Selecciona plataforma" // Se muestra el texto "Selecciona plataforma"
                textView.setTextColor(Color.parseColor("#AAAAAA")) // Se cambia el color del texto a gris
            } else {
                textView.text = stringBuilder.toString() // Se muestra el texto con las plataformas seleccionadas
                textView.setTextColor(Color.WHITE) // Se cambia el color del texto a blanco
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() } // Funcionalidad del botón cancelar

        // Creamos el diálogo para poder acceder a los botones después de mostrarlo
        val dialog = builder.create() // Se crea el diálogo
        dialog.show() // Se muestra el diálogo

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.Amarillo)) // Se cambia el color del botón aceptar
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE) // Se cambia el color del botón cancelar
    }

    private fun setupCustomSpinner(spinner: Spinner, items: List<String>) {
        val adapter = ArrayAdapter(this, R.layout.spinner_item, items)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun setupModoEdicion() {
        binding.titulo.text = "EDITAR PRODUCTO" // ID actualizado: tvTitulo -> titulo
        binding.btnConfirmar.text = "ACTUALIZAR"

        val juego = juegoAEditar!!
        binding.etNombreJuego.setText(juego.nombre) // ID actualizado: edtNombre -> etNombreJuego
        binding.etDescripcion.setText(juego.descripcion) // ID actualizado: edtDescripcion -> etDescripcion
        binding.etPrecio.setText(juego.precio) // ID actualizado: edtPrecio -> etPrecio

        // Preseleccionar spinners
        selectSpinnerItem(binding.spinnerCategoria, categorias, juego.categoria)

        // Para el stock, como ahora es Int, seleccionamos basado en el valor
        val stockTexto = if (juego.stock > 0) "En stock" else "No hay stock"
        selectSpinnerItem(binding.spinnerStock, stockOpciones, stockTexto)

        // Mostrar imagen actual
        if (juego.imagenUrl.isNotEmpty()) {
            binding.ivPreview.visibility = View.VISIBLE
            Glide.with(this).load(juego.imagenUrl).into(binding.ivPreview)
        }
    }

    private fun selectSpinnerItem(spinner: Spinner, items: List<String>, value: String) {
        val index = items.indexOf(value)
        if (index >= 0) {
            spinner.setSelection(index)
        }
    }

    private fun setupListeners() {
        binding.btnSubirImagen.setOnClickListener { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
        binding.btnCamara.setOnClickListener {
            val cameraIntent = android.content.Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            // Solicitar cámara frontal (1)
            cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", 1)
            cameraIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
            cameraIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true)

            // Verificamos si hay alguna app de cámara antes de lanzar (aunque en emulador siempre hay)
            takePictureLauncher.launch(cameraIntent)
        }
        binding.btnConfirmar.setOnClickListener { validarYSubir() }
    }

    private fun mostrarPrevisualizacion(uri: Uri) {
        binding.ivPreview.visibility = View.VISIBLE
        Glide.with(this).load(uri).into(binding.ivPreview)
    }

    private fun validarYSubir() {
        val nombre = binding.etNombreJuego.text.toString().trim()
        val desc = binding.etDescripcion.text.toString().trim()
        val precioInput = binding.etPrecio.text.toString().trim() // Obtenemos el input

        val categoria = binding.spinnerCategoria.selectedItem.toString()
        val estadoStock = binding.spinnerStock.selectedItem.toString()

        if (nombre.isEmpty() || desc.isEmpty() || precioInput.isEmpty()) {
            Toast.makeText(this, "Por favor completa los campos de texto", Toast.LENGTH_SHORT).show()
            return
        }

        if (listaPlataformasElegidas.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos una plataforma", Toast.LENGTH_SHORT).show()
            return
        }

        // Cambio del estilo para el precio
        val precioFormateado = try {
            // Quitamos el símbolo € o espacios si el usuario los puso por error para poder convertir a número
            val limpiarPrecio = precioInput.replace("€", "").trim()
            val numero = limpiarPrecio.toDouble()

            // Formateamos a 2 decimales y añadimos el símbolo
            "%.2f €".format(java.util.Locale.US, numero)
        } catch (e: Exception) {
            Toast.makeText(this, "Por favor, introduce un precio válido", Toast.LENGTH_SHORT).show()
            return
        }

        // Construir string de plataformas
        val stringBuilder = StringBuilder()
        listaPlataformasElegidas.sort()
        for (i in listaPlataformasElegidas.indices) {
            stringBuilder.append(plataformasArray[listaPlataformasElegidas[i]])
            if (i != listaPlataformasElegidas.size - 1) stringBuilder.append(", ")
        }
        val plataforma = stringBuilder.toString()

        // Si es nuevo y no hay imagen
        if (imagenSeleccionadaUri == null && juegoAEditar == null) {
            Toast.makeText(this, "Debes seleccionar una imagen", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        // Lógica automática de stock:
        // Si el usuario marca "En stock", el sistema asigna 1 automáticamente (o podrías poner 10 si es lote).
        // Si es edición, podrías mantener el stock que ya tenía si se selecciona "En stock".
        val stockAutomatico = if (estadoStock == "En stock") {
            if (juegoAEditar != null && juegoAEditar!!.stock > 0) juegoAEditar!!.stock else 1
        } else {
            0
        }

        if (imagenSeleccionadaUri != null) {
            // Caso A: Nueva imagen (sea alta o edición con cambio de foto)
            inventoryManager.subirImagen(imagenSeleccionadaUri!!, object : FirebaseInventoryManager.ImageUploadCallback {
                override fun onUrlLoaded(url: String) {
                    guardarProductoFinal(nombre, desc, precioFormateado, url, categoria, plataforma, stockAutomatico)
                }
                override fun onError(mensaje: String) {
                    setLoading(false)
                    Toast.makeText(this@AnadirProductoActivity, "Error imagen: $mensaje", Toast.LENGTH_LONG).show()
                }
            })
        } else {
            // Caso B: Edición sin cambios de imagen
            guardarProductoFinal(nombre, desc, precioFormateado, juegoAEditar?.imagenUrl ?: "", categoria, plataforma, stockAutomatico)
        }
    }

    private fun guardarProductoFinal(nombre: String, desc: String, precio: String, urlImagen: String,
                                     cat: String, plat: String, stockInfo: Int) {

        val tagsGenerados = listOf(cat, plat)
        val idProducto = juegoAEditar?.id ?: "" // Mantiene ID si es edición

        val nuevoJuego = Juego(
            id = idProducto,
            nombre = nombre,
            descripcion = desc,
            precio = precio,
            imagenUrl = urlImagen,
            tags = tagsGenerados,
            categoria = cat,
            plataforma = plat,
            stock = stockInfo
        )

        inventoryManager.subirProducto(nuevoJuego, object : FirebaseInventoryManager.ProductSaveCallback {
            override fun onSaveComplete(exito: Boolean) {
                setLoading(false)
                if (exito) {
                    val msj = if (juegoAEditar == null) "¡Producto añadido!" else "¡Producto actualizado!"
                    Toast.makeText(this@AnadirProductoActivity, msj, Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this@AnadirProductoActivity, "Error al guardar", Toast.LENGTH_SHORT).show()
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