package com.example.prueba1integrador

data class JuegoComprado(
    val juego: Juego,
    val fechaCompra: Long? = null,
    val fechaInicio: Long? = null,
    val fechaFin: Long? = null,
    val esAlquiler: Boolean = false
)
