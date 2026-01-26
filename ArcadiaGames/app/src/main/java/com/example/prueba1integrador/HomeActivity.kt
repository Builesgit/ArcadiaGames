package com.example.prueba1integrador

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.finishAffinity
import com.example.prueba1integrador.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recuperamos los datos que enviamos desde MainActivity
        val usuario = intent.getStringExtra("USUARIO_LOGUEADO")
        val rol = intent.getStringExtra("ROL_USUARIO")

        binding.btnIrAPerfil.setOnClickListener {
            val intentPerfil = Intent(this, PerfilActivity::class.java)
            intentPerfil.putExtra("USUARIO_LOGUEADO", usuario)
            intentPerfil.putExtra("ROL_USUARIO", rol)
            startActivity(intentPerfil)
        }

        binding.btnCerrarSesion.setOnClickListener {
            // Esto cierra todas las actividades de la aplicación y sale por completo
            finishAffinity()

            // Opcional: Si quieres asegurar que el proceso se detenga totalmente (uso extremo)
            // System.exit(0)
        }
    }
}