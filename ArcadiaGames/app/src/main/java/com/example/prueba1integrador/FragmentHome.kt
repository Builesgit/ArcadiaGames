package com.example.prueba1integrador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.carousel.CarouselLayoutManager

class FragmentHome : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)


        val listaNovedades = listOf(
            Juego(nombre = "Cyberpunk 2077", descripcion = "Futuro distópico", imagenResId = R.drawable.cyberpunk2077, precio = "59.99€", tags = listOf("PC", "Playstation", "Xbox")),
            Juego(nombre = "The Witcher 3", descripcion = "Caza monstruos", imagenResId = R.drawable.wicher3, precio = "29.99€", tags = listOf("PC", "Playstation", "Xbox", "Nintendo")),
            Juego(nombre = "Super Mario Odyssey", descripcion = "Aventura 3D", imagenResId = R.drawable.super_mario_odyssey, precio = "49.99€", tags = listOf("Nintendo")),
            Juego(nombre = "Halo Infinite", descripcion = "Jefe Maestro", imagenResId = R.drawable.halo_infinitive, precio = "0.00€", tags = listOf("PC", "Xbox")),
            Juego(nombre = "God of War Ragnarok", descripcion = "Kratos vuelve", imagenResId = R.drawable.god_of_war, precio = "69.99€", tags = listOf("Playstation")),
            Juego(nombre = "Elden Ring", descripcion = "Sin luz", imagenResId = R.drawable.elden_ring, precio = "59.99€", tags = listOf("PC", "Playstation", "Xbox"))
        )


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