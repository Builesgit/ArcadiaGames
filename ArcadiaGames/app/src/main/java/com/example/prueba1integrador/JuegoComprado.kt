package com.example.prueba1integrador

data class JuegoComprado(
    val juego: Juego,
    val fechaCompra: Long? = null,
    val fechaInicioMillis: Long? = null,
    val fechaFinMillis: Long? = null,
    val esAlquiler: Boolean = false
)