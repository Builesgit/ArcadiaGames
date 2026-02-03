package com.example.prueba1integrador

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PagoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pago)

        // Views del XML de pago
        val tvTotal = findViewById<TextView>(R.id.tvTotal)
        val etName = findViewById<EditText>(R.id.etName)
        val etCardNumber = findViewById<EditText>(R.id.etCardNumber)
        val etExpiration = findViewById<EditText>(R.id.etExpiration)
        val etCvv = findViewById<EditText>(R.id.etCvv)
        val btnCheckout = findViewById<Button>(R.id.btnCheckout)

        // 👉 SOLO recibimos el total desde la cesta
        val total = intent.getDoubleExtra("PRECIO_TOTAL", 0.0)

        // Mostrar total
        tvTotal.text = "TOTAL: €%.2f".format(total)

        btnCheckout.setOnClickListener {

            val nombre = etName.text.toString().trim()
            val tarjeta = etCardNumber.text.toString().trim()
            val expiracion = etExpiration.text.toString().trim()
            val cvv = etCvv.text.toString().trim()

            when {
                nombre.isEmpty() -> {
                    Toast.makeText(
                        this,
                        "Introduce el nombre del titular",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                tarjeta.length < 12 -> {
                    Toast.makeText(
                        this,
                        "Número de tarjeta inválido",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                expiracion.isEmpty() -> {
                    Toast.makeText(
                        this,
                        "Introduce la fecha de expiración",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                cvv.length < 3 -> {
                    Toast.makeText(
                        this,
                        "CVV inválido",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    Toast.makeText(
                        this,
                        "Pago realizado correctamente ✅",
                        Toast.LENGTH_LONG
                    ).show()

                    // Aquí luego podrás:
                    // - vaciar la cesta
                    // - guardar la compra en Firebase

                    finish()
                }
            }
        }
    }
}
