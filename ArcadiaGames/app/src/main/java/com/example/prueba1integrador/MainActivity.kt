package com.example.prueba1integrador

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.prueba1integrador.databinding.ActivityMainBinding
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import android.os.Handler
import android.os.Looper

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.initialScreenLayout.visibility = View.GONE
        binding.loginScreenLayout.visibility = View.VISIBLE
        binding.loginScreenLayout.rotationY = 0f // Aseguramos que esté de frente

        // Listener Login
        binding.btnLogin.setOnClickListener {
            val user = binding.edtUsuario.text.toString()
            val pass = binding.edtPassword.text.toString()

            if (user.isNotEmpty() && pass.isNotEmpty()) {
                validarUsuario("http://10.0.2.2/arcadia_games_db/validar_usuario.php")
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Ir a Registro
        binding.btnRegistrarse?.setOnClickListener {
            startActivity(Intent(this, CrearCuenta::class.java))
        }
    }

    private fun validarUsuario(url: String) {
        val stringRequest = object : StringRequest(Request.Method.POST, url,
            { response ->
                if (response.isNotEmpty() && !response.contains("no_existe")) {
                    val jsonResponse = JSONObject(response)
                    val rol = jsonResponse.getString("rol")
                    val usuario = jsonResponse.getString("usuario")

                    // Solo dejamos el retraso de 3s para que el servidor respire
                    Handler(Looper.getMainLooper()).postDelayed({
                        val intent = Intent(this, HomeActivity::class.java).apply {
                            putExtra("ROL", rol)
                            putExtra("USUARIO_LOGUEADO", usuario)
                        }
                        startActivity(intent)
                        finish()
                    }, 3000)
                } else {
                    Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                }
            },
            { Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show() }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val parametros = HashMap<String, String>()
                parametros["usuario"] = binding.edtUsuario.text.toString()
                parametros["password"] = binding.edtPassword.text.toString()
                return parametros
            }
        }
        Volley.newRequestQueue(this).add(stringRequest)
    }
}