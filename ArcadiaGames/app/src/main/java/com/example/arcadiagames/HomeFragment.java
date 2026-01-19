package com.example.arcadiagames;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.carousel.CarouselLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Aquí pegas el código del RecyclerView y la lista de juegos
        List<Juegos> listaNovedades = new ArrayList<>();
        listaNovedades.add(new Juegos("Cybercriminal 2077", "29.99€", R.drawable.wicher3 ));
        listaNovedades.add(new Juegos("The Witcher 3", "19.99€", R.drawable.wicher3));
        listaNovedades.add(new Juegos("Starfield", "60.0€", R.drawable.wicher3));
        listaNovedades.add(new Juegos("Morio", "600.0€", R.drawable.wicher3));
        listaNovedades.add(new Juegos("AgarthaBrainrot", "67.67€", R.drawable.wicher3));


        // 1. Buscamos el RecyclerView
        RecyclerView rvNovedades = view.findViewById(R.id.rv_novedades);


        // 2. Creamos el LayoutManager del carrusel
        CarouselLayoutManager layoutManager = new CarouselLayoutManager();


        layoutManager.setCarouselStrategy(new com.google.android.material.carousel.UncontainedCarouselStrategy());
        //hay diferentes tipos de carrusel, UncontainedCarouselStrategy() es el que creo que es el mejor, per también está CarouselLayoutManager()


        // 4. Lo asignamos al RecyclerView
        rvNovedades.setLayoutManager(layoutManager);
        JuegoAdapter adapter = new JuegoAdapter(listaNovedades);
        rvNovedades.setAdapter(adapter);

        return view;
    }
}
