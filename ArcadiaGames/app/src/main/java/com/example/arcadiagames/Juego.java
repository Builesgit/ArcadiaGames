package com.example.arcadiagames;

import java.util.List;

public class Juego {
    private String nombre;
    private String descripcion;
    private String precio;
    private int imagenResId;
    private List<String> tags; // Aquí irán "PolyStation", "MeloInvento", etc.

    public Juego(String nombre, String descripcion, int imagenResId, String precio, List<String> tags) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.imagenResId = imagenResId;
        this.precio = precio;
        this.tags = tags;
    }

    // Getters
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getPrecio() { return precio; }
    public int getImagenResId() { return imagenResId; }
    public List<String> getTags() { return tags; }
}

