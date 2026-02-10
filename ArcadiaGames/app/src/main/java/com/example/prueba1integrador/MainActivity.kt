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
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
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

        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )

        // Inicializar Firebase Auth
        auth = Firebase.auth

        // View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.initialScreenLayout.visibility = View.GONE
        binding.loginScreenLayout.visibility = View.VISIBLE
        binding.loginScreenLayout.rotationY = 0f // Aseguramos que el login esté derecho

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

    private fun loginConFirebase(email: String, pass: String) {
        // Mostrar un mensaje de carga o deshabilitar botón si lo deseas
        //un log
        Log.d("DEBUG_APP", "Intentando login para: $email")
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    //log
                    Log.d("DEBUG_APP", "Login Auth exitoso. UID: ${user?.uid}")

                    // Buscamos el rol del usuario en la base de datos (nodo "usuarios")
                    val dbRef = Firebase.database.getReference("usuarios").child(user?.uid ?: "")

                    dbRef.get().addOnSuccessListener { snapshot ->
                        if (snapshot.exists()) {
                            Log.d("DEBUG_APP", "Datos de usuario encontrados en DB")
                            val rol = snapshot.child("rol").value?.toString() ?: "cliente"
                            val nombreUsuario = snapshot.child("nombre").value?.toString() ?: email

                            val intent = Intent(this, HomeActivity::class.java)
                            intent.putExtra("ROL_USUARIO", rol)
                            intent.putExtra("USUARIO_LOGUEADO", nombreUsuario)
                            startActivity(intent)
                            finish()
                        } else {
                            Log.w("DEBUG_APP", "El UID existe en Auth pero NO en el nodo 'usuarios' de la base de datos")
                            Toast.makeText(this, "Error: Usuario no registrado en la base de datos", Toast.LENGTH_LONG).show()
                        }
                    }.addOnFailureListener { e ->
                        Log.e("DEBUG_APP", "Error al leer Base de Datos: ${e.message}")
                        Toast.makeText(this, "Error de red o permisos de base de datos", Toast.LENGTH_SHORT).show()

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

                    // AQUÍ ESTÁ LA CLAVE: Ver el error real de Firebase
                    val error = task.exception?.message
                    Log.e("DEBUG_APP", "Error al leer Base de Datos: ${task.exception?.message}")
                    Toast.makeText(this, "Error de red o permisos de base de datos", Toast.LENGTH_SHORT).show()

                }
            }
    }

}