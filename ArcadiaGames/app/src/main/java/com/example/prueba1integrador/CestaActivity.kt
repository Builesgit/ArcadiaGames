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

    private val uid = FirebaseAuth.getInstance().currentUser!!.uid

    // Referencia a la cesta del usuario en Firebase
    private val cestaRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance()
            .getReference("cesta")
            .child(uid)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cesta)

        // Views
        rvCart = findViewById(R.id.rvCart)
        tvTotal = findViewById(R.id.tvTotal)
        btnPay = findViewById(R.id.btnPay)

        // Adapter de la cesta
        adapter = CestaAdapter(
            onClickItem = { juego ->
                val intent = Intent(this, DetalleCestaActivity::class.java)
                intent.putExtra("JUEGO_CESTA", juego)
                startActivity(intent)
            },
            onDeleteAt = { pos ->
                eliminarJuegoFirebase(pos)
            }
        )

        rvCart.layoutManager = LinearLayoutManager(this)
        rvCart.adapter = adapter

        // Cargar cesta desde Firebase
        cargarCestaFirebase()

        // 👉 BOTÓN PROCEDER AL PAGO (CORRECTO)
        btnPay.setOnClickListener {

            val intent = Intent(this, PagoActivity::class.java)
            intent.putExtra("PRECIO_TOTAL", calcularTotal())

            startActivity(intent)
        }
    }

    /**
     * Carga la cesta desde Firebase y actualiza la vista
     */
    private fun cargarCestaFirebase() {

        cestaRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                listaCesta.clear()

                for (snap in snapshot.children) {
                    val juego = snap.getValue(Juego::class.java)
                    if (juego != null) {
                        listaCesta.add(juego)
                    }
                }

                adapter.submitList(listaCesta.toList())
                actualizarTotal()
            }

            override fun onCancelled(error: DatabaseError) {
                // Error de lectura (opcional)
            }
        })
    }

    /**
     * Elimina un juego de Firebase
     */
    private fun eliminarJuegoFirebase(pos: Int) {
        val juego = listaCesta[pos]
        cestaRef.child(juego.id).removeValue()
    }

    /**
     * Calcula el total de la cesta
     */
    private fun calcularTotal(): Double {
        return listaCesta.sumOf {
            it.precio
                .replace("€", "")
                .replace(",", ".")
                .trim()
                .toDoubleOrNull() ?: 0.0
        }
    }

    private fun actualizarTotal() {
        tvTotal.text = "Total: €%.2f".format(calcularTotal())
    }
}
