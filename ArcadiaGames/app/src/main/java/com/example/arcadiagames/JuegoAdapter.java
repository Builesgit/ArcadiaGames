package com.example.arcadiagames;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class JuegoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Juego> listaJuego;
    public static final int VIEW_TYPE_LISTA = 1;
    public static final int VIEW_TYPE_CAROUSEL = 2;
    private final int tipoConfigurado;

    // Constructor para Catálogo
    public JuegoAdapter(List<Juego> listaJuego) {
        this.listaJuego = listaJuego;
        this.tipoConfigurado = VIEW_TYPE_LISTA;
    }

    // Constructor para Home (Carousel)
    public JuegoAdapter(List<Juego> listaJuego, boolean esCarousel) {
        this.listaJuego = listaJuego;
        this.tipoConfigurado = esCarousel ? VIEW_TYPE_CAROUSEL : VIEW_TYPE_LISTA;
    }

    // ESTO le dice al RecyclerView qué estamos dibujando
    @Override
    public int getItemViewType(int position) {
        return tipoConfigurado;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_CAROUSEL) {
            View v = inflater.inflate(R.layout.item_novedades_carrusel, parent, false);
            return new CarouselViewHolder(v);
        } else {
            View v = inflater.inflate(R.layout.item_juego_catalogo_u, parent, false);
            return new ListaViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Juego juego = listaJuego.get(position);

        if (holder instanceof CarouselViewHolder) {
            CarouselViewHolder h = (CarouselViewHolder) holder;
            h.tvTitulo.setText(juego.getNombre());
            h.tvPrecio.setText(juego.getPrecio());
            h.ivPortada.setImageResource(juego.getImagenResId());
        }
        else if (holder instanceof ListaViewHolder) {
            ListaViewHolder h = (ListaViewHolder) holder;
            h.tvTitulo.setText(juego.getNombre());
            h.tvPrecio.setText(juego.getPrecio());
            h.tvDescripcion.setText(juego.getDescripcion());
            h.ivPortada.setImageResource(juego.getImagenResId());
            if (juego.getTags() != null) {
                h.tvTags.setText(String.join(" • ", juego.getTags()));
            }
        }
    }

    @Override
    public int getItemCount() {
        return listaJuego.size();
    }

    public void setFilteredList(List<Juego> filteredList) {
        this.listaJuego = filteredList;
        notifyDataSetChanged();
    }

    // VIEWHOLDER 1: Solo tiene lo que usa el Carousel
    static class CarouselViewHolder extends RecyclerView.ViewHolder {

        ImageView ivPortada;
        TextView tvTitulo, tvPrecio;

        public CarouselViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPortada = itemView.findViewById(R.id.img_juego);
            tvTitulo = itemView.findViewById(R.id.tv_nombre_jc);
            tvPrecio = itemView.findViewById(R.id.tv_precio_jc);
        }
    }

    // VIEWHOLDER 2: Tiene todo lo del Catálogo
    static class ListaViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPortada;
        TextView tvTitulo, tvDescripcion, tvPrecio, tvTags;

        public ListaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPortada = itemView.findViewById(R.id.iv_juego_portada);
            tvTitulo = itemView.findViewById(R.id.tv_juego_titulo);
            tvDescripcion = itemView.findViewById(R.id.tv_juego_descripcion);
            tvPrecio = itemView.findViewById(R.id.tv_juego_precio);
            tvTags = itemView.findViewById(R.id.tv_juego_tags);
        }
    }
}