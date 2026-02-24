package com.example.prueba1integrador

import java.io.Serializable

data class Alquiler(
    val alquilerId: String = "",
    val juegoId: String = "",
    val nombreJuego: String = "",
    val idUsuario: String = "",
    val nombreUsuario: String = "",
    val fechaInicio: String = "",
    val fechaFin: String = "",
    val dias: Long = 0,
    val precioTotal: Double = 0.0,
    val timestamp: Long = 0
) : Serializable