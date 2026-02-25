package com.example.prueba1integrador

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class DetalleCestaActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_cesta)

        val juego = intent.getSerializableExtra("JUEGO_CESTA") as? Juego ?: return

        findViewById<TextView>(R.id.tvNombre).text = juego.nombre
        findViewById<TextView>(R.id.tvPrecio).text = "Precio: ${juego.precio}"

        findViewById<Button>(R.id.btnComprar).setOnClickListener {
            val i = Intent(this, PagoActivity::class.java)

            // Pasamos el juego en una lista para PagoActivity
            val lista = arrayListOf(juego)
            i.putExtra("LISTA_PRODUCTOS", lista)

            // ENVIAR EL PRECIO CON LA CLAVE CORRECTA
            i.putExtra("TOTAL_PAGO", juego.precio)

            startActivity(i)
        }

        findViewById<Button>(R.id.btnAlquilar).setOnClickListener {
            val i = Intent(this, AlquilarJuegoActivity::class.java)
            i.putExtra("JUEGO", juego)
            startActivity(i)
        }
    }
}