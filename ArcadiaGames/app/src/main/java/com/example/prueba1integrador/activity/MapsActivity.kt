package com.example.prueba1integrador.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.prueba1integrador.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val tienda1 = LatLng(40.598839, -3.502025)
        val tienda2 = LatLng(40.539033, -3.625239)

        mMap.addMarker(MarkerOptions().position(tienda1).title("Tienda 1 - Algete"))
        mMap.addMarker(MarkerOptions().position(tienda2).title("Tienda 2 - Alcobendas"))

        // Centrar mapa
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(40.57, -3.56), 11f))
    }
}