package com.example.prueba1integrador.model

import java.io.Serializable

data class Juego(
    var id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val imagenResId: Int = 0,
    val imagenUrl: String = "",
    val precio: String = "",
    val tags: List<String> = emptyList(),
    val categoria: String = "",
    val plataforma: String = "",
    val stock: Int = 0,
    val detalle_stock: Map<String, Int>? = null,

    val rendimiento_vistas: Int = 0,
    val rendimiento_ventas: Int = 0,
    val precio_original: Double = 0.0,
    val ultima_actualizacion: Long = System.currentTimeMillis()
) : Serializable

data class ItemInventario(
    val juego: Juego,
    val cantidad: Int,
    val idsAgrupados: List<String>,
    val ps: Int = 0,
    val xb: Int = 0,
    val ni: Int = 0,
    val pc: Int = 0,

    val totalVistas: Int = 0,
    val totalVentas: Int = 0
) : Serializable