package com.example.prueba1integrador

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prueba1integrador.databinding.FragmentCatalogoBinding
import com.google.android.material.chip.Chip
import android.content.Intent
import android.graphics.Color
import com.example.prueba1integrador.databinding.DialogDetalleJuegoBinding // Asegúrate de crear este layout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.bumptech.glide.Glide

class FragmentCatalogo : Fragment() {

    // Gestión del Binding para evitar fugas de memoria en Fragments
    private var _binding: FragmentCatalogoBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: JuegoAdapter
    private var listaCompleta: List<Juego> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflamos el layout usando ViewBinding
        _binding = FragmentCatalogoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupData()
        setupRecyclerView()
        setupFilters()
        setupFab()
    }

    private val inventoryManager = FirebaseInventoryManager()

    private fun setupData() {
        // Usamos Firebase para cargar datos reales
        inventoryManager.consultarInventario(object: FirebaseInventoryManager.InventoryCallback {
            override fun onDataLoaded(lista: List<Juego>) {
                listaCompleta = lista
                if (::adapter.isInitialized) {
                    adapter.setFilteredList(listaCompleta)
                }
            }
        })
    }

    private fun setupFab() {
        // Lógica para mostrar/ocultar FAB según rol
        val rolUsuario = activity?.intent?.getStringExtra("ROL_USUARIO") ?: "cliente"

        if (rolUsuario == "admin") {
            binding.fabAgregarJuego.visibility = View.VISIBLE
            binding.fabAgregarJuego.setOnClickListener {
                startActivity(Intent(requireContext(), AnadirProductoActivity::class.java))
            }
        } else {
            binding.fabAgregarJuego.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        // Usamos binding para acceder al RecyclerView sin findViewById
        binding.rvCatalogoCompleto.layoutManager = LinearLayoutManager(context)
        // Inicializamos el adapter con la lambda para abrir el pop-up
        adapter = JuegoAdapter(listaCompleta, esCarousel = false) { juego ->
            mostrarPopUpJuego(juego)
        }
        binding.rvCatalogoCompleto.adapter = adapter
    }

    // Función para mostrar el detalle del juego en un Pop-up deslizable
    // Función para mostrar el detalle del juego en un recuadro flotante centrado
    private fun mostrarPopUpJuego(juego: Juego) {
        // Creamos el diálogo con un estilo base
        val dialog = android.app.Dialog(requireContext())
        val dialogBinding = DialogDetalleJuegoBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        // Hacemos que el fondo de la ventana del diálogo sea transparente
        // para que se vea nuestro diseño redondeado
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Asignamos los datos del juego al pop-up
        dialogBinding.tvDetalleNombre.text = juego.nombre
        dialogBinding.tvDetalleDescripcion.text = juego.descripcion
        dialogBinding.tvDetallePrecio.text = juego.precio
        dialogBinding.tvDetallePlataforma.text = juego.tags.joinToString(", ")

        // Lógica de Stock: Verde si hay, Rojo y 0 si no
        // Como ahora es Int, la comparación es directa
        if (juego.stock > 0) {
            dialogBinding.tvDetalleStock.text = "Stock: ${juego.stock}"
            dialogBinding.tvDetalleStock.setTextColor(Color.GREEN)
        } else {
            dialogBinding.tvDetalleStock.text = "Stock: 0"
            dialogBinding.tvDetalleStock.setTextColor(Color.RED)
            dialogBinding.btnComprar.isEnabled = false // Desactivar si no hay stock
        }

        // Carga de imagen con Glide
        if (juego.imagenUrl.isNotEmpty()) {
            Glide.with(this).load(juego.imagenUrl).into(dialogBinding.ivDetalleImagen)
        } else {
            dialogBinding.ivDetalleImagen.setImageResource(juego.imagenResId)
        }

        // Acciones de los botones
        dialogBinding.btnComprar.setOnClickListener {
            // Aquí iría tu lógica de compra
            dialog.dismiss()
        }
        dialogBinding.btnAlquilar.setOnClickListener {
            // Aquí iría tu lógica de alquiler
            dialog.dismiss()
        }

        dialog.show()

        // Ajustamos el tamaño del diálogo para que no ocupe toda la pantalla
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt() // 90% del ancho
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun setupFilters() {
        // Configuramos la conexión entre SearchBar y SearchView
        binding.searchViewCatalogo.setupWithSearchBar(binding.searchBarCatalogo)

        // Escuchador de texto para búsqueda en tiempo real
        binding.searchViewCatalogo.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val texto = s.toString()
                // Corrección del error .text: usamos setText()
                binding.searchBarCatalogo.setText(texto)
                filtrar(texto)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Escuchador para cambios en los Chips de categorías
        binding.chipGroupCategorias.setOnCheckedStateChangeListener { _, _ ->
            val textoBusqueda = binding.searchViewCatalogo.text.toString()
            filtrar(textoBusqueda)
        }
    }

    private fun filtrar(texto: String) {
        // Obtenemos los nombres de los chips seleccionados
        val etiquetasSeleccionadas = binding.chipGroupCategorias.checkedChipIds.map { id ->
            binding.chipGroupCategorias.findViewById<Chip>(id).text.toString()
        }

        // Aplicamos el filtro a la lista completa
        val listaFiltrada = listaCompleta.filter { juego ->
            val coincideNombre = juego.nombre.contains(texto, ignoreCase = true)
            val coincideChip = etiquetasSeleccionadas.isEmpty() ||
                    juego.tags.any { tag ->
                        etiquetasSeleccionadas.any { selected -> tag.contains(selected, ignoreCase = true) }
                    }

            coincideNombre && coincideChip
        }

        // Actualizamos el adaptador con la nueva lista
        adapter.setFilteredList(listaFiltrada)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Limpiamos el binding para evitar fugas de memoria
        _binding = null
    }
}