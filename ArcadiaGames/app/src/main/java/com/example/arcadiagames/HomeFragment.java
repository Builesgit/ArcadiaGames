package com.example.arcadiagames;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.carousel.CarouselLayoutManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Aquí se pega el código del RecyclerView y la lista de juegos
        List<Juego> listaNovedades = new ArrayList<>();

        listaNovedades.add(new Juego("Cybercriminal 2077", "29.99€", R.drawable.wicher3,"29.99€", Arrays.asList("PC")));
        listaNovedades.add(new Juego("The Witcher 3", "19.99€", R.drawable.wicher3, "19.99€", Arrays.asList("PC")));
        listaNovedades.add(new Juego("Starfield", "60.0€", R.drawable.wicher3, "60.0€", Arrays.asList("PC")));
        listaNovedades.add(new Juego("Morio", "600.0€", R.drawable.wicher3, "600.0€", Arrays.asList("PC")));
        listaNovedades.add(new Juego("AgarthaBrainrot", "67.67€", R.drawable.wicher3, "67.67€", Arrays.asList("PC")));



        RecyclerView rvNovedades = view.findViewById(R.id.rv_novedades);

        CarouselLayoutManager layoutManager = new CarouselLayoutManager();

        layoutManager.setCarouselStrategy(new com.google.android.material.carousel.UncontainedCarouselStrategy());

        rvNovedades.setLayoutManager(layoutManager);
        JuegoAdapter adapter = new JuegoAdapter(listaNovedades, true);
        rvNovedades.setAdapter(adapter);


        return view;
    }
}
