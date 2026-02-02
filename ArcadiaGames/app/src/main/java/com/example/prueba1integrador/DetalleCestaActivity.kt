package com.example.prueba1integrador

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetalleCestaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_cesta)

        val juego = intent.getSerializableExtra("JUEGO_CESTA") as? Juego
        if (juego == null) {
            finish()
            return
        }

        val tvNombre = findViewById<TextView>(R.id.tvNombre)
        val tvPrecio = findViewById<TextView>(R.id.tvPrecio)
        val btnComprar = findViewById<Button>(R.id.btnComprar)
        val btnAlquilar = findViewById<Button>(R.id.btnAlquilar)

        tvNombre.text = juego.nombre
        tvPrecio.text = "Precio (base): ${juego.precio}"

        btnComprar.setOnClickListener {
            val i = Intent(this, PagoActivity::class.java)
            i.putExtra("TIPO_OPERACION", "COMPRA")
            i.putExtra("JUEGO_PAGO", juego)
            startActivity(i)
        }

        btnAlquilar.setOnClickListener {
            val i = Intent(this, PagoActivity::class.java)
            i.putExtra("TIPO_OPERACION", "ALQUILER")
            i.putExtra("JUEGO_PAGO", juego)
            startActivity(i)
        }
    }
}
