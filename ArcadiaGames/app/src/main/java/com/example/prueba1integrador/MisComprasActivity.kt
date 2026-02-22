package com.example.prueba1integrador

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlin.jvm.java

class MisComprasActivity : BaseActivity() {

    private lateinit var rvCompras: RecyclerView
    private lateinit var adapter: ComprasAdapter
    private lateinit var tabLayout: TabLayout

    private val listaCompras = mutableListOf<JuegoComprado>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_compras)

        rvCompras = findViewById(R.id.rvCompras)
        tabLayout = findViewById(R.id.tabLayout)

        rvCompras.layoutManager = LinearLayoutManager(this)

        adapter = ComprasAdapter(listaCompras)
        rvCompras.adapter = adapter

        // Tabs
        tabLayout.addTab(tabLayout.newTab().setText("MIS COMPRAS"))
        tabLayout.addTab(tabLayout.newTab().setText("MIS ALQUILERES"))

        // Cargar compras por defecto
        cargarCompras()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) {
                    cargarCompras()
                } else {
                    cargarAlquileres()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun cargarCompras() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseDatabase.getInstance()
            .getReference("compras")
            .child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    listaCompras.clear()

                    for (compraSnap in snapshot.children) {

                        val fechaCompra = compraSnap.child("fecha")
                            .getValue(Long::class.java)

                        val juegosSnap = compraSnap.child("juegos")

                        for (juegoSnap in juegosSnap.children) {
                            val juego = juegoSnap.getValue(Juego::class.java)
                            if (juego != null && fechaCompra != null) {

                                listaCompras.add(
                                    JuegoComprado(
                                        juego = juego,
                                        fechaCompra = fechaCompra,
                                        esAlquiler = false
                                    )
                                )
                            }
                        }
                    }

                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@MisComprasActivity,
                        "Error cargando compras",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun cargarAlquileres() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseDatabase.getInstance()
            .getReference("alquileres")
            .child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    listaCompras.clear()

                    for (alquilerSnap in snapshot.children) {

                        val fechaInicio = alquilerSnap.child("fechaInicio")
                            .getValue(Long::class.java)

                        val fechaFin = alquilerSnap.child("fechaFin")
                            .getValue(Long::class.java)

                        val juego = alquilerSnap.getValue(Juego::class.java)

                        if (juego != null && fechaInicio != null && fechaFin != null) {

                            listaCompras.add(
                                JuegoComprado(
                                    juego = juego,
                                    fechaInicio = fechaInicio,
                                    fechaFin = fechaFin,
                                    esAlquiler = true
                                )
                            )
                        }
                    }

                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@MisComprasActivity,
                        "Error cargando alquileres",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
