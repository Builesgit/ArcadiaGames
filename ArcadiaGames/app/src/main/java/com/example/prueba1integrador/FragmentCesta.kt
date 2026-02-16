package com.example.prueba1integrador

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FragmentCesta : Fragment() {

    private lateinit var rvCart: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var btnPay: Button
    private lateinit var adapter: CestaAdapter
    private val listaCesta = mutableListOf<Juego>()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val cestaRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference("cesta").child(uid)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_cesta, container, false)

        rvCart = view.findViewById(R.id.rvCart)
        tvTotal = view.findViewById(R.id.tvTotal)
        btnPay = view.findViewById(R.id.btnPay)

        setupRecyclerView()
        cargarCestaFirebase()

        btnPay.setOnClickListener {
            if (listaCesta.isNotEmpty()) {
                val intent = Intent(requireContext(), PagoActivity::class.java)
                intent.putExtra("PRECIO_TOTAL", calcularTotal())

                // Convertimos la lista a ArrayList para pasarla por el Intent
                val arrayListJuegos = ArrayList<Juego>()
                for (j in listaCesta) {
                    arrayListJuegos.add(j)
                }
                intent.putExtra("LISTA_PRODUCTOS", arrayListJuegos)
                startActivity(intent)
            }
        }

        return view
    }

    private fun setupRecyclerView() {
        adapter = CestaAdapter(
            onClickItem = { /* Detalle si es necesario */ },
            onDeleteAt = { pos -> eliminarJuegoFirebase(pos) }
        )
        rvCart.layoutManager = LinearLayoutManager(context)
        rvCart.adapter = adapter
    }

    private fun cargarCestaFirebase() {
        cestaRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaCesta.clear()
                for (snap in snapshot.children) {
                    val juego = snap.getValue(Juego::class.java)
                    if (juego != null) listaCesta.add(juego)
                }
                adapter.submitList(listaCesta)
                actualizarTotal()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun eliminarJuegoFirebase(pos: Int) {
        val juego = listaCesta[pos]
        cestaRef.child(juego.id).removeValue()
    }

    private fun calcularTotal(): Double {
        var acumulador = 0.0
        for (i in 0 until listaCesta.size) {
            val item = listaCesta[i]
            val limpio = item.precio.replace("€", "").replace(",", ".").trim()
            val valor = limpio.toDoubleOrNull()
            if (valor != null) {
                acumulador += valor
            }
        }
        return acumulador
    }

    private fun actualizarTotal() {
        val total = calcularTotal()
        tvTotal.text = "Total: €" + String.format("%.2f", total)
    }
}