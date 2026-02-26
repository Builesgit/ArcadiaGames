package com.example.prueba1integrador.activity

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.prueba1integrador.R
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

        // 1. Configuración de UI
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        // 2. MARCADORES
        val tienda1x = 40.598839
        val tienda1y = -3.502025

        val tienda2x = 40.539033
        val tienda2y = -3.625239

        addMarker(tienda1x, tienda1y, "Tienda 1 - San Fernando")
        addMarker(tienda2x, tienda2y, "Tienda 2 - Alcobendas")

        val centroMadrid = LatLng(40.55, -3.55)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centroMadrid, 10f))

        // 3. Gestionar la ubicación
        checkLocationPermissionAndStart()
    }


    private fun addMarker(lat: Double, lng: Double, titulo: String) {
        val coordinates = LatLng(lat, lng)
        mMap.addMarker(
            MarkerOptions()
                .position(coordinates)
                .title(titulo)
        )
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 12f), 4000, null)
    }

    private fun moveToUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Posición inicial por defecto (Madrid) si no hay permisos
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(40.416775, -3.703790), 10f))
            return
        }

        // Usamos animateCamera solo si obtenemos la ubicación, si no, moveCamera (es más rápido)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val userLatLng = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12f))
            } else {
                // Si el GPS no tiene caché, mostramos la zona de las tiendas directamente
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(40.598839, -3.502025), 10f))
            }
        }.addOnFailureListener {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(40.416775, -3.703790), 10f))
        }
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


}

data class Tienda(val nombre: String, val lat: Double, val lng: Double)