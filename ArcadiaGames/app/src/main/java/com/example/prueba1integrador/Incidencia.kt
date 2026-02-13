package com.example.prueba1integrador

data class Incidencia(
    val id: String? = null,
    val tema: String = "",
    val descripcion: String = "",
    val infoAdicional: String = "",
    val tipo: String = "",
    val usuarioId: String = "",
    val usuarioEmail: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : java.io.Serializable