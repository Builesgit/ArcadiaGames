package com.example.prueba1integrador

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.prueba1integrador.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding  // Es una variable que nos permite acceder a los elementos de la interfaz
    private lateinit var edtUsuario: EditText // Es una variable que nos permite acceder a los elementos de la interfaz
    private lateinit var edtPassword: EditText // Es una variable que nos permite acceder a los elementos de la interfaz
    private lateinit var btnLogin: Button // Es una variable que nos permite acceder a los elementos de la interfaz

    // Variable para Firebase Authentication
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar Firebase Auth
        auth = Firebase.auth

        // View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.initialScreenLayout.visibility = View.GONE
        binding.loginScreenLayout.visibility = View.VISIBLE
        binding.loginScreenLayout.rotationY = 0f // Aseguramos que el login esté derecho

        // CONFIGURAR EL ESTADO INICIAL DE LA TRANSICION
        //setupInitialState()

        // CARGAR IMG FONDO LOGO
        //binding.logoImageView.setImageResource(R.drawable.fondo_con_logo)

        // EJECUTAR LA TRANSICIÓN AUTOMÁTICA (6 segundos de splash)
        //binding.root.postDelayed({
        //    executeFlipTransition()
        //}, 3000)

        // INICIALIZAR VARIABLES LOGIN
        edtUsuario = binding.edtUsuario
        edtPassword = binding.edtPassword
        btnLogin = binding.btnLogin

        btnLogin.setOnClickListener {
            val email = edtUsuario.text.toString().trim()
            val pass = edtPassword.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                loginConFirebase(email, pass)
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
        // Ir a Registro
        binding.btnRegistrarse?.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    /*private fun setupInitialState() {
        val scale = resources.displayMetrics.density
        val distance = 8000 * scale
        binding.initialScreenLayout.cameraDistance = distance
        binding.loginScreenLayout.cameraDistance = distance
        binding.loginScreenLayout.visibility = View.GONE
    }*/

    // Función para ejecutar la transición de volteo
    /*private fun executeFlipTransition() {
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
    }*/

    private fun loginConFirebase(email: String, pass: String) {
        // Mostrar un mensaje de carga o deshabilitar botón si lo deseas
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    // Buscamos el rol del usuario en la base de datos (nodo "usuarios")
                    val dbRef = Firebase.database.getReference("usuarios").child(user?.uid ?: "")

                    dbRef.get().addOnSuccessListener { snapshot ->
                        // Si el usuario existe en la DB, leemos su rol
                        val rol = snapshot.child("rol").value?.toString() ?: "cliente"
                        val nombreUsuario = snapshot.child("usuario").value?.toString() ?: email

                        val intent = Intent(this, HomeActivity::class.java)
                        intent.putExtra("ROL_USUARIO", rol)
                        intent.putExtra("USUARIO_LOGUEADO", nombreUsuario)

                        startActivity(intent)
                        finish()
                    }.addOnFailureListener {
                        // Si falla la lectura de la DB, entra como cliente básico
                        val intent = Intent(this, HomeActivity::class.java)
                        intent.putExtra("ROL_USUARIO", "cliente")
                        startActivity(intent)
                        finish()
                    }
                } else {
                    // Si el login falla (contraseña mal, usuario no existe, etc)
                    Log.e("FirebaseLogin", "Error: ${task.exception?.message}")
                    Toast.makeText(this, "Error: Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                }
            }
    }
}