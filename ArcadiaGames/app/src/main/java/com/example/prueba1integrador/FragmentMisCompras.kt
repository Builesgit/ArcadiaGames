package com.example.prueba1integrador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FragmentMisCompras : Fragment() {

    private lateinit var rvCompras: RecyclerView
    private lateinit var adapter: ComprasAdapter
    private val listaCompras = mutableListOf<JuegoComprado>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_lista_compras, container, false)

        rvCompras = view.findViewById(R.id.rvLista)
        rvCompras.layoutManager = LinearLayoutManager(requireContext())

        adapter = ComprasAdapter(listaCompras)
        rvCompras.adapter = adapter

        cargarCompras()

        return view
    }

    private fun cargarCompras() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseDatabase.getInstance()
            .getReference("compras")
            .child(uid)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    listaCompras.clear()

                    for (compraSnap in snapshot.children) {
                        val fechaCompra = compraSnap.child("fecha").getValue(Long::class.java)
                            ?: compraSnap.child("fechaCompra").getValue(Long::class.java)
                            ?: compraSnap.child("timestamp").getValue(Long::class.java)
                            ?: 0L

                        val juegosSnap = when {
                            compraSnap.child("juegos").exists() -> compraSnap.child("juegos")
                            compraSnap.child("productos").exists() -> compraSnap.child("productos")
                            compraSnap.child("items").exists() -> compraSnap.child("items")
                            compraSnap.child("lista").exists() -> compraSnap.child("lista")
                            else -> null
                        }

                        if (juegosSnap != null) {
                            for (item in juegosSnap.children) {
                                val juego = item.child("juego").getValue(Juego::class.java)
                                    ?: item.getValue(Juego::class.java)
                                if (juego != null) {
                                    listaCompras.add(JuegoComprado(juego = juego, fechaCompra = fechaCompra, esAlquiler = false))
                                }
                            }
                        } else {
                            val juego = compraSnap.child("juego").getValue(Juego::class.java)
                                ?: compraSnap.getValue(Juego::class.java)
                            if (juego != null) {
                                listaCompras.add(JuegoComprado(juego = juego, fechaCompra = fechaCompra, esAlquiler = false))
                            }
                        }
                    }

                    listaCompras.sortByDescending { it.fechaCompra ?: 0L }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}