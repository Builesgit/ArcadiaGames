package com.example.prueba1integrador

import java.io.Serializable

data class Incidencia(
    var id: String? = null,
    val tema: String = "",
    val descripcion: String = "",
    val infoAdicional: String = "",
    val tipo: String = "",
    val usuarioId: String = "",
    val usuarioEmail: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Serializable