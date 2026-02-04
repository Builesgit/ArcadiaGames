package com.example.prueba1integrador

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.CarouselSnapHelper
import com.google.android.material.carousel.FullScreenCarouselStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentHome : Fragment() {

    private lateinit var rvNovedades: RecyclerView
    private val sliderHandler = Handler(Looper.getMainLooper())
    private val snapHelper = CarouselSnapHelper()
    private var listaJuegosDynamic = mutableListOf<Juego>() // Lista dinámica
    private lateinit var adapter: JuegoAdapter

    private val sliderRunnable = object : Runnable {
        override fun run() {
            if (!isAdded || !::rvNovedades.isInitialized || listaJuegosDynamic.isEmpty()) return

            val scrollDistance = 600
            rvNovedades.smoothScrollBy(scrollDistance, 0)
            sliderHandler.postDelayed(this, 3000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val layoutUsuario = view.findViewById<LinearLayout>(R.id.layoutHomeUsuario)
        val layoutAdmin = view.findViewById<LinearLayout>(R.id.layoutHomeAdmin)
        rvNovedades = view.findViewById(R.id.rv_novedades)

        // Configuración de UI
        val layoutManager = CarouselLayoutManager()
        layoutManager.setCarouselStrategy(FullScreenCarouselStrategy())
        rvNovedades.layoutManager = layoutManager
        rvNovedades.onFlingListener = null
        snapHelper.attachToRecyclerView(rvNovedades)

        // Inicializamos el adaptador con una lista vacía
        adapter = JuegoAdapter(listaJuegosDynamic, true)
        rvNovedades.adapter = adapter

        // 1. CARGAR LOS ÚLTIMOS JUEGOS DESDE LA BASE DE DATOS
        cargarUltimosJuegos()

        // Lógica de roles
        val uidActual = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (uidActual.isNotEmpty()) {
            val userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(uidActual)
            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val rol = snapshot.child("rol").getValue(String::class.java) ?: "user"
                    if (rol == "admin") {
                        layoutAdmin.visibility = View.VISIBLE
                        layoutUsuario.visibility = View.GONE
                        sliderHandler.removeCallbacks(sliderRunnable)
                    } else {
                        layoutAdmin.visibility = View.GONE
                        layoutUsuario.visibility = View.VISIBLE
                        iniciarAutoScroll()
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }

        configurarInteraccionUsuario()
        return view
    }

    private fun cargarUltimosJuegos() {
        // Cambiamos "juegos" por "productos" que es la rama que existe en tus reglas
        val juegosRef = FirebaseDatabase.getInstance().getReference("productos")

        juegosRef.limitToLast(10).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaJuegosDynamic.clear()
                for (postSnapshot in snapshot.children) {
                    val juego = postSnapshot.getValue(Juego::class.java)
                    if (juego != null) {
                        listaJuegosDynamic.add(juego)
                    }
                }

                // Invertimos la lista para que el último añadido sea el primero en verse
                listaJuegosDynamic.reverse()

                // Notificamos al adaptador y posicionamos en el centro para el scroll infinito
                adapter.notifyDataSetChanged()
                if (listaJuegosDynamic.isNotEmpty()) {
                    val middle = 5000 - (5000 % listaJuegosDynamic.size)
                    rvNovedades.scrollToPosition(middle)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error cargando juegos: ${error.message}")
            }
        })
    }

    private fun iniciarAutoScroll() {
        sliderHandler.removeCallbacks(sliderRunnable)
        if (listaJuegosDynamic.isNotEmpty()) {
            sliderHandler.postDelayed(sliderRunnable, 3000)
        }
    }

    private fun configurarInteraccionUsuario() {
        rvNovedades.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    sliderHandler.removeCallbacks(sliderRunnable)
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    iniciarAutoScroll()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        iniciarAutoScroll()
    }

    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
    }
}