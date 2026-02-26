package com.example.prueba1integrador.activity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prueba1integrador.adapter.AdminAdapter
import com.example.prueba1integrador.model.Usuario
import com.example.prueba1integrador.databinding.ActivityGestionAdminsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class GestionAdminsActivity : BaseActivity() {

    private lateinit var binding: ActivityGestionAdminsBinding
    private val dbRef = FirebaseDatabase.getInstance().getReference()
    private val uidJefe = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestionAdminsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Lógica de generación de código temporal
        binding.btnGenerarNuevoCodigo.setOnClickListener {
            generarCodigoTemporal()
        }

        setupRecyclerView()
        escucharCodigoActivo()
        cargarListaAdministradores()
    }

    private fun generarCodigoTemporal() {
        val nuevoCodigo = (1000..9999).random().toString()
        val tiempoExpiracion = System.currentTimeMillis() + (5 * 60 * 1000)

        val datosCodigo = mapOf(
            "valor" to nuevoCodigo,
            "expira" to tiempoExpiracion
        )

        dbRef.child("codigos_admin").setValue(datosCodigo).addOnSuccessListener {
            Toast.makeText(this, "Código $nuevoCodigo generado", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Log.e("FirebaseError", "Error: ${e.message}")
            Toast.makeText(this, "Error de permisos de Jefe", Toast.LENGTH_LONG).show()
        }
    }

    private fun escucharCodigoActivo() {
        dbRef.child("codigos_admin").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val valor = snapshot.child("valor").getValue(String::class.java)
                val expira = snapshot.child("expira").getValue(Long::class.java) ?: 0L

                if (valor != null && System.currentTimeMillis() < expira) {
                    binding.tvCodigoActivo.text = "CÓDIGO ACTIVO: $valor"
                    binding.tvCodigoActivo.setTextColor(Color.YELLOW)
                } else {
                    binding.tvCodigoActivo.text = "Ningún código activo"
                    binding.tvCodigoActivo.setTextColor(Color.WHITE)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupRecyclerView() {
        binding.rvAdmins.layoutManager = LinearLayoutManager(this)
    }

    private fun cargarListaAdministradores() {
        dbRef.child("usuarios").orderByChild("rol").equalTo("admin")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val listaAdmins = mutableListOf<Usuario>()

                    for (data in snapshot.children) {
                        val rol = data.child("rol").getValue(String::class.java)

                        if (rol == "admin" && data.key != uidJefe) {
                            val admin = Usuario(
                                id = data.key ?: "",
                                nombre = data.child("nombre").getValue(String::class.java)
                                    ?: "Sin nombre",
                                correo = data.child("correo").getValue(String::class.java)
                                    ?: "Sin correo",
                                rol = "admin"
                            )
                            listaAdmins.add(admin)
                        }
                    }

                    if (listaAdmins.isEmpty()) {
                        binding.rvAdmins.visibility = View.GONE
                        binding.tvListaVacia.visibility = View.VISIBLE
                    } else {
                        binding.rvAdmins.visibility = View.VISIBLE
                        binding.tvListaVacia.visibility = View.GONE

                        // Vinculamos el adaptador (asegúrate de que tvEmailAdminItem esté en item_admin.xml)
                        binding.rvAdmins.adapter = AdminAdapter(listaAdmins) { adminADegradar ->
                            dbRef.child("usuarios").child(adminADegradar.id).child("rol")
                                .setValue("user")
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this@GestionAdminsActivity,
                                        "Rango retirado",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", error.message)
                }
            })
    }
}