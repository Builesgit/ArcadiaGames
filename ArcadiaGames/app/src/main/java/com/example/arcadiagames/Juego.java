package com.example.arcadiagames;

public class Juego {
    private String nombre;
    private String precio;
    private int imagenResId; // Por ahora usamos el ID de R.drawable

    public Juego(String nombre, String precio, int imagenResId) {
        this.nombre = nombre;
        this.precio = precio;
        this.imagenResId = imagenResId;
    }

    public String getNombre() { return nombre; }
    public String getPrecio() { return precio; }
    public int getImagenResId() { return imagenResId; }
}

