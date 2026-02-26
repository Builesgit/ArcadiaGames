package com.example.prueba1integrador.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prueba1integrador.model.AccionHistorial
import com.example.prueba1integrador.adapter.HistorialAdapter
import com.example.prueba1integrador.model.Juego
import com.example.prueba1integrador.adapter.JuegoAdapter
import com.example.prueba1integrador.R
import com.example.prueba1integrador.activity.AnadirProductoActivity
import com.example.prueba1integrador.activity.GestionarInventarioActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
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

    // ===== MAPA (USUARIO) =====
    private var mapView: MapView? = null
    private var gMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var esUsuarioVisible: Boolean = false

    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                enableMyLocationAndCenter()
            } else {
                Toast.makeText(requireContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }

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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

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
                        esUsuarioVisible = false
                        layoutAdmin.visibility = View.VISIBLE
                        layoutUsuario.visibility = View.GONE
                        sliderHandler.removeCallbacks(sliderRunnable)

                        // Parar/limpiar mapa si estaba
                        teardownMap()

                        // Admin
                        configurarDashboardAdmin(view)
                        cargarHistorialReciente()
                    } else {
                        esUsuarioVisible = true
                        layoutAdmin.visibility = View.GONE
                        layoutUsuario.visibility = View.VISIBLE

                        // Usuario: configurar mapa embebido
                        setupMapUsuario(view, savedInstanceState)

                        iniciarAutoScroll()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

        configurarInteraccionUsuario()
        return view
    }

    // ====================== MAPA USUARIO ======================

    private fun setupMapUsuario(view: View, savedInstanceState: Bundle?) {
        // Solo inicializamos una vez
        if (mapView != null) return

        mapView = view.findViewById(R.id.mapViewTiendas)
        if (mapView == null) return

        mapView?.onCreate(savedInstanceState)

        mapView?.getMapAsync { map ->
            gMap = map

            // Ajustes para que no pelee con el NestedScrollView
            map.uiSettings.isZoomControlsEnabled = false
            map.uiSettings.isMyLocationButtonEnabled = true
            map.uiSettings.isScrollGesturesEnabled = false  // MUY importante en scroll
            map.uiSettings.isZoomGesturesEnabled = true
            map.uiSettings.isRotateGesturesEnabled = false
            map.uiSettings.isTiltGesturesEnabled = false

            // Marcadores de ejemplo (luego los cambias por Firebase)
            val tiendas = listOf(
                Triple("Arcadia Games Centro", 40.416775, -3.703790),
                Triple("Arcadia Games Norte", 40.478000, -3.688000)
            )

            tiendas.forEach { (nombre, lat, lng) ->
                map.addMarker(
                    MarkerOptions()
                        .position(LatLng(lat, lng))
                        .title(nombre)
                )
            }

            // Cámara por defecto (fallback)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(40.416775, -3.703790), 11f))

            // Intentar activar ubicación (pide permiso si hace falta)
            checkLocationPermissionAndEnable()
        }
    }

    private fun checkLocationPermissionAndEnable() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        val granted = ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            enableMyLocationAndCenter()
        } else {
            // Pide permiso solo si el usuario está viendo el layout usuario
            if (esUsuarioVisible) requestLocationPermissionLauncher.launch(permission)
        }
    }

    private fun enableMyLocationAndCenter() {
        val map = gMap ?: return

        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        val granted = ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
        if (!granted) return

        try {
            map.isMyLocationEnabled = true
        } catch (_: SecurityException) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                val user = LatLng(loc.latitude, loc.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(user, 13f))
            }
        }
    }

    private fun teardownMap() {
        // Si el admin entra, destruimos el mapa para evitar fugas
        mapView?.onPause()
        mapView?.onStop()
        mapView?.onDestroy()
        mapView = null
        gMap = null
    }

    // ====================== ADMIN ======================

    private fun configurarDashboardAdmin(view: View) {
        // 1. Inventario
        view.findViewById<View>(R.id.cardInventarioDashboard)?.setOnClickListener {
            startActivity(Intent(requireContext(), GestionarInventarioActivity::class.java))
        }

        // 2. Añadir Producto
        view.findViewById<View>(R.id.cardNuevoProductoDashboard)?.setOnClickListener {
            startActivity(Intent(requireContext(), AnadirProductoActivity::class.java))
        }

        // 3. Incidencias
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

    // ====================== JUEGOS / SLIDER ======================

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

    // ====================== LIFECYCLE (IMPORTANTE PARA MAPVIEW) ======================

    override fun onResume() {
        super.onResume()
        mapView?.onResume()

        val uidActual = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (uidActual.isNotEmpty()) {
            FirebaseDatabase.getInstance().getReference("usuarios").child(uidActual).child("rol")
                .get().addOnSuccessListener { if (it.value != "admin") iniciarAutoScroll() }
        }
    }

    override fun onPause() {
        mapView?.onPause()
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        mapView?.onStop()
        super.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroyView() {
        // OJO: onDestroyView es el correcto en fragments
        mapView?.onDestroy()
        mapView = null
        gMap = null
        super.onDestroyView()
    }
}