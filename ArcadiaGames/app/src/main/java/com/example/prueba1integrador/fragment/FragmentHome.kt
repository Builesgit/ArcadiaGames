package com.example.prueba1integrador.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import com.example.prueba1integrador.R
import com.example.prueba1integrador.activity.*
import com.example.prueba1integrador.adapter.*
import com.example.prueba1integrador.model.*
import com.google.android.material.carousel.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FragmentHome : Fragment() {

    private lateinit var rvNovedades: RecyclerView
    private lateinit var rvHistorialAdmin: RecyclerView
    private val sliderHandler = Handler(Looper.getMainLooper())
    private var listaJuegosDynamic = mutableListOf<Juego>()
    private lateinit var adapterJuegos: JuegoAdapter

    private val sliderRunnable = object : Runnable {
        override fun run() {
            if (!isAdded || listaJuegosDynamic.isEmpty()) return
            rvNovedades.smoothScrollBy(600, 0)
            sliderHandler.postDelayed(this, 3000)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        rvNovedades = view.findViewById(R.id.rv_novedades)
        rvHistorialAdmin = view.findViewById(R.id.recyclerViewAdmin)

        rvNovedades.layoutManager = CarouselLayoutManager().apply { setCarouselStrategy(FullScreenCarouselStrategy()) }
        CarouselSnapHelper().attachToRecyclerView(rvNovedades)

        adapterJuegos = JuegoAdapter(listaJuegosDynamic, esCarousel = true)
        rvNovedades.adapter = adapterJuegos

        val uidActual = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (uidActual.isNotEmpty()) {
            FirebaseDatabase.getInstance().getReference("usuarios").child(uidActual)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!isAdded) return
                        val rol = snapshot.child("rol").getValue(String::class.java) ?: "user"

                        if (rol == "admin") {
                            view.findViewById<View>(R.id.layoutHomeAdmin).visibility = View.VISIBLE
                            view.findViewById<View>(R.id.layoutHomeUsuario).visibility = View.GONE
                            configurarDashboardAdmin(view)
                            cargarHistorialReciente()
                        } else {
                            view.findViewById<View>(R.id.layoutHomeAdmin).visibility = View.GONE
                            view.findViewById<View>(R.id.layoutHomeUsuario).visibility = View.VISIBLE
                            iniciarAutoScroll()
                        }
                    }
                    override fun onCancelled(e: DatabaseError) {}
                })
        }
        cargarUltimosJuegos()
        return view
    }

    private fun configurarDashboardAdmin(view: View) {
        view.findViewById<View>(R.id.cardInventarioDashboard)?.setOnClickListener {
            startActivity(Intent(context, GestionarInventarioActivity::class.java))
        }
        view.findViewById<View>(R.id.cardNuevoProductoDashboard)?.setOnClickListener {
            startActivity(Intent(context, AnadirProductoActivity::class.java))
        }
        view.findViewById<View>(R.id.cardEstadisticasDashboard)?.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_home_U_fragment, FragmentEstadisticas())
                .addToBackStack(null)
                .commit()
        }
        view.findViewById<View>(R.id.cardIncidenciasDashboard)?.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_home_U_fragment, FragmentMostrarIncidencias())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun cargarUltimosJuegos() {
        FirebaseDatabase.getInstance().getReference("productos").limitToLast(10)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(s: DataSnapshot) {
                    listaJuegosDynamic.clear()
                    s.children.forEach { it.getValue(Juego::class.java)?.let { j -> listaJuegosDynamic.add(j) } }
                    listaJuegosDynamic.reverse()
                    adapterJuegos.notifyDataSetChanged()
                }
                override fun onCancelled(e: DatabaseError) {}
            })
    }

    private fun iniciarAutoScroll() {
        sliderHandler.removeCallbacks(sliderRunnable)
        sliderHandler.postDelayed(sliderRunnable, 3000)
    }

    private fun cargarHistorialReciente() {
        FirebaseDatabase.getInstance().getReference("historial").limitToLast(10)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(s: DataSnapshot) {
                    val logs = s.children.mapNotNull { it.getValue(AccionHistorial::class.java) }.reversed()
                    rvHistorialAdmin.layoutManager = LinearLayoutManager(context)
                    rvHistorialAdmin.adapter = HistorialAdapter(logs, esModoMenu = true)
                }
                override fun onCancelled(e: DatabaseError) {}
            })
    }

    override fun onPause() { super.onPause(); sliderHandler.removeCallbacks(sliderRunnable) }
}