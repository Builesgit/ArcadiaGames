package com.example.prueba1integrador

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.prueba1integrador.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RegisterActivity : BaseActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageUtils.updateBaseContextLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.flagSpanish.setOnClickListener { changeLang("es") }
        binding.flagEnglish.setOnClickListener { changeLang("en") }
        binding.flagFrench.setOnClickListener { changeLang("fr") }

        binding.btnRegistrar.setOnClickListener {
            val nombreUser = binding.etNombreUsuario.text.toString().trim()
            val email = binding.etUsuario.text.toString().trim()
            val pass = binding.etPass.text.toString().trim()
            val confirm = binding.etConfirmPass.text.toString().trim()

            if (nombreUser.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty() && pass == confirm) {
                registrarEnFirebase(email, pass, nombreUser)
            } else {
                Toast.makeText(this, getString(R.string.completar_campos), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnVolverLogin.setOnClickListener { finish() }
    }

    private fun registrarEnFirebase(email: String, pass: String, nombre: String) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""
                    val myRef = Firebase.database.getReference("usuarios").child(uid)

                    val usuarioData = mapOf(
                        "nombre" to nombre,
                        "correo" to email,
                        "rol" to "user"
                    )

                    myRef.setValue(usuarioData).addOnSuccessListener {
                        Toast.makeText(this, "¡Registro completado!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun changeLang(code: String) {
        LanguageUtils.saveLocale(this, code)
        recreate()
    }
}