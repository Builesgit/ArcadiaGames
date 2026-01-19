package com.example.arcadiagames;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.search.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.search.SearchBar; 

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FragmentCatalogo extends Fragment {

    private RecyclerView recyclerView;
    private JuegoAdapter adapter;
    private List<Juego> listaCompleta;
    private SearchBar searchBar;
    private ChipGroup chipGroup;
    private SearchView searchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_catalogo, container, false);

        // Inicializar vistas
        recyclerView = view.findViewById(R.id.rv_catalogo_completo);
        searchBar = view.findViewById(R.id.search_bar_catalogo);
        searchView = view.findViewById(R.id.search_view_catalogo);
        chipGroup = view.findViewById(R.id.chip_group_categorias);

        // 1. Crear la lista de datos (Mockup)
        llenarListaProvisional();

        // 2. Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new JuegoAdapter(new ArrayList<>(listaCompleta));
        recyclerView.setAdapter(adapter);

        // 3. Configurar Filtros
        configurarFiltros();
        searchView.setupWithSearchBar(searchBar);

        return view;
    }

    private void configurarFiltros() {
        // Escuchar cuando el usuario escribe en el SearchView
        searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Actualizamos el texto de la SearchBar para que se vea lo que escribimos al cerrar
                searchBar.setText(s.toString());
                filtrar(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Escuchar cambios en los Chips
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            // Obtenemos el texto actual del SearchView para no perder el filtro de nombre
            String textoBusqueda = searchView.getText().toString();
            filtrar(textoBusqueda);
        });
    }

    private void filtrar(String texto) {
        List<Juego> listaFiltrada = new ArrayList<>();

        // 1. Obtener Tags seleccionados
        List<String> etiquetasSeleccionadas = new ArrayList<>();
        for (int id : chipGroup.getCheckedChipIds()) {
            Chip chip = chipGroup.findViewById(id);
            if (chip != null) {
                etiquetasSeleccionadas.add(chip.getText().toString());
            }
        }

        // 2. Lógica de filtrado combinada
        for (Juego juego : listaCompleta) {
            boolean coincideNombre = juego.getNombre().toLowerCase().contains(texto.toLowerCase());

            boolean coincideChip = etiquetasSeleccionadas.isEmpty();
            if (!etiquetasSeleccionadas.isEmpty()) {
                for (String tag : juego.getTags()) {
                    if (etiquetasSeleccionadas.contains(tag)) {
                        coincideChip = true;
                        break;
                    }
                }
            }

            if (coincideNombre && coincideChip) {
                listaFiltrada.add(juego);
            }
        }

        // 3. Actualizar el adaptador
        adapter.setFilteredList(listaFiltrada);
    }

    private void llenarListaProvisional() {
        listaCompleta = new ArrayList<>();
        // Ejemplo de datos con los nuevos nombres
        listaCompleta.add(new Juego("Super Morio World", "C morió.", R.drawable.morio, "4000.99€", Arrays.asList("MeloInvento")));
        listaCompleta.add(new Juego("Poly Racing", "Carreras poligonales. Mira un vértice", R.drawable.polyracing, "0.09€", Arrays.asList("PolyStation")));
        listaCompleta.add(new Juego("Xbob El con-Xtructor", "Construye como nadie", R.drawable.wicher3, "49.99€", Arrays.asList("Xbob")));
        listaCompleta.add(new Juego("PC Master Race", "Racist era algo de carreras ¿no?.", R.drawable.pcmasterrace, "50.00€", Arrays.asList("PC")));
        listaCompleta.add(new Juego("Multi Plataforma", "Funciona en todo.", R.drawable.wicher3, "3.99€", Arrays.asList("PolyStation", "PC", "Xbob", "MeloInvento")));
        listaCompleta.add(new Juego("Facto Horio", "Tira factos en el horio.", R.drawable.wicher3, "39.99€", Arrays.asList("PolyStation", "PC")));

    }
}