package com.example.prueba1integrador.fragment

import android.R
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
import com.example.prueba1integrador.model.Incidencia
import com.example.prueba1integrador.adapter.IncidenciaAdapter
import com.example.prueba1integrador.databinding.DialogoDetalleIncidenciaBinding
import com.example.prueba1integrador.databinding.FragmentMostrarIncidenciaBinding
import com.google.android.material.chip.Chip
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FragmentMostrarIncidencias : Fragment() {

    private var _binding: FragmentMostrarIncidenciaBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: IncidenciaAdapter
    private val listaCompleta: MutableList<Incidencia> = mutableListOf()

    private lateinit var dbPendientes: DatabaseReference
    private lateinit var dbResueltas: DatabaseReference
    private lateinit var rootRef: DatabaseReference

    private var listenerActual: ValueEventListener? = null
    private var refActual: DatabaseReference? = null

    private var mostrandoResueltas = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMostrarIncidenciaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rootRef = FirebaseDatabase.getInstance().reference
        dbPendientes = rootRef.child("incidencias")                     // pendientes = incidencias/{id}
        dbResueltas = rootRef.child("incidencias").child("resueltas")   // resueltas = incidencias/resueltas/{id}

        setupRecyclerView()
        setupFilters()
        setupFabResueltas()

        // Vista por defecto: pendientes
        cargarDesde(dbPendientes)
    }

    private fun setupRecyclerView() {
        binding.rvCatalogoCompleto.layoutManager = LinearLayoutManager(requireContext())
        adapter = IncidenciaAdapter(emptyList()) { incidencia ->
            mostrarDetalleIncidencia(incidencia)
        }
        binding.rvCatalogoCompleto.adapter = adapter
    }

    private fun setupFabResueltas() {
        binding.fabVerResueltas.setOnClickListener {
            mostrandoResueltas = !mostrandoResueltas

            if (mostrandoResueltas) {
                Toast.makeText(requireContext(), "Mostrando incidencias resueltas", Toast.LENGTH_SHORT).show()
                cargarDesde(dbResueltas)
            } else {
                Toast.makeText(requireContext(), "Mostrando incidencias pendientes", Toast.LENGTH_SHORT).show()
                cargarDesde(dbPendientes)
            }
        }
    }

    private fun mostrarDetalleIncidencia(incidencia: Incidencia) {
        val dialog = Dialog(requireContext())
        val dialogBinding = DialogoDetalleIncidenciaBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawableResource(R.color.transparent)

        dialogBinding.tvDetalleTema.text = incidencia.tema.uppercase()
        dialogBinding.tvDetalleDescripcion.text = incidencia.descripcion
        dialogBinding.tvDetalleInfo.text = incidencia.infoAdicional
        dialogBinding.tvDetalleUsuario.text = "De: ${incidencia.usuarioEmail}"

        // Si estás viendo resueltas, ocultamos el botón
        dialogBinding.btnFinalizarIncidencia.visibility =
            if (mostrandoResueltas) View.GONE else View.VISIBLE

        dialogBinding.btnFinalizarIncidencia.setOnClickListener {

            // ✅ key real de Firebase (siempre es la correcta)
            val keyReal = incidencia.id?.trim()
            if (keyReal.isNullOrEmpty()) {
                Toast.makeText(context, "No se pudo resolver: ID vacío.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fecha = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

            val incidenciaResueltaMap = hashMapOf<String, Any?>(
                "id" to keyReal,
                "tema" to incidencia.tema,
                "descripcion" to incidencia.descripcion,
                "infoAdicional" to incidencia.infoAdicional,
                "usuarioEmail" to incidencia.usuarioEmail,
                "tipo" to incidencia.tipo,
                "estado" to "resuelta",
                "fechaResuelta" to fecha
            )

            // ✅ ATÓMICO: archiva y borra en una sola operación
            val updates = hashMapOf<String, Any?>(
                "incidencias/resueltas/$keyReal" to incidenciaResueltaMap,
                "incidencias/$keyReal" to null
            )

            rootRef.updateChildren(updates)
                .addOnSuccessListener {
                    Toast.makeText(context, "Reporte archivado como resuelto", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()

                    // ✅ refrescar vista actual
                    refActual?.let { cargarDesde(it) }
                }
                .addOnFailureListener { e ->
                    // ✅ muestra el error real (si es "Permission denied", lo verás aquí)
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
        }

        dialog.show()
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun setupFilters() {
        // Filtro por texto
        binding.searchBarCatalogo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrar()
            }
        })

        // Filtro por chips
        binding.chipGroupFiltros.setOnCheckedStateChangeListener { _, _ ->
            filtrar()
        }
    }

    private fun cargarDesde(ref: DatabaseReference) {
        // quitar listener anterior
        listenerActual?.let { old ->
            refActual?.removeEventListener(old)
        }
        refActual = ref

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return

                listaCompleta.clear()

                for (postSnapshot in snapshot.children) {
                    // Si leemos pendientes desde "incidencias", ignoramos el nodo "resueltas"
                    if (!mostrandoResueltas && postSnapshot.key == "resueltas") continue

                    val incidencia = postSnapshot.getValue(Incidencia::class.java) ?: continue

                    // Guardar la key real (obligatorio para borrar / archivar)
                    incidencia.id = postSnapshot.key

                    listaCompleta.add(incidencia)
                }

                filtrar()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error Firebase: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }

        listenerActual = listener
        ref.addValueEventListener(listener)
    }

    private fun filtrar() {
        val texto = binding.searchBarCatalogo.text.toString().trim().lowercase()

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
        listenerActual?.let { old ->
            refActual?.removeEventListener(old)
        }
        _binding = null
    }
}