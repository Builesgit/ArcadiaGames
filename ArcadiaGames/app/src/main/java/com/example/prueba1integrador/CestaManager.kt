package com.example.prueba1integrador


class CestaManager {
    private val _items = mutableListOf<Juego>()
    val items: List<Juego> get() = _items

    fun add(juego: Juego) {
        _items.add(juego)
    }

    fun removeAt(position: Int) {
        if (position in _items.indices) _items.removeAt(position)
    }

    fun clear() = _items.clear()

    fun total(): Double {
        // En tu modelo precio es String (ej: "59.99€" o "59.99")
        return _items.sumOf { juego ->
            juego.precio
                .replace("€", "")
                .replace(",", ".")
                .trim()
                .toDoubleOrNull() ?: 0.0
        }
    }
}
