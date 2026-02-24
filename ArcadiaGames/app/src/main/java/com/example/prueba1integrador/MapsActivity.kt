package com.example.prueba1integrador

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                enableMyLocation()
                moveToUserLocation()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
                // fallback: mostrar Madrid
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(40.416775, -3.703790), 11f))
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Opcional: controles del mapa
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        // Marcadores de ejemplo (cambia por tus tiendas reales)
        val tiendas = listOf(
            Tienda("Arcadia Games Centro", 40.416775, -3.703790),
            Tienda("Arcadia Games Norte", 40.478000, -3.688000)
        )

        tiendas.forEach { t ->
            val pos = LatLng(t.lat, t.lng)
            mMap.addMarker(MarkerOptions().position(pos).title(t.nombre))
        }

        checkLocationPermissionAndStart()
    }

    private fun checkLocationPermissionAndStart() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
            moveToUserLocation()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        }
    }

    private fun moveToUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val userLatLng = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 13f))
            } else {
                // Si no hay ubicación, centramos en Madrid por defecto
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(40.416775, -3.703790), 11f))
            }
        }.addOnFailureListener {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(40.416775, -3.703790), 11f))
        }
    }
}

data class Tienda(val nombre: String, val lat: Double, val lng: Double)