package com.example.prueba1integrador

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.prueba1integrador.databinding.ActivityPerfilBinding


class PerfilActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPerfilBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val rol = intent.getStringExtra("ROL_USUARIO")
        val nombre = intent.getStringExtra("USUARIO_LOGUEADO")

        binding.txtNombreUsuario.text = nombre

        if (rol?.lowercase() == "admin") {
            binding.layoutAdmin.visibility = View.VISIBLE
            binding.layoutUsuario.visibility = View.GONE

            binding.txtTituloPerfil.text = "Perfil Administrador"
        } else {
            binding.layoutAdmin.visibility = View.GONE
            binding.layoutUsuario.visibility = View.VISIBLE

            binding.txtTituloPerfil.text = "Perfil Usuario"
        }

        binding.btnLogout.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}