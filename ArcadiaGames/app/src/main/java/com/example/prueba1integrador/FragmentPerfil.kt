package com.example.prueba1integrador

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.prueba1integrador.databinding.ActivityPerfilBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FragmentPerfil : Fragment() {

    private var _binding: ActivityPerfilBinding? = null
    private val binding get() = _binding!!

    private var userListener: ValueEventListener? = null
    private var userRef: DatabaseReference? = null

    companion object {
        fun newInstance(usuario: String, rol: String): FragmentPerfil {
            val fragment = FragmentPerfil()
            val args = Bundle()
            args.putString("USUARIO", usuario)
            args.putString("ROL", rol)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uidActual = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // ESCUCHA EN TIEMPO REAL: El perfil se actualiza apenas cambia el rol en Firebase
        val userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(uidActual)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return

                val rolActualizado = snapshot.child("rol").getValue(String::class.java) ?: "user"
                val nombreUser = snapshot.child("nombre").getValue(String::class.java) ?: "Desconocido"

                // ¡AQUÍ ESTÁ LA CLAVE! Llamamos a la función para que ejecute la lógica de visibilidad
                actualizarInterfaz(nombreUser, rolActualizado, uidActual, snapshot)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error al escuchar cambios: ${error.message}")
            }
        })

        // Lógica para cerrar sesión
        binding.btnLogout.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun actualizarInterfaz(usuario: String, rol: String, uid: String, snapshot: DataSnapshot) {
        binding.txtNombreUsuario.text = usuario
        binding.txtTituloPerfil.text = if (rol == "admin") "ADMINISTRADOR" else "USUARIO"

        if (rol == "admin") {
            binding.layoutAdmin.visibility = View.VISIBLE
            binding.layoutUsuario.visibility = View.GONE
            binding.btnAbrirDialogoAdmin.visibility = View.GONE

            val esJefe = snapshot.child("esJefe").getValue(Boolean::class.java) ?: false
            binding.btnGestionAdmins.visibility = if (esJefe) View.VISIBLE else View.GONE

            // --- BOTONES ADMIN EXISTENTES ---
            binding.btnCrearProducto.setOnClickListener {
                startActivity(Intent(requireContext(), AnadirProductoActivity::class.java))
            }
            binding.btnInventario.setOnClickListener {
                startActivity(Intent(requireContext(), GestionarInventarioActivity::class.java))
            }
            binding.btnGestionAdmins.setOnClickListener {
                startActivity(Intent(requireContext(), GestionAdminsActivity::class.java))
            }
            binding.btnHistorial.setOnClickListener {
                startActivity(Intent(requireContext(), HistorialActivity::class.java))
            }

            binding.btnGestionarIncidencias.setOnClickListener {
                val fragmentoIncidencias = FragmentMostrarIncidencias()
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    // Reemplazamos el perfil por el listado de incidencias
                    .replace(R.id.main_home_U_fragment, fragmentoIncidencias)
                    .addToBackStack(null)
                    .commit()
            }

        } else {
            binding.layoutAdmin.visibility = View.GONE
            binding.layoutUsuario.visibility = View.VISIBLE
            binding.btnAbrirDialogoAdmin.visibility = View.VISIBLE

            binding.btnMisCompras.setOnClickListener {

                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.main_home_U_fragment, FragmentMisPedidos())
                    .addToBackStack(null)
                    .commit()
            }

            // --- NUEVO: SOPORTE TÉCNICO (USUARIO) ---
            binding.btnSoporteTecnico.setOnClickListener {
                // El usuario va a la Activity para CREAR la incidencia
                val intent = Intent(requireContext(), CrearIncidencia::class.java)
                startActivity(intent)
            }

            binding.btnAbrirDialogoAdmin.setOnClickListener {
                mostrarPopUpAdmin(uid)
            }
        }
    }

    // 2. Nueva función para mostrar el Pop-up
    private fun mostrarPopUpAdmin(uid: String) {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialogo_canjear_admin, null)

        val etCodigo = view.findViewById<android.widget.EditText>(R.id.etCodigoAdminPop)
        val btnConfirmar = view.findViewById<android.widget.Button>(R.id.btnConfirmarCanje)

        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setView(view)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnConfirmar.setOnClickListener {
            val input = etCodigo.text.toString().trim()
            if (input.isEmpty()) {
                Toast.makeText(context, "Escribe un código", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val refCodigos = FirebaseDatabase.getInstance().getReference("codigos_admin")
            refCodigos.get().addOnSuccessListener { snapshot ->
                val codigoReal = snapshot.child("valor").getValue(String::class.java)
                val expira = snapshot.child("expira").getValue(Long::class.java) ?: 0L

                if (input == codigoReal && System.currentTimeMillis() < expira) {
                    val userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(uid)
                    userRef.child("rol").setValue("admin").addOnSuccessListener {
                        Toast.makeText(context, "¡Ahora eres administrador!", Toast.LENGTH_LONG).show()
                        refCodigos.removeValue() // El código se usa una sola vez
                        dialog.dismiss()
                    }
                } else {
                    Toast.makeText(context, "Código inválido o caducado", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Error al verificar código", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}