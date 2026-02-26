package com.example.prueba1integrador.model

// Usamos String para el ID porque Firebase usa UIDs alfanuméricos
// Añadimos valores por defecto (como "" o false) para que Firebase pueda crear el objeto vacío
data class Usuario(
    val id: String = "",
    val nombre: String = "", // Este será el "Nombre de Usuario"
    val correo: String = "", // Este será el "Correo"
    val rol: String = "user",
    val esJefe: Boolean = false
)