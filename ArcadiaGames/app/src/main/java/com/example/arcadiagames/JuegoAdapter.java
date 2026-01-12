package com.example.arcadiagames;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide; // Importamos Glide
import java.util.List;

public class JuegoAdapter extends RecyclerView.Adapter<JuegoAdapter.JuegoViewHolder> {

    private List<Juego> listaJuegos;

    public JuegoAdapter(List<Juego> listaJuegos) {
        this.listaJuegos = listaJuegos;
    }

    @NonNull
    @Override
    public JuegoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_novedades_carrusel, parent, false);
        return new JuegoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JuegoViewHolder holder, int position) {
        if (listaJuegos == null || listaJuegos.isEmpty()) return;

        // Ahora usamos la posición real, sin trucos de módulo
        Juego juegoActual = listaJuegos.get(position);

        holder.tvNombre.setText(juegoActual.getNombre());
        holder.tvPrecio.setText(juegoActual.getPrecio());

        // Cargamos la imagen de forma optimizada
        Glide.with(holder.itemView.getContext())
                .load(juegoActual.getImagenResId())
                .centerCrop()
                .into(holder.imgJuego);
    }

    @Override
    public int getItemCount() {
        // Devolvemos el tamaño real de la lista
        return listaJuegos != null ? listaJuegos.size() : 0;
    }

    class JuegoViewHolder extends RecyclerView.ViewHolder {
        ImageView imgJuego;
        TextView tvNombre, tvPrecio;

        public JuegoViewHolder(View itemView) {
            super(itemView);
            imgJuego = itemView.findViewById(R.id.img_juego);
            tvNombre = itemView.findViewById(R.id.tv_nombre_jc);
            tvPrecio = itemView.findViewById(R.id.tv_precio_jc);
        }
    }
}
