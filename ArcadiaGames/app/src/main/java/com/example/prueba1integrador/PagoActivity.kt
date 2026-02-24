package com.example.prueba1integrador

import android.content.Intent
import android.os.Bundle
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class PagoActivity : BaseActivity() {

    private lateinit var listaProductos: ArrayList<Juego>
    private lateinit var compraId: String
    private val inventoryManager = FirebaseInventoryManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pago)

        listaProductos = intent.getSerializableExtra("LISTA_PRODUCTOS") as? ArrayList<Juego> ?: arrayListOf()
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        compraId = FirebaseDatabase.getInstance().reference.push().key ?: UUID.randomUUID().toString()

        findViewById<Button>(R.id.btnCheckout).setOnClickListener {
            val nombreCliente = findViewById<EditText>(R.id.etName).text.toString()
            if (nombreCliente.isNotEmpty() && listaProductos.isNotEmpty()) {
                procesarFinalizacionPago(nombreCliente)
            }
        }
    }

    private fun procesarFinalizacionPago(cliente: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().reference

        db.child("compras").child(uid).child(compraId).child("cliente").setValue(cliente)
            .addOnSuccessListener {
                for (juego in listaProductos) {
                    // 1. Guardar en historial de compras
                    db.child("compras").child(uid).child(compraId).child("juegos").child(juego.id).setValue(juego)

                    // 2. Descuento de Stock por plataforma
                    val platKey = juego.plataforma.lowercase().trim()
                    val mapaStock = juego.detalle_stock?.toMutableMap() ?: mutableMapOf()
                    val actual = mapaStock[platKey] ?: 0

                    if (actual > 0) {
                        mapaStock[platKey] = actual - 1
                        val updates = hashMapOf<String, Any>(
                            "stock" to (juego.stock - 1).coerceAtLeast(0),
                            "detalle_stock" to mapaStock
                        )
                        db.child("productos").child(juego.id).updateChildren(updates)
                    }

                    // 3. ACTUALIZAR ESTADÍSTICAS (Venta registrada)
                    inventoryManager.registrarVentaMecanica(juego.id, 1)
                    inventoryManager.registrarEnHistorial(cliente, "Compró ${juego.nombre}", juego.nombre, 1)
                }
                db.child("cesta").child(uid).removeValue()
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
    }
}