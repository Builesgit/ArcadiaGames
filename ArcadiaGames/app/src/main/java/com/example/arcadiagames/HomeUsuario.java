package com.example.arcadiagames;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ValueAnimator;

import com.google.android.material.carousel.CarouselLayoutManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigationrail.NavigationRailView;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;

public class HomeUsuario extends AppCompatActivity {

    private Button btnAbrir;
    private NavigationRailView rail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homeusuario_layout);


        rail = findViewById(R.id.navigation_rail);
        btnAbrir = findViewById(R.id.btn_abrir_rail);
        float density = getResources().getDisplayMetrics().density;
        /*
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
        */
        //cerral navigator
        rail.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment seleccionado = null;
            seleccionado = new HomeFragment();

            if (item.getItemId() == R.id.item_menu) {
                seleccionado = new HomeFragment();

            } else if (id == R.id.item_catalogo) {
                seleccionado = new FragmentCatalogo();

            }

            if (seleccionado != null) {
                cargarFragmento(seleccionado);
                cerrarRail(); // Llamamos al método para animar el cierre
                return true;
            }
            return false; // Gestionar otros clics de menú aquí
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
    // Método para cambiar fragmentos
    private void cargarFragmento(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.main_home_U_fragment, fragment)
                .commit();
    }

    // Método para animar el cierre del Rail
    private void cerrarRail() {
        ValueAnimator animRail = ValueAnimator.ofInt(rail.getWidth(), 0);
        animRail.setDuration(200);
        animRail.addUpdateListener(animation -> {
            rail.getLayoutParams().width = (int) animation.getAnimatedValue();
            rail.requestLayout();
        });
        btnAbrir.animate().translationX(0).setDuration(200).start();
        animRail.start();
    }
/*ValueAnimator animRail = ValueAnimator.ofInt(rail.getWidth(), 0);
                animRail.setDuration(200);
                animRail.addUpdateListener(animation -> {
                    rail.getLayoutParams().width = (int) animation.getAnimatedValue();
                    rail.requestLayout();
                });

                btnAbrir.animate().translationX(0).setDuration(200).start();

                animRail.start();
                return true;*/


}
