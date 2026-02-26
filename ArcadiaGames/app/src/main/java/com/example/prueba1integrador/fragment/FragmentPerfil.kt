package com.example.prueba1integrador.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.prueba1integrador.R
// IMPORTS ESPECÍFICOS DE LAS ACTIVIDADES
import com.example.prueba1integrador.activity.AnadirProductoActivity
import com.example.prueba1integrador.activity.GestionAdminsActivity
import com.example.prueba1integrador.activity.GestionarInventarioActivity
import com.example.prueba1integrador.activity.HistorialActivity
import com.example.prueba1integrador.activity.MainActivity
import com.example.prueba1integrador.fragment.FragmentMisPedidos
import com.example.prueba1integrador.databinding.ActivityPerfilBinding
import com.example.prueba1integrador.model.CrearIncidencia
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FragmentPerfil : Fragment() {

    private var _binding: ActivityPerfilBinding? = null
    private val binding get() = _binding!!

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

        if (uidActual.isNotEmpty()) {
            val userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(uidActual)
            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded || _binding == null) return
                    val rolActualizado = snapshot.child("rol").getValue(String::class.java) ?: "user"
                    val nombreUser = snapshot.child("nombre").getValue(String::class.java) ?: "Desconocido"
                    actualizarInterfaz(nombreUser, rolActualizado, uidActual, snapshot)
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Error: ${error.message}")
                }
            })
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
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

            binding.btnEstadisticasPerfil.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_home_U_fragment, FragmentEstadisticas())
                    .addToBackStack(null)
                    .commit()
            }

            binding.btnGestionarIncidencias.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_home_U_fragment, FragmentMostrarIncidencias())
                    .addToBackStack(null)
                    .commit()
            }

        } else {
            binding.layoutAdmin.visibility = View.GONE
            binding.layoutUsuario.visibility = View.VISIBLE
            binding.btnAbrirDialogoAdmin.visibility = View.VISIBLE

            // FUNCIONALIDAD BOTÓN NORMAL PARA MIS COMPRAS
            binding.btnMisCompras.setOnClickListener {
                val intent = Intent(requireContext(), FragmentMisPedidos::class.java)
                startActivity(intent)
            }

            binding.btnSoporteTecnico.setOnClickListener {
                startActivity(Intent(requireContext(), CrearIncidencia::class.java))
            }

            binding.btnAbrirDialogoAdmin.setOnClickListener {
                mostrarPopUpAdmin(uid)
            }
        }
    }

    private fun mostrarPopUpAdmin(uid: String) {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialogo_canjear_admin, null)
        val etCodigo = view.findViewById<EditText>(R.id.etCodigoAdminPop)
        val btnConfirmar = view.findViewById<Button>(R.id.btnConfirmarCanje)

        val dialog = AlertDialog.Builder(requireContext()).setView(view).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnConfirmar.setOnClickListener {
            val input = etCodigo.text.toString().trim()
            if (input.isEmpty()) return@setOnClickListener

            FirebaseDatabase.getInstance().getReference("codigos_admin").get().addOnSuccessListener { snap ->
                val codigoReal = snap.child("valor").getValue(String::class.java)
                val expira = snap.child("expira").getValue(Long::class.java) ?: 0L

                if (input == codigoReal && System.currentTimeMillis() < expira) {
                    FirebaseDatabase.getInstance().getReference("usuarios").child(uid).child("rol").setValue("admin")
                    snap.ref.removeValue()
                    dialog.dismiss()
                } else {
                    Toast.makeText(context, "Código inválido", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}