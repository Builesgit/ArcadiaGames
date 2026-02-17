package com.example.prueba1integrador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.jvm.java


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

                        val fechaCompra = compraSnap.child("fecha")
                            .getValue(Long::class.java) ?: 0L

                        val juegosSnap = compraSnap.child("juegos")

                        for (juegoSnap in juegosSnap.children) {
                            val juego = juegoSnap.getValue(Juego::class.java)
                            if (juego != null) {
                                listaCompras.add(
                                    JuegoComprado(juego, fechaCompra)
                                )
                            }
                        }
                    }

                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
