package com.example.arcadiagames;

import java.util.List;

public class Juegos {
    private String nombre;
    private String precio;
    private int imagenResId;

    public Juegos(String nombre, String precio, int imagenResId) {
        this.nombre = nombre;
        this.precio = precio;
        this.imagenResId = imagenResId;
    }

    // Getters
    public String getNombre() { return nombre; }
    public String getPrecio() { return precio; }
    public int getImagenResId() { return imagenResId; }
}
