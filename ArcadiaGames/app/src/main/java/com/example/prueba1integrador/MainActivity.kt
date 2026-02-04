package com.example.prueba1integrador

import com.example.prueba1integrador.BuildConfig
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.prueba1integrador.databinding.ActivityMainBinding

import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.ktx.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var edtUsuario: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnLogin: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. INICIALIZAR FIREBASE Y APP CHECK ANTES QUE NADA
        FirebaseApp.initializeApp(this)
        val firebaseAppCheck = Firebase.appCheck

        // Forzamos el modo Debug si estamos en desarrollo para ver el token en Logcat
        if (BuildConfig.DEBUG) {
            firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            // Este Log te confirmará que el modo Debug entró correctamente
            Log.d("AppCheck", "Modo Debug activado. Busca el 'debug secret' más abajo.")
        } else {
            firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }

        auth = Firebase.auth

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // UI Setup
        binding.initialScreenLayout.visibility = View.GONE
        binding.loginScreenLayout.visibility = View.VISIBLE

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

        binding.btnRegistrarse?.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginConFirebase(email: String, pass: String) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val dbRef = Firebase.database.getReference("usuarios").child(user?.uid ?: "")

                    dbRef.get().addOnSuccessListener { snapshot ->
                        val rol = snapshot.child("rol").value?.toString() ?: "cliente"
                        val nombreUsuario = snapshot.child("nombre").value?.toString() ?: email

                        val intent = Intent(this, HomeActivity::class.java)
                        intent.putExtra("ROL_USUARIO", rol)
                        intent.putExtra("USUARIO_LOGUEADO", nombreUsuario)

                        startActivity(intent)
                        finish()
                    }.addOnFailureListener {
                        Log.e("FirebaseDB", "Error al leer datos: ${it.message}")
                        val intent = Intent(this, HomeActivity::class.java)
                        intent.putExtra("ROL_USUARIO", "cliente")
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Log.e("FirebaseLogin", "Error: ${task.exception?.message}")
                    Toast.makeText(this, "Error: Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                }
            }
    }
}