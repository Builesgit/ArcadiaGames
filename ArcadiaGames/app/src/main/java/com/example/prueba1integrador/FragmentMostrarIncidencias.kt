package com.example.prueba1integrador

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prueba1integrador.databinding.DialogoDetalleIncidenciaBinding
import com.example.prueba1integrador.databinding.FragmentMostrarIncidenciaBinding
import com.google.android.material.chip.Chip
import com.google.firebase.database.*

class FragmentMostrarIncidencias : Fragment() {

    private var _binding: FragmentMostrarIncidenciaBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: IncidenciaAdapter
    private var listaCompleta: MutableList<Incidencia> = mutableListOf()
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMostrarIncidenciaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance().getReference("incidencias")

        setupRecyclerView()
        loadIncidencias()
        setupFilters()
    }

    private fun setupRecyclerView() {
        binding.rvCatalogoCompleto.layoutManager = LinearLayoutManager(requireContext())
        adapter = IncidenciaAdapter(emptyList()) { incidencia ->
            mostrarDetalleIncidencia(incidencia)
        }
        binding.rvCatalogoCompleto.adapter = adapter
    }

    private fun mostrarDetalleIncidencia(incidencia: Incidencia) {
        val dialog = Dialog(requireContext())
        val dialogBinding = DialogoDetalleIncidenciaBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.tvDetalleTema.text = incidencia.tema.uppercase()
        dialogBinding.tvDetalleDescripcion.text = incidencia.descripcion
        dialogBinding.tvDetalleInfo.text = incidencia.infoAdicional
        dialogBinding.tvDetalleUsuario.text = "De: ${incidencia.usuarioEmail}"

        dialogBinding.btnFinalizarIncidencia.setOnClickListener {
            incidencia.id?.let { id ->
                database.child(id).removeValue().addOnSuccessListener {
                    Toast.makeText(context, "Reporte resuelto", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun loadIncidencias() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                listaCompleta.clear()
                for (postSnapshot in snapshot.children) {
                    val incidencia = postSnapshot.getValue(Incidencia::class.java)
                    if (incidencia != null) listaCompleta.add(incidencia)
                }
                filtrar()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupFilters() {
        // Filtro por Texto
        binding.searchBarCatalogo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrar()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Filtro por Chips
        binding.chipGroupFiltros.setOnCheckedStateChangeListener { _, _ ->
            filtrar()
        }
    }

    private fun filtrar() {
        val texto = binding.searchBarCatalogo.text.toString().trim().lowercase()

        // Obtener el tipo del Chip seleccionado
        val selectedChipId = binding.chipGroupFiltros.checkedChipId
        val tipoSeleccionado = if (selectedChipId != -1) {
            binding.chipGroupFiltros.findViewById<Chip>(selectedChipId).text.toString().lowercase()
        } else {
            "todos"
        }

        val listaFiltrada = listaCompleta.filter {
            val coincideTexto = it.tema.lowercase().contains(texto) ||
                    it.descripcion.lowercase().contains(texto)

            val coincideTipo = if (tipoSeleccionado == "todos") true
            else it.tipo.lowercase().contains(tipoSeleccionado)

            coincideTexto && coincideTipo
        }
        adapter.updateList(listaFiltrada)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}