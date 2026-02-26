package com.example.prueba1integrador.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prueba1integrador.adapter.ComprasAdapter
import com.example.prueba1integrador.model.Juego
import com.example.prueba1integrador.model.JuegoComprado
import com.example.prueba1integrador.R
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FragmentMisPedidos : Fragment(R.layout.fragment_mis_pedidos) {

    private lateinit var rvCompras: RecyclerView
    private lateinit var adapter: ComprasAdapter
    private lateinit var tabLayout: TabLayout

    private val lista = mutableListOf<JuegoComprado>()

    private var comprasListener: ValueEventListener? = null
    private var alquileresListener: ValueEventListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvCompras = view.findViewById(R.id.rvCompras)
        tabLayout = view.findViewById(R.id.tabLayout)

        rvCompras.layoutManager = LinearLayoutManager(requireContext())
        adapter = ComprasAdapter(lista)
        rvCompras.adapter = adapter

        tabLayout.removeAllTabs()
        tabLayout.addTab(tabLayout.newTab().setText("MIS COMPRAS"))
        tabLayout.addTab(tabLayout.newTab().setText("MIS ALQUILERES"))

        cargarCompras()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) cargarCompras()
                else cargarAlquileres()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        comprasListener?.let {
            FirebaseDatabase.getInstance()
                .getReference("compras")
                .child(uid)
                .removeEventListener(it)
        }

        alquileresListener?.let {
            FirebaseDatabase.getInstance()
                .getReference("alquileres")
                .child(uid)
                .removeEventListener(it)
        }
    }

    // =========================
    // COMPRAS
    // =========================
    private fun cargarCompras() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("compras").child(uid)

        alquileresListener?.let {
            FirebaseDatabase.getInstance()
                .getReference("alquileres")
                .child(uid)
                .removeEventListener(it)
        }

        comprasListener?.let { ref.removeEventListener(it) }

        comprasListener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                lista.clear()

                for (compraSnap in snapshot.children) {

                    val fechaCompra = compraSnap.child("fecha")
                        .getValue(Long::class.java) ?: 0L

                    val juegosSnap = compraSnap.child("juegos")
                    if (!juegosSnap.exists()) continue

                    for (juegoSnap in juegosSnap.children) {

                        val juego = juegoSnap.getValue(Juego::class.java)

                        if (juego != null) {
                            lista.add(
                                JuegoComprado(
                                    juego = juego,
                                    fechaCompra = fechaCompra,
                                    esAlquiler = false
                                )
                            )
                        }
                    }
                }

                lista.sortByDescending { it.fechaCompra ?: 0L }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        ref.addValueEventListener(comprasListener!!)
    }

    // =========================
    // ALQUILERES
    // =========================
    private fun cargarAlquileres() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance()
            .getReference("alquileres")
            .child(uid)

        // Quitamos listener de compras
        comprasListener?.let {
            FirebaseDatabase.getInstance()
                .getReference("compras")
                .child(uid)
                .removeEventListener(it)
        }

        lista.clear()

        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (!isAdded) return

                lista.clear()

                for (alqSnap in snapshot.children) {

                    val nombre = alqSnap.child("nombre")
                        .getValue(String::class.java) ?: continue

                    val plataforma = alqSnap.child("plataforma")
                        .getValue(String::class.java) ?: "Desconocida"

                    val precio = alqSnap.child("precio")
                        .getValue(String::class.java) ?: "0"

                    val imagenUrl = alqSnap.child("imagenUrl")
                        .getValue(String::class.java) ?: ""

                    val fechaInicio = alqSnap.child("fechaInicio")
                        .getValue(Long::class.java)

                    val fechaFin = alqSnap.child("fechaFin")
                        .getValue(Long::class.java)

                    val juego = Juego(
                        nombre = nombre,
                        plataforma = plataforma,
                        precio = precio,
                        imagenUrl = imagenUrl,
                        imagenResId = 0
                    )

                    lista.add(
                        JuegoComprado(
                            juego = juego,
                            fechaInicioMillis = fechaInicio,
                            fechaFinMillis = fechaFin,
                            esAlquiler = true
                        )
                    )
                }

                lista.sortByDescending { it.fechaInicioMillis ?: 0L }

                if (isAdded) {
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}