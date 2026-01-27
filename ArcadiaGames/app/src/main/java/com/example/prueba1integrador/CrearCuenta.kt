package com.example.prueba1integrador

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
// El nombre de la clase Binding siempre es el nombre del XML en CamelCase + "Binding"
import com.example.prueba1integrador.databinding.ActivityRegisterBinding
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class CrearCuenta : AppCompatActivity() {

    // Cambiamos el tipo para que coincida con activity_register.xml
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Si aquí 'ActivityRegisterBinding' sale en rojo, realiza el paso 3
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegistrar.setOnClickListener {
            val user = binding.etUsuario.text.toString()
            val pass = binding.etPass.text.toString()
            val confirm = binding.etConfirmPass.text.toString()

            if (user.isNotEmpty() && pass.isNotEmpty() && pass == confirm) {
                ejecutarRegistro("http://10.0.2.2/arcadia_games_db/registro.php", user, pass)
            } else {
                Toast.makeText(this, "Verifica los campos", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnVolverLogin.setOnClickListener { finish() }
    }

    private fun ejecutarRegistro(url: String, usuario: String, pass: String) {
        val queue = Volley.newRequestQueue(this)
        val postRequest = object : StringRequest(Request.Method.POST, url,
            { response ->
                if (response.trim() == "registrado_exitoso") {
                    Toast.makeText(this, "¡Registro completado!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            },
            { Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show() }
        ) {
            override fun getParams(): Map<String, String> = mapOf(
                "usuario" to usuario,
                "password" to pass
            )
        }
        queue.add(postRequest)
    }
}