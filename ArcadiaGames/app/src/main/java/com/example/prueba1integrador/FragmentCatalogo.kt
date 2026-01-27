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

class FragmentCatalogo : Fragment() {

    // Gestión del Binding para evitar fugas de memoria en Fragments
    private var _binding: FragmentCatalogoBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: JuegoAdapter
    private lateinit var listaCompleta: List<Juego>

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
    }

    private fun setupRecyclerView() {
        // Usamos binding para acceder al RecyclerView sin findViewById
        binding.rvCatalogoCompleto.layoutManager = LinearLayoutManager(context)
        adapter = JuegoAdapter(listaCompleta)
        binding.rvCatalogoCompleto.adapter = adapter
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
                    juego.tags.any { tag -> etiquetasSeleccionadas.contains(tag) }

            coincideNombre && coincideChip
        }

        // Actualizamos el adaptador con la nueva lista
        adapter.setFilteredList(listaFiltrada)
    }

    private fun setupData() {
        // Lista de datos de ejemplo (Mockup)
        listaCompleta = listOf(
            Juego("Super Morio World", "C morió.", R.drawable.morio, "4000.99€", listOf("MeloInvento")),
            Juego("Poly Racing", "Carreras poligonales.", R.drawable.polyracing, "0.09€", listOf("PolyStation")),
            Juego("Xbob El con-Xtructor", "Construye como nadie", R.drawable.wicher3, "49.99€", listOf("Xbob")),
            Juego("PC Master Race", "Solo para Chads.", R.drawable.pcmasterrace, "50.00€", listOf("PC")),
            Juego("Multi Plataforma", "Funciona en todo.", R.drawable.wicher3, "3.99€", listOf("PolyStation", "PC", "Xbob", "MeloInvento"))
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Limpiamos el binding para evitar fugas de memoria
        _binding = null
    }
}