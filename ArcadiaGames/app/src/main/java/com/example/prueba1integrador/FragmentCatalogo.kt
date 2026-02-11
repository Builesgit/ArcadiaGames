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
import com.bumptech.glide.Glide

class FragmentCatalogo : Fragment() {

    private var _binding: FragmentCatalogoBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: JuegoAdapter
    private var listaCompleta: List<Juego> = emptyList()
    private val inventoryManager = FirebaseInventoryManager()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

    private fun setupData() {
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
        binding.rvCatalogoCompleto.layoutManager = LinearLayoutManager(context)

        val rolUsuario = activity?.intent?.getStringExtra("ROL_USUARIO") ?: "cliente"
        val esAdmin = rolUsuario == "admin"

        // Pasamos lista vacía inicialmente, se actualizará en setupData
        adapter = JuegoAdapter(emptyList(), esCarousel = false, esAdmin = esAdmin)
        binding.rvCatalogoCompleto.adapter = adapter
    }

    private fun setupFilters() {
        binding.searchViewCatalogo.setupWithSearchBar(binding.searchBarCatalogo)

        binding.searchViewCatalogo.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val texto = s.toString()
                binding.searchBarCatalogo.setText(texto)
                filtrar(texto)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.chipGroupCategorias.setOnCheckedStateChangeListener { _, _ ->
            val textoBusqueda = binding.searchViewCatalogo.text.toString()
            filtrar(textoBusqueda)
        }
    }

    private fun filtrar(texto: String) {
        val etiquetasSeleccionadas = binding.chipGroupCategorias.checkedChipIds.map { id ->
            binding.chipGroupCategorias.findViewById<Chip>(id).text.toString()
        }

        // CORREGIDO: Lógica de filtrado con retorno Boolean explícito
        val listaFiltrada = listaCompleta.filter { juego ->
            val coincideNombre = juego.nombre.contains(texto, ignoreCase = true)
            val coincideChip = etiquetasSeleccionadas.isEmpty() || etiquetasSeleccionadas.any { selected ->
                juego.categoria.contains(selected, ignoreCase = true) ||
                        juego.plataforma.contains(selected, ignoreCase = true)
            }

            coincideNombre && coincideChip // Esto devuelve el Boolean que necesita filter
        }

        adapter.setFilteredList(listaFiltrada)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}