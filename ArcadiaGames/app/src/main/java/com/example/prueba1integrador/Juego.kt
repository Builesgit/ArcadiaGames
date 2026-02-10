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
    val stock: Int = 1
) : java.io.Serializable

// Clase de datos auxiliar para manejar Juego + Cantidad
data class ItemInventario(
    val juego: Juego,
    val cantidad: Int,
    val idsAgrupados: List<String> // Guardamos todos los IDs que forman este grupo
)