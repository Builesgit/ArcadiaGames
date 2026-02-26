package com.example.prueba1integrador.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.prueba1integrador.model.Juego
import com.example.prueba1integrador.R

class DetalleCestaActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_cesta)

        val juego = intent.getSerializableExtra("JUEGO_CESTA") as? Juego ?: return

        findViewById<TextView>(R.id.tvNombre).text = juego.nombre
        findViewById<TextView>(R.id.tvPrecio).text = "Precio: ${juego.precio}"

        findViewById<Button>(R.id.btnComprar).setOnClickListener {
            val i = Intent(this, PagoActivity::class.java)
            val precioNum = juego.precio.replace("€", "").replace(",", ".").replace(" ", "").trim().toDoubleOrNull() ?: 0.0

            i.putExtra("LISTA_PRODUCTOS", arrayListOf(juego))
            i.putExtra("PRECIO_TOTAL", precioNum)
            startActivity(i)
        }

        findViewById<Button>(R.id.btnAlquilar).setOnClickListener {
            val i = Intent(this, AlquilarJuegoActivity::class.java)
            i.putExtra("JUEGO", juego)
            startActivity(i)
        }
    }
}