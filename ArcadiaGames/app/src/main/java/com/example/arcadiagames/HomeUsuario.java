package com.example.arcadiagames;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.animation.ValueAnimator;
import com.google.android.material.navigationrail.NavigationRailView;


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

        //cerral navigator
        cargarFragmento(new HomeFragment());
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

            abrirNavigator(density);
        });
    }

    private void abrirNavigator(float density) {
        ValueAnimator animRail = ValueAnimator.ofInt(0, (int) (72 * density));
        animRail.setDuration(50);
        animRail.addUpdateListener(animation -> {
            rail.getLayoutParams().width = (int) animation.getAnimatedValue();
            rail.requestLayout();
        });
        btnAbrir.animate().translationX(-100 * density).setDuration(300).start();
        animRail.start();
    }


    // Método para cambiar fragmentos
    private void cargarFragmento(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.main_home_U_fragment, fragment)
                .commit();
    }

    // Método para animar el cierre del Rail, creo que habría que cambiarlo porque va muy lento, especialmente en la pestaña de catalogo, siguen siendo ambos métodos, los de abrir y cerrar algo lentos
    private void cerrarRail() {
        ValueAnimator animRail = ValueAnimator.ofInt(rail.getWidth(), 0);
        animRail.setDuration(50);
        animRail.addUpdateListener(animation -> {
            rail.getLayoutParams().width = (int) animation.getAnimatedValue()/4;
            rail.requestLayout();
        });
        btnAbrir.animate().translationX(0).setDuration(200).start();
        animRail.start();
    }
}
