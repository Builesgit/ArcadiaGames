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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val listaNovedades = listOf(
            Juego("Cybercriminal 2077", "29.99€", R.drawable.wicher3, "29.99€", listOf("PC")),
            Juego("The Witcher 3", "19.99€", R.drawable.wicher3, "19.99€", listOf("PC")),
            Juego("Starfield", "60.0€", R.drawable.wicher3, "60.0€", listOf("PC")),
            Juego("Morio", "600.0€", R.drawable.wicher3, "600.0€", listOf("PC")),
            Juego("AgarthaBrainrot", "67.67€", R.drawable.wicher3, "67.67€", listOf("PC"))
        )

        val rvNovedades = view.findViewById<RecyclerView>(R.id.rv_novedades)

        rvNovedades.layoutManager = CarouselLayoutManager()

        val adapter = JuegoAdapter(listaNovedades, true)
        rvNovedades.adapter = adapter

        return view
    }
}