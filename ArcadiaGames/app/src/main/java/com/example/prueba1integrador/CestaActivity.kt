package com.example.prueba1integrador

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CestaActivity : AppCompatActivity() {

    private lateinit var rvCart: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var btnPay: Button
    private lateinit var adapter: CestaAdapter
    private val listaCesta = mutableListOf<Juego>()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val cestaRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference("cesta").child(uid)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cesta)

        rvCart = findViewById(R.id.rvCart)
        tvTotal = findViewById(R.id.tvTotal)
        btnPay = findViewById(R.id.btnPay)

        adapter = CestaAdapter(
            onClickItem = { /* Detalle si fuera necesario */ },
            onDeleteAt = { pos -> eliminarJuegoFirebase(pos) }
        )

        rvCart.layoutManager = LinearLayoutManager(this)
        rvCart.adapter = adapter

        cargarCestaFirebase()

        btnPay.setOnClickListener {
            if (listaCesta.isNotEmpty()) {
                val intent = Intent(this, PagoActivity::class.java)
                intent.putExtra("PRECIO_TOTAL", calcularTotal())
                startActivity(intent)
            }
        }
    }

    private fun cargarCestaFirebase() {
        cestaRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaCesta.clear()
                for (snap in snapshot.children) {
                    snap.getValue(Juego::class.java)?.let { listaCesta.add(it) }
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
        return listaCesta.sumOf {
            it.precio.replace("€", "").replace(",", ".").replace("[^0-9.]".toRegex(), "").trim().toDoubleOrNull() ?: 0.0
        }
    }

    private fun actualizarTotal() {
        tvTotal.text = "Total: €%.2f".format(calcularTotal())
    }
}