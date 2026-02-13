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
import com.example.prueba1integrador.databinding.DialogDetalleIncidenciaBinding
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
        val dialogBinding = DialogDetalleIncidenciaBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        // Fondo transparente para el diseño redondeado
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Asignación de datos
        dialogBinding.tvDetalleTema.text = incidencia.tema
        dialogBinding.tvDetalleDescripcion.text = incidencia.descripcion
        dialogBinding.tvDetalleInfo.text = incidencia.infoAdicional
        dialogBinding.tvDetalleTipo.text = incidencia.tipo.uppercase()
        dialogBinding.tvDetalleUsuario.text = "Enviada por: ${incidencia.usuarioEmail} con id: ${incidencia.usuarioId}"

        // Acción del botón Finalizar
        dialogBinding.btnFinalizarIncidencia.setOnClickListener {
            incidencia.id?.let { id ->
                database.child(id).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Incidencia finalizada y borrada", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error al finalizar: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }

        dialog.show()

        // Ajuste de ancho (90%)
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun loadIncidencias() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaCompleta.clear()
                for (postSnapshot in snapshot.children) {
                    val incidencia = postSnapshot.getValue(Incidencia::class.java)
                    if (incidencia != null) {
                        listaCompleta.add(incidencia)
                    }
                }
                filtrar()
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar error
            }
        })
    }

    private fun setupFilters() {
        binding.searchViewCatalogo.setupWithSearchBar(binding.searchBarCatalogo)
        binding.searchViewCatalogo.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.searchBarCatalogo.setText(s.toString())
                filtrar()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.chipGroupCategorias.setOnCheckedStateChangeListener { _, _ ->
            filtrar()
        }
    }

    private fun filtrar() {
        val texto = binding.searchBarCatalogo.text.toString().trim().lowercase()
        val chipsSeleccionados = binding.chipGroupCategorias.checkedChipIds.map { id ->
            binding.chipGroupCategorias.findViewById<Chip>(id).text.toString().lowercase()
        }

        val listaFiltrada = listaCompleta.filter { incidencia ->
            val coincideTexto = incidencia.tema.lowercase().contains(texto) || 
                               incidencia.descripcion.lowercase().contains(texto)
            
            val coincideTipo = chipsSeleccionados.isEmpty() || 
                              chipsSeleccionados.any { chip -> incidencia.tipo.lowercase().contains(chip) }

            coincideTexto && coincideTipo
        }

        adapter.updateList(listaFiltrada)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}