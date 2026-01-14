package com.example.prueba1integrador

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val txtBienvenida: TextView = findViewById(R.id.txtBienvenida)
        val btnCerrarSesion: Button = findViewById(R.id.btnCerrarSesion)

        val nombre = intent.getStringExtra("USUARIO_LOGUEADO")
        txtBienvenida.text = "¡Bienvenido, $nombre!"

        btnCerrarSesion.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}