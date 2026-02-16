package com.example.prueba1integrador

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.CarouselSnapHelper
import com.google.android.material.carousel.FullScreenCarouselStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FragmentHome : Fragment() {

    private lateinit var rvNovedades: RecyclerView
    private lateinit var rvHistorialAdmin: RecyclerView
    private val sliderHandler = Handler(Looper.getMainLooper())
    private val snapHelper = CarouselSnapHelper()
    private var listaJuegosDynamic = mutableListOf<Juego>()
    private lateinit var adapterJuegos: JuegoAdapter

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

        // Referencias a Layouts
        val layoutUsuario = view.findViewById<LinearLayout>(R.id.layoutHomeUsuario)
        val layoutAdmin = view.findViewById<LinearLayout>(R.id.layoutHomeAdmin)

        // Referencias a RecyclerViews
        rvNovedades = view.findViewById(R.id.rv_novedades)
        rvHistorialAdmin = view.findViewById(R.id.recyclerViewAdmin)

        // --- CONFIGURACIÓN CAROUSEL (USUARIO) ---
        val layoutManagerCarousel = CarouselLayoutManager()
        layoutManagerCarousel.setCarouselStrategy(FullScreenCarouselStrategy())
        rvNovedades.layoutManager = layoutManagerCarousel
        rvNovedades.onFlingListener = null
        snapHelper.attachToRecyclerView(rvNovedades)

        adapterJuegos = JuegoAdapter(listaJuegosDynamic, true)
        rvNovedades.adapter = adapterJuegos

        // Cargar datos iniciales
        cargarUltimosJuegos()

        // --- LÓGICA DE ROLES Y DASHBOARD ---
        val uidActual = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (uidActual.isNotEmpty()) {
            val userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(uidActual)
            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    val rol = snapshot.child("rol").getValue(String::class.java) ?: "user"

                    if (rol == "admin") {
                        layoutAdmin.visibility = View.VISIBLE
                        layoutUsuario.visibility = View.GONE
                        sliderHandler.removeCallbacks(sliderRunnable)

                        // Configuramos el Dashboard técnico
                        configurarDashboardAdmin(view)
                        cargarHistorialReciente()
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

    private fun configurarDashboardAdmin(view: View) {
        // 1. Inventario
        view.findViewById<View>(R.id.cardInventarioDashboard)?.setOnClickListener {
            startActivity(Intent(requireContext(), GestionarInventarioActivity::class.java))
        }

        // 2. Añadir Producto
        view.findViewById<View>(R.id.cardNuevoProductoDashboard)?.setOnClickListener {
            startActivity(Intent(requireContext(), AnadirProductoActivity::class.java))
        }

        // 3. TU BOTÓN DE INCIDENCIAS (Ahora con la lógica de "Gestionar")
        view.findViewById<View>(R.id.cardIncidenciasDashboard)?.setOnClickListener {
            val fragmentoIncidencias = FragmentMostrarIncidencias()
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.main_home_U_fragment, fragmentoIncidencias)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun cargarHistorialReciente() {
        val refHistorial = FirebaseDatabase.getInstance().getReference("historial")
        refHistorial.limitToLast(15).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                val listaLocalLogs = mutableListOf<AccionHistorial>()
                for (data in snapshot.children) {
                    val log = data.getValue(AccionHistorial::class.java)
                    if (log != null) listaLocalLogs.add(log)
                }
                listaLocalLogs.reverse()
                rvHistorialAdmin.layoutManager = LinearLayoutManager(requireContext())
                rvHistorialAdmin.adapter = HistorialAdapter(listaLocalLogs, esModoMenu = true)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error en historial: ${error.message}")
            }
        })
    }

    private fun cargarUltimosJuegos() {
        val juegosRef = FirebaseDatabase.getInstance().getReference("productos")
        juegosRef.limitToLast(10).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                listaJuegosDynamic.clear()
                for (postSnapshot in snapshot.children) {
                    val juego = postSnapshot.getValue(Juego::class.java)
                    if (juego != null) listaJuegosDynamic.add(juego)
                }
                listaJuegosDynamic.reverse()
                adapterJuegos.notifyDataSetChanged()
                if (listaJuegosDynamic.isNotEmpty()) {
                    val middle = 5000 - (5000 % listaJuegosDynamic.size)
                    rvNovedades.scrollToPosition(middle)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
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
        val uidActual = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (uidActual.isNotEmpty()) {
            FirebaseDatabase.getInstance().getReference("usuarios").child(uidActual).child("rol")
                .get().addOnSuccessListener { if (it.value != "admin") iniciarAutoScroll() }
        }
    }

    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
    }
}