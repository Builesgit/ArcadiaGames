package com.example.prueba1integrador.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import com.example.prueba1integrador.R
import com.example.prueba1integrador.activity.*
import com.example.prueba1integrador.adapter.*
import com.example.prueba1integrador.model.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.carousel.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FragmentHome : Fragment() {

    private lateinit var rvNovedades: RecyclerView
    private lateinit var rvHistorialAdmin: RecyclerView
    private val sliderHandler = Handler(Looper.getMainLooper())
    private var listaJuegosDynamic = mutableListOf<Juego>()
    private lateinit var adapterJuegos: JuegoAdapter

    private var mapView: MapView? = null
    private var gMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var esUsuarioVisible: Boolean = false

    private val requestLocationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) enableMyLocationAndCenter()
    }

    private val sliderRunnable = object : Runnable {
        override fun run() {
            if (!isAdded || listaJuegosDynamic.isEmpty()) return
            rvNovedades.smoothScrollBy(600, 0)
            sliderHandler.postDelayed(this, 3000)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

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
                            esUsuarioVisible = false
                            view.findViewById<View>(R.id.layoutHomeAdmin).visibility = View.VISIBLE
                            view.findViewById<View>(R.id.layoutHomeUsuario).visibility = View.GONE
                            teardownMap()
                            configurarDashboardAdmin(view)
                            cargarHistorialReciente()
                        } else {
                            esUsuarioVisible = true
                            view.findViewById<View>(R.id.layoutHomeAdmin).visibility = View.GONE
                            view.findViewById<View>(R.id.layoutHomeUsuario).visibility = View.VISIBLE
                            setupMapUsuario(view, savedInstanceState)
                            iniciarAutoScroll()
                        }
                    }
                    override fun onCancelled(e: DatabaseError) {}
                })
        }
        cargarUltimosJuegos()
        return view
    }

    private fun setupMapUsuario(view: View, savedInstanceState: Bundle?) {
        if (mapView != null) return
        mapView = view.findViewById(R.id.mapViewTiendas)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync { map ->
            gMap = map
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(40.41, -3.70), 10f))
            checkLocationPermission()
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocationAndCenter()
        } else if (esUsuarioVisible) {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun enableMyLocationAndCenter() {
        val map = gMap ?: return
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

        try {
            map.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                loc?.let { map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 13f)) }
            }
        } catch (e: SecurityException) {
            Log.e("MapError", "Error de permisos: ${e.message}")
        }
    }

    private fun configurarDashboardAdmin(view: View) {
        view.findViewById<View>(R.id.cardInventarioDashboard)?.setOnClickListener { startActivity(Intent(context, GestionarInventarioActivity::class.java)) }
        view.findViewById<View>(R.id.cardNuevoProductoDashboard)?.setOnClickListener { startActivity(Intent(context, AnadirProductoActivity::class.java)) }
        view.findViewById<View>(R.id.cardEstadisticasDashboard)?.setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.main_home_U_fragment, FragmentEstadisticas()).addToBackStack(null).commit()
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

    private fun teardownMap() {
        mapView?.onDestroy()
        mapView = null
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

    override fun onResume() { super.onResume(); mapView?.onResume() }
    override fun onPause() { super.onPause(); mapView?.onPause(); sliderHandler.removeCallbacks(sliderRunnable) }
    override fun onDestroyView() { teardownMap(); super.onDestroyView() }
}