package com.example.prueba1integrador.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prueba1integrador.manager.FirebaseInventoryManager
import com.example.prueba1integrador.model.Juego
import com.example.prueba1integrador.adapter.JuegoAdapter
import com.example.prueba1integrador.R
import com.example.prueba1integrador.databinding.FragmentCatalogoBinding

class FragmentCatalogo : Fragment() {

    private var _binding: FragmentCatalogoBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: JuegoAdapter
    private var listaCompleta: List<Juego> = emptyList()
    private val inventoryManager = FirebaseInventoryManager()

    private var plataformaSeleccionada: String? = null
    private var cambiandoChips = false

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

        setupRecyclerView()
        setupFilters()
        setupData()

        // ✅ Por defecto: TODOS
        marcarSolo(binding.chipFiltroTodos.id)
        plataformaSeleccionada = null
    }

    private fun setupData() {
        inventoryManager.consultarInventario(object : FirebaseInventoryManager.InventoryCallback {
            override fun onDataLoaded(lista: List<Juego>) {
                listaCompleta = lista
                filtrar("") // ✅ sin SearchBar: filtramos solo por chips
            }
        })
    }

    private fun setupRecyclerView() {
        binding.rvCatalogoCompleto.layoutManager = LinearLayoutManager(context)

        val rolUsuario = activity?.intent?.getStringExtra("ROL_USUARIO") ?: "cliente"
        val esAdmin = (rolUsuario == "admin")

        adapter = JuegoAdapter(emptyList(), esCarousel = false, esAdmin = esAdmin)
        binding.rvCatalogoCompleto.adapter = adapter
    }

    private fun setupFilters() {

        val chips = listOf(
            binding.chipFiltroTodos,
            binding.chipFiltroBug,
            binding.chipFiltroUsuario,
            binding.chipFiltroJuego,
            binding.chipFiltroOtros
        )

        // ✅ Forzamos selección única (entre las dos filas)
        chips.forEach { chip ->
            chip.setOnCheckedChangeListener { buttonView, isChecked ->
                if (cambiandoChips) return@setOnCheckedChangeListener

                if (isChecked) {
                    marcarSolo(buttonView.id)

                    plataformaSeleccionada = when (buttonView.id) {
                        R.id.chipFiltroTodos -> null
                        R.id.chipFiltroBug -> "playstation"
                        R.id.chipFiltroUsuario -> "pc"
                        R.id.chipFiltroJuego -> "xbox"
                        R.id.chipFiltroOtros -> "nintendo"
                        else -> null
                    }

                    filtrar("")
                } else {
                    // ✅ Evitar que se queden TODOS desmarcados
                    val algunoMarcado = chips.any { it.isChecked }
                    if (!algunoMarcado) {
                        marcarSolo(buttonView.id)
                    }
                }
            }
        }
    }

    private fun marcarSolo(chipId: Int) {
        cambiandoChips = true
        try {
            val chips = listOf(
                binding.chipFiltroTodos,
                binding.chipFiltroBug,
                binding.chipFiltroUsuario,
                binding.chipFiltroJuego,
                binding.chipFiltroOtros
            )
            chips.forEach { it.isChecked = (it.id == chipId) }
        } finally {
            cambiandoChips = false
        }
    }

    private fun filtrar(texto: String) {
        val query = texto.lowercase().trim()

        val listaFiltrada = listaCompleta.filter { juego ->

            // ✅ Si en el futuro metes un buscador real, esto ya te sirve
            val coincideNombre = juego.nombre.lowercase().contains(query)

            // ✅ Filtro plataforma exacto (multiplataforma con comas)
            val coincidePlataforma = plataformaSeleccionada == null || run {
                juego.plataforma
                    .lowercase()
                    .split(",")
                    .map { it.trim() }
                    .contains(plataformaSeleccionada)
            }

            coincideNombre && coincidePlataforma
        }

        adapter.setFilteredList(listaFiltrada)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}