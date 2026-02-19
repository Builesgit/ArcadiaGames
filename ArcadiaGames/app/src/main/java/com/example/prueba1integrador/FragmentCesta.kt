package com.example.prueba1integrador

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FragmentCesta : Fragment() {

    private lateinit var rvCart: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var btnPay: Button
    private lateinit var tvCestaVacia: TextView
    private lateinit var adapter: CestaAdapter

    private val listaCesta = mutableListOf<Juego>()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val cestaRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference("cesta").child(uid)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_cesta, container, false)

        rvCart = view.findViewById(R.id.rvCart)
        tvTotal = view.findViewById(R.id.tvTotal)
        btnPay = view.findViewById(R.id.btnPay)
        tvCestaVacia = view.findViewById(R.id.tvCestaVacia)

        setupRecyclerView()
        cargarCestaFirebase()

        btnPay.setOnClickListener {
            if (listaCesta.isNotEmpty()) {

                val intent = Intent(requireContext(), PagoActivity::class.java)
                intent.putExtra("PRECIO_TOTAL", calcularTotal())
                intent.putExtra("LISTA_PRODUCTOS", ArrayList(listaCesta))
                startActivity(intent)

            } else {
                Toast.makeText(context, "La cesta está vacía", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun setupRecyclerView() {
        adapter = CestaAdapter(
            onClickItem = { /* Detalle si quieres */ },
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

                adapter.submitList(listaCesta.toList())
                actualizarTotal()
                actualizarVistaVacia()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun actualizarVistaVacia() {
        if (listaCesta.isEmpty()) {

            tvCestaVacia.visibility = View.VISIBLE
            rvCart.visibility = View.GONE

            // Desactivar botón
            btnPay.isEnabled = false
            btnPay.alpha = 0.5f   // efecto visual más apagado

        } else {

            tvCestaVacia.visibility = View.GONE
            rvCart.visibility = View.VISIBLE

            // Activar botón
            btnPay.isEnabled = true
            btnPay.alpha = 1.0f
        }
    }

    private fun eliminarJuegoFirebase(pos: Int) {
        val juego = listaCesta[pos]
        cestaRef.child(juego.id).removeValue()
    }

    private fun calcularTotal(): Double {
        var acumulador = 0.0

        for (item in listaCesta) {
            val limpio = item.precio
                .replace("€", "")
                .replace(",", ".")
                .trim()

            val valor = limpio.toDoubleOrNull()
            if (valor != null) {
                acumulador += valor
            }
        }

        return acumulador
    }

    private fun actualizarTotal() {
        val total = calcularTotal()
        tvTotal.text =  String.format("%.2f", total) + "€"
    }
}
