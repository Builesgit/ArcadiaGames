package com.example.prueba1integrador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.MultiBrowseCarouselStrategy

class FragmentHome : Fragment() {

    private lateinit var rvNovedades: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val listaNovedades = listOf(
            Juego("Cyberpunk 2077", "Desc...", R.drawable.cyberpunk2077, "59.99€", listOf("PC", "Playstation", "Xbox")),
            Juego("The Witcher 3", "Desc...", R.drawable.wicher3, "29.99€", listOf("PC", "Playstation", "Xbox", "Nintendo")),
            Juego("Super Mario Odyssey", "Desc...", R.drawable.super_mario_odyssey, "49.99€", listOf("Nintendo")),
            Juego("Halo Infinite", "Desc...", R.drawable.halo_infinitive, "0.00€", listOf("PC", "Xbox")),
            Juego("God of War Ragnarok", "Desc...", R.drawable.god_of_war, "69.99€", listOf("Playstation")),
            Juego("Elden Ring", "Desc...", R.drawable.elden_ring, "59.99€", listOf("PC", "Playstation", "Xbox"))
        )

        // Dentro de onCreateView en FragmentHome.kt
        val rvNovedades = view.findViewById<RecyclerView>(R.id.rv_novedades)
        rvNovedades.layoutManager = CarouselLayoutManager()

        val adapter = JuegoAdapter(listaNovedades, true)
        rvNovedades.adapter = adapter

        // Posicionamos el carrusel en el centro del "infinito" para permitir scroll en ambas direcciones
        if (listaNovedades.isNotEmpty()) {
            val middle = 5000 - (5000 % listaNovedades.size)
            rvNovedades.scrollToPosition(middle)
        }

        return view
    }
}