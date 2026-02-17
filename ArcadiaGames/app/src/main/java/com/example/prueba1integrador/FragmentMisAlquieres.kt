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


class FragmentMisAlquileres : Fragment() {

    private lateinit var rvAlquileres: RecyclerView
    private lateinit var adapter: ComprasAdapter
    private val listaAlquileres = mutableListOf<JuegoComprado>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_lista_compras, container, false)

        rvAlquileres = view.findViewById(R.id.rvLista)
        rvAlquileres.layoutManager = LinearLayoutManager(requireContext())

        adapter = ComprasAdapter(listaAlquileres)
        rvAlquileres.adapter = adapter

        cargarAlquileres()

        return view
    }

    private fun cargarAlquileres() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseDatabase.getInstance()
            .getReference("alquileres")
            .child(uid)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    listaAlquileres.clear()

                    for (alquilerSnap in snapshot.children) {

                        val fecha = alquilerSnap.child("fecha")
                            .getValue(Long::class.java) ?: 0L

                        val juego = alquilerSnap.getValue(Juego::class.java)
                        if (juego != null) {
                            listaAlquileres.add(JuegoComprado(juego, fecha))
                        }
                    }

                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}

