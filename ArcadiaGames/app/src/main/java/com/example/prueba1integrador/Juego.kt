package com.example.prueba1integrador

data class Juego(
    val nombre: String,
    val descripcion: String,
    val imagenResId: Int,
    val precio: String,
    val tags: List<String> = emptyList()
)