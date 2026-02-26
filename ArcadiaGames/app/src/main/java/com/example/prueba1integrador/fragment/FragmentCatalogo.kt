package com.example.prueba1integrador.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prueba1integrador.R
import com.example.prueba1integrador.manager.FirebaseInventoryManager
import com.example.prueba1integrador.model.Juego
import com.example.prueba1integrador.adapter.JuegoAdapter
import com.example.prueba1integrador.databinding.FragmentCatalogoBinding

class FragmentCatalogo : Fragment() {

    private var _binding: FragmentCatalogoBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: JuegoAdapter
    private var listaCompleta: List<Juego> = emptyList()
    private val inventoryManager = FirebaseInventoryManager()

    private var plataformaSeleccionada: String? = null
    private var cambiandoChips = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCatalogoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFilters()
        setupData()
        marcarSolo(binding.chipFiltroTodos.id)
    }

    private fun setupData() {
        inventoryManager.consultarInventario(object : FirebaseInventoryManager.InventoryCallback {
            override fun onDataLoaded(lista: List<Juego>) {
                listaCompleta = lista
                filtrar("")
            }
        })
    }

    private fun setupRecyclerView() {
        binding.rvCatalogoCompleto.layoutManager = LinearLayoutManager(context)
        val rolUsuario = activity?.intent?.getStringExtra("ROL_USUARIO") ?: "cliente"
        adapter = JuegoAdapter(emptyList(), esCarousel = false, esAdmin = (rolUsuario == "admin"))
        binding.rvCatalogoCompleto.adapter = adapter
    }

    private fun setupFilters() {
        val chips = listOf(binding.chipFiltroTodos, binding.chipFiltroBug, binding.chipFiltroUsuario, binding.chipFiltroJuego, binding.chipFiltroOtros)
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
                } else if (!chips.any { it.isChecked }) {
                    marcarSolo(buttonView.id)
                }
            }
        }
    }

    private fun marcarSolo(chipId: Int) {
        cambiandoChips = true
        val chips = listOf(binding.chipFiltroTodos, binding.chipFiltroBug, binding.chipFiltroUsuario, binding.chipFiltroJuego, binding.chipFiltroOtros)
        chips.forEach { it.isChecked = (it.id == chipId) }
        cambiandoChips = false
    }

    private fun filtrar(texto: String) {
        val query = texto.lowercase().trim()
        val listaFiltrada = listaCompleta.filter { juego ->
            val coincidePlataforma = plataformaSeleccionada == null || juego.plataforma.lowercase().split(",").map { it.trim() }.contains(plataformaSeleccionada)
            juego.nombre.lowercase().contains(query) && coincidePlataforma
        }
        adapter.setFilteredList(listaFiltrada)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}