package com.example.prueba1integrador

data class AccionHistorial(
    val id: String = "",
    val usuarioNombre: String = "",
    val accion: String = "", // Ejemplo: "compró", "añadió", "eliminó"
    val productoNombre: String = "",
    val cantidad: Int = 1,
    val fecha: Long = System.currentTimeMillis()
)
