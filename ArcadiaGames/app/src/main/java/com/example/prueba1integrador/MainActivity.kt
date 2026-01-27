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
// import com.bumptech.glide.Glide
// import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.prueba1integrador.databinding.ActivityMainBinding

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import android.util.Log

import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var edtUsuario: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = Firebase.database
        val myRef = database.getReference("test_conexion")

        myRef.setValue("Hola desde Arcadia Games!")
            .addOnSuccessListener {
                // Esto saldrá en el Logcat de Android Studio si funciona
                Log.d("FirebaseTest", "¡Dato enviado correctamente!")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseTest", "Error al enviar dato", e)
            }

        binding = ActivityMainBinding.inflate(layoutInflater)
            // layoutInflater: lee los archivos XML y los convierte en objetos de Kotlin
            // .inflate(): Toma el XML y lo convierte en un objeto 3D en la memoria del teléfono
            // binding: te da acceso y control directo de todos los elementos del diseño

        setContentView(binding.root) // setContentView: muestra la pantalla al usuario

        // CONFIGURAR EL ESTADO INICIAL DE LA TRANSICION
        setupInitialState()

        // CARGAR EL GIF
        /* binding.root.post {
            loadGifOptimized()
        } */

        // CARGAR IMG FONDO LOGO
        binding.logoImageView.setImageResource(R.drawable.fondo_con_logo)

        // EJECUTAR LA TRANSICIÓN
        binding.root.postDelayed({
            executeFlipTransition() // Ejecuta la animación de cambio de pantalla durante 6s
        }, 6000)

        // INICIALIZAR VARIABLES LOGIN
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

    private fun setupInitialState() {
        val scale = resources.displayMetrics.density
        val distance = 8000 * scale

        // cameraDistance: hace que la animación sea más realista aplicandole una distancia a la hora de la rotación
        binding.initialScreenLayout.cameraDistance = distance
        binding.loginScreenLayout.cameraDistance = distance

        binding.loginScreenLayout.visibility = View.GONE // Desaparece la pantalla de inicio
    }

    /* private fun loadGifOptimized() {
        Glide.with(this)
            .asGif()
            .load(R.drawable.gif_inicio)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(binding.logoImageView)
    } */

    private fun executeFlipTransition() {
        val duration = 600L
        val interpolator = AccelerateDecelerateInterpolator()

        // PARTE 1: La pantalla inicial gira 90 grados (se pone de lado)
        binding.initialScreenLayout.animate()
            .rotationY(90f)
            .setDuration(duration)
            .setInterpolator(interpolator)
            .withEndAction {
                binding.initialScreenLayout.visibility = View.GONE // Desaparece la pantalla inicial

                // PARTE 2: El Login aparece desde -90 y gira a 0 (se pone de frente)
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

    private fun validarUsuario(url: String) {
        val stringRequest = object : StringRequest(
            Request.Method.POST, url, // POST: forma de envió seguro para que la contraseña no viaje en la URL
            { response ->
                println("DEBUG_SERVER_RESPONSE: $response")

                if (response.isNotEmpty() && !response.contains("no_existe")) {
                    try {
                        val jsonResponse = JSONObject(response)

                        // IMPORTANTE: Verifica que estas claves coincidan con el JSON del log
                        val rol = jsonResponse.getString("rol")
                        val usuario = jsonResponse.getString("usuario")

                        val intent = Intent(this, HomeActivity::class.java)
                        intent.putExtra("ROL_USUARIO", rol)
                        intent.putExtra("USUARIO_LOGUEADO", usuario)

                        startActivity(intent)
                        finish()
                    } catch (e: Exception) {
                        // Imprime el error real en la consola para saber qué falló
                        e.printStackTrace()
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                }
            },
            { error -> // Si falla el internet o el servidor

                Toast.makeText(this, "Error: ${error.message ?: "Conexión fallida"}", Toast.LENGTH_LONG).show()
            }
        ) {
            // getParams: obtiene los datos que se van a enviar al servidor mediante $_POST['usuario']
            override fun getParams(): MutableMap<String, String> {
                val parametros = HashMap<String, String>()

                parametros["usuario"] = edtUsuario.text.toString()
                parametros["password"] = edtPassword.text.toString()

                return parametros

            }



        }

        // RequestQueue: Es una fila de espera
        // Volley envía la petición y queda esperando la respuesta sin bloquear la pantalla del usuario
        Volley.newRequestQueue(this).add(stringRequest)
    }
}