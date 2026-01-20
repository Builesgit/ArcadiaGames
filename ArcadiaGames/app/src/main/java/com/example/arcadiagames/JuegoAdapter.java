package com.example.arcadiagames;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class JuegoAdapter extends RecyclerView.Adapter<JuegoAdapter.JuegoViewHolder> {

    private List<Juego> listaJuego;
    private boolean modoCompacto = false; //esto es para el modo de la lista para el carrusel

    public JuegoAdapter(List<Juego> listaJuego) {
        this.listaJuego = listaJuego;
        this.modoCompacto = false;
    }

    // este en principio es para el carrusel
    public JuegoAdapter(List<Juego> listaJuego, boolean modoCompacto) {
        this.listaJuego = listaJuego;
        this.modoCompacto = modoCompacto;
    }



    // Método para actualizar la lista cuando filtramos
    public void setFilteredList(List<Juego> filteredList) {
        this.listaJuego = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public JuegoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_juego_catalogo_u, parent, false);
        return new JuegoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JuegoViewHolder holder, int position) {
        Juego juego = listaJuego.get(position);

        holder.tvTitulo.setText(juego.getNombre());
        holder.tvPrecio.setText(juego.getPrecio());
        holder.ivPortada.setImageResource(juego.getImagenResId());

        // Unimos los tags en un solo String separado por comas o puntos
        if (modoCompacto) {
            holder.tvDescripcion.setVisibility(View.GONE);
            holder.tvTags.setVisibility(View.GONE);
            // Puedes ocultar también el texto extra si quieres
        } else {
            holder.tvDescripcion.setVisibility(View.VISIBLE);
            holder.tvTags.setVisibility(View.VISIBLE);
            holder.tvDescripcion.setText(juego.getDescripcion());

            if (juego.getTags() != null) {
                holder.tvTags.setText(String.join(" • ", juego.getTags()));
            }
        }
    }

    @Override
    public int getItemCount() {
        return listaJuego.size();
    }

    static class JuegoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPortada;
        TextView tvTitulo, tvDescripcion, tvPrecio, tvTags;

        public JuegoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPortada = itemView.findViewById(R.id.iv_juego_portada);
            tvTitulo = itemView.findViewById(R.id.tv_juego_titulo);
            tvDescripcion = itemView.findViewById(R.id.tv_juego_descripcion);
            tvPrecio = itemView.findViewById(R.id.tv_juego_precio);
            tvTags = itemView.findViewById(R.id.tv_juego_tags);
        }
    }


}