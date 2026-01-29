package com.example.prueba1integrador

data class Juego(
    var id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val imagenResId: Int = 0, // Campo nuevo para recursos locales
    val imagenUrl: String = "",
    val precio: String = "",
    val tags: List<String> = emptyList(),
    val categoria: String = "",
    val plataforma: String = "",
    val stock: String = "En stock"
) : java.io.Serializable