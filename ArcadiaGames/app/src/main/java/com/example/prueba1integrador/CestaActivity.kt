package com.example.prueba1integrador

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CestaActivity : AppCompatActivity() {

    private lateinit var rvCart: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var btnPay: Button

    private lateinit var adapter: CestaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cesta)

        rvCart = findViewById(R.id.rvCart)
        tvTotal = findViewById(R.id.tvTotal)
        btnPay = findViewById(R.id.btnPay)

        adapter = CestaAdapter(
            onClickItem = { juego ->
                val i = Intent(this, DetalleCestaActivity::class.java)
                i.putExtra("JUEGO_CESTA", juego) // Juego es Serializable en tu proyecto ✅
                startActivity(i)
            },
            onDeleteAt = { pos ->
                CestaManager.removeAt(pos)
                adapter.submitList(CestaManager.items)
                actualizarTotal()
            }
        )

        rvCart.layoutManager = LinearLayoutManager(this)
        rvCart.adapter = adapter

        adapter.submitList(CestaManager.items)
        actualizarTotal()

        // Si quieres que "Proceder al pago" pague el total directo (opcional)
        btnPay.setOnClickListener {
            val i = Intent(this, PagoActivity::class.java)
            i.putExtra("PRECIO_TOTAL", CestaManager.total())
            startActivity(i)
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.submitList(CestaManager.items)
        actualizarTotal()
    }

    private fun actualizarTotal() {
        tvTotal.text = "Total: €%.2f".format(CestaManager.total())
    }
}
