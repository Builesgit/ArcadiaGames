package com.example.prueba1integrador

class CestaManager {
    private val _items = mutableListOf<Juego>()
    val items: List<Juego> get() = _items

    fun add(juego: Juego) {
        _items.add(juego)
    }

    fun removeAt(position: Int) {
        if (position in _items.indices) {
            _items.removeAt(position)
        }
    }

    fun clear() = _items.clear()

    fun total(): Double {
        return _items.sumOf { juego ->
            // Limpieza robusta del String de precio
            juego.precio
                .replace("€", "")
                .replace(",", ".")
                .replace("[^0-9.]".toRegex(), "") // Elimina cualquier otro caracter no numérico
                .trim()
                .toDoubleOrNull() ?: 0.0
        }
    }
}