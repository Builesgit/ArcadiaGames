package com.example.arcadiagames;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ValueAnimator;

import com.google.android.material.carousel.CarouselLayoutManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigationrail.NavigationRailView;

import java.util.ArrayList;
import java.util.List;

public class HomeUsuario extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homeusuario_layout);


        NavigationRailView rail = findViewById(R.id.navigation_rail);
        Button btnAbrir = findViewById(R.id.btn_abrir_rail);
        float density = getResources().getDisplayMetrics().density;

        List<Juego> listaNovedades = new ArrayList<>();
        listaNovedades.add(new Juego("Cybercriminal 2077", "29.99€", R.drawable.wicher3));
        listaNovedades.add(new Juego("The Witcher 3", "19.99€", R.drawable.wicher3));
        listaNovedades.add(new Juego("Starfield", "60.0€", R.drawable.wicher3));
        listaNovedades.add(new Juego("Morio", "600.0€", R.drawable.wicher3));
        listaNovedades.add(new Juego("AgarthaBrainrot", "67.67€", R.drawable.wicher3));


        // 1. Buscamos el RecyclerView
        RecyclerView rvNovedades = findViewById(R.id.rv_novedades);
        rvNovedades = findViewById(R.id.rv_novedades);

        // 2. Creamos el LayoutManager del carrusel
        CarouselLayoutManager layoutManager = new CarouselLayoutManager();

        layoutManager.setCarouselStrategy(new com.google.android.material.carousel.UncontainedCarouselStrategy());
        //hay diferentes tipos de carrusel, UncontainedCarouselStrategy() es el que creo que es el mejor, per también está CarouselLayoutManager()


        // 4. Lo asignamos al RecyclerView
        rvNovedades.setLayoutManager(layoutManager);
        JuegoAdapter adapter = new JuegoAdapter(listaNovedades);
        rvNovedades.setAdapter(adapter);

        //cerral navigator
        rail.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.item_menu) {

                ValueAnimator animRail = ValueAnimator.ofInt(rail.getWidth(), 0);
                animRail.setDuration(200);
                animRail.addUpdateListener(animation -> {
                    rail.getLayoutParams().width = (int) animation.getAnimatedValue();
                    rail.requestLayout();
                });


                btnAbrir.animate().translationX(0).setDuration(200).start();

                animRail.start();
                return true;
            }

            if (item.getItemId() == R.id.item_catalogo) {

            }
            return true; // Gestionar otros clics de menú aquí
        });

        //abril navigator
        btnAbrir.setOnClickListener(v -> {
            ValueAnimator animRail = ValueAnimator.ofInt(0, (int) (72 * density));
            animRail.setDuration(200);
            animRail.addUpdateListener(animation -> {
                rail.getLayoutParams().width = (int) animation.getAnimatedValue();
                rail.requestLayout();
            });


            btnAbrir.animate().translationX(-100 * density).setDuration(300).start();

            animRail.start();
        });


    }




}
