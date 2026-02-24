package com.example.prueba1integrador

import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MisComprasActivity : BaseActivity() {

    private lateinit var rvCompras: RecyclerView
    private lateinit var adapter: ComprasAdapter
    private lateinit var tabLayout: TabLayout

    private val lista = mutableListOf<JuegoComprado>()

    private var comprasListener: ValueEventListener? = null
    private var alquileresListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_compras)

        rvCompras = findViewById(R.id.rvCompras)
        tabLayout = findViewById(R.id.tabLayout)

        rvCompras.layoutManager = LinearLayoutManager(this)
        adapter = ComprasAdapter(lista)
        rvCompras.adapter = adapter

        tabLayout.addTab(tabLayout.newTab().setText("MIS COMPRAS"))
        tabLayout.addTab(tabLayout.newTab().setText("MIS ALQUILERES"))

        // Por defecto
        cargarCompras()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) cargarCompras() else cargarAlquileres()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseDatabase.getInstance().getReference("compras").child(uid).apply {
            comprasListener?.let { removeEventListener(it) }
        }
        FirebaseDatabase.getInstance().getReference("alquileres").apply {
            alquileresListener?.let { removeEventListener(it) }
        }
    }

    // -------------------------
    // COMPRAS
    // -------------------------
    private fun cargarCompras() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("compras").child(uid)

        comprasListener?.let { ref.removeEventListener(it) }

        comprasListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    lista.clear()

                    for (compraSnap in snapshot.children) {
                        val fechaCompra = compraSnap.child("fecha").getValue(Long::class.java)
                            ?: compraSnap.child("fechaCompra").getValue(Long::class.java)
                            ?: compraSnap.child("timestamp").getValue(Long::class.java)
                            ?: 0L

                        val juegosSnap = compraSnap.child("juegos")

                        if (juegosSnap.exists()) {
                            // Caso normal: compra/juegos/{id} -> Juego
                            for (juegoSnap in juegosSnap.children) {
                                val juego = leerJuegoSeguro(juegoSnap.child("juego"))
                                    ?: leerJuegoSeguro(juegoSnap)

                                if (juego != null) {
                                    lista.add(
                                        JuegoComprado(
                                            juego = juego,
                                            fechaCompra = fechaCompra,
                                            esAlquiler = false
                                        )
                                    )
                                }
                            }
                        } else {
                            // Caso alternativo: compra/juego o compra = Juego
                            val juego = leerJuegoSeguro(compraSnap.child("juego"))
                                ?: leerJuegoSeguro(compraSnap)

                            if (juego != null) {
                                lista.add(
                                    JuegoComprado(
                                        juego = juego,
                                        fechaCompra = fechaCompra,
                                        esAlquiler = false
                                    )
                                )
                            }
                        }
                    }

                    lista.sortByDescending { it.fechaCompra ?: 0L }
                    adapter.notifyDataSetChanged()

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@MisComprasActivity, "Crash evitado en compras: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MisComprasActivity, "Error compras: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }

        ref.addValueEventListener(comprasListener!!)
    }

    // -------------------------
    // ALQUILERES (tu BD está mezclada: uid + raíz)
    // -------------------------
    private fun cargarAlquileres() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid
        val email = user.email ?: ""

        val ref = FirebaseDatabase.getInstance().getReference("alquileres")
        alquileresListener?.let { ref.removeEventListener(it) }

        alquileresListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    lista.clear()

                    // 1) alquileres/{uid}/...
                    val nodoUsuario = snapshot.child(uid)
                    if (nodoUsuario.exists()) {
                        for (alqSnap in nodoUsuario.children) {
                            parseAlquilerSeguro(alqSnap)?.let { lista.add(it) }
                        }
                    }

                    // 2) alquileres/{pushId}... (global)
                    for (alqSnap in snapshot.children) {
                        if (alqSnap.key == uid) continue

                        // Filtrar por usuario
                        val usuarioId = alqSnap.child("usuarioId").getValue(String::class.java)
                            ?: alqSnap.child("uid").getValue(String::class.java)
                            ?: alqSnap.child("clienteUid").getValue(String::class.java)

                        val usuarioEmail = alqSnap.child("usuarioEmail").getValue(String::class.java) ?: ""

                        val esDelUsuario = (usuarioId == uid) || (usuarioEmail.isNotBlank() && usuarioEmail == email)
                        if (!esDelUsuario) continue

                        parseAlquilerSeguro(alqSnap)?.let { lista.add(it) }
                    }

                    lista.sortByDescending { it.fechaInicio ?: 0L }
                    adapter.notifyDataSetChanged()

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@MisComprasActivity, "Crash evitado en alquileres: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MisComprasActivity, "Error alquileres: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }

        ref.addValueEventListener(alquileresListener!!)
    }

    // -------------------------
    // Helpers ANTI-CRASH
    // -------------------------
    private fun leerJuegoSeguro(snap: DataSnapshot): Juego? {
        if (!snap.exists()) return null
        return try {
            snap.getValue(Juego::class.java)
        } catch (e: Exception) {
            // Aquí caía tu app (DatabaseException)
            null
        }
    }

    private fun parseAlquilerSeguro(alqSnap: DataSnapshot): JuegoComprado? {
        val fechaInicio = alqSnap.child("fechaInicio").getValue(Long::class.java)
            ?: alqSnap.child("inicio").getValue(Long::class.java)
            ?: 0L

        val fechaFin = alqSnap.child("fechaFin").getValue(Long::class.java)
            ?: alqSnap.child("fin").getValue(Long::class.java)
            ?: 0L

        val juego = leerJuegoSeguro(alqSnap.child("juego")) ?: leerJuegoSeguro(alqSnap)
        if (juego == null) return null

        return JuegoComprado(
            juego = juego,
            fechaInicio = fechaInicio,
            fechaFin = fechaFin,
            esAlquiler = true
        )
    }
}