package com.example.prueba1integrador

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.prueba1integrador.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // Declaración de la variable para la animación de cambio de pantallas
    private lateinit var binding: ActivityMainBinding

    // Declaración de la variable para la el login funcional
    private lateinit var edtUsuario: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Solo una vez el binding e inflar la vista
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Configurar UI de la animación
        setupInitialState()

        // 2. Cargar el GIF
        binding.root.post {
            loadGifOptimized()
        }

        // 3. Ejecutar transición
        binding.root.postDelayed({
            executeFlipTransition()
        }, 6000)

        // 4. Inicializar variables del login usando BINDING
        edtUsuario = binding.edtUsuario
        edtPassword = binding.edtPassword
        btnLogin = binding.btnLogin

        btnLogin.setOnClickListener {
            val user = edtUsuario.text.toString()
            val pass = edtPassword.text.toString()

            if (user.isNotEmpty() && pass.isNotEmpty()) {
                validarUsuario("http://10.0.2.2/arcadia_games_db/validar_usuario.php")
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // METODOS PARA LA ANIMACIÓN DE CAMBIO DE PANTALLA

    private fun setupInitialState() {
        val scale = resources.displayMetrics.density
        val distance = 8000 * scale

        binding.initialScreenLayout.cameraDistance = distance
        binding.loginScreenLayout.cameraDistance = distance
        binding.loginScreenLayout.visibility = View.GONE
    }

    private fun loadGifOptimized() {
        Glide.with(this)
            .asGif()
            .load(R.drawable.prueba3)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(binding.logoImageView)
    }

    private fun executeFlipTransition() {
        val duration = 600L
        val interpolator = AccelerateDecelerateInterpolator()

        binding.initialScreenLayout.animate()
            .rotationY(90f)
            .setDuration(duration)
            .setInterpolator(interpolator)
            .withEndAction {
                binding.initialScreenLayout.visibility = View.GONE

                binding.loginScreenLayout.apply {
                    rotationY = -90f
                    visibility = View.VISIBLE
                    animate()
                        .rotationY(0f)
                        .setDuration(duration)
                        .setInterpolator(interpolator)
                        .start()
                }
            }
            .start()
    }

    // METODOS PARA EL LOGIN CON BASE DE DATOS

    private fun validarUsuario(url: String) {
        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                if (response.isNotEmpty() && !response.contains("no_existe")) {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.putExtra("USUARIO_LOGUEADO", edtUsuario.text.toString())
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                // Esto te dirá el error real en un Toast
                Toast.makeText(this, "Error: ${error.message ?: "Conexión fallida"}", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val parametros = HashMap<String, String>()
                parametros["usuario"] = edtUsuario.text.toString()
                parametros["password"] = edtPassword.text.toString()
                return parametros
            }
        }
        Volley.newRequestQueue(this).add(stringRequest)
    }
}