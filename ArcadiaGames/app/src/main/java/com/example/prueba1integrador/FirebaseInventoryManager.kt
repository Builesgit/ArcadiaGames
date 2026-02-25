package com.example.prueba1integrador

import android.net.Uri
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class FirebaseInventoryManager {

    private val dbReference = FirebaseDatabase.getInstance().getReference("productos")
    private val storageReference = FirebaseStorage.getInstance().reference.child("imagenes_productos")
    private val statsReference = FirebaseDatabase.getInstance().getReference("estadisticas")

    interface ImageUploadCallback {
        fun onUrlLoaded(url: String)
        fun onError(mensaje: String)
    }

    interface ProductSaveCallback {
        fun onSaveComplete(exito: Boolean)
    }

    interface InventoryCallback {
        fun onDataLoaded(lista: List<Juego>)
    }

    // --- REGISTRO DE ESTADÍSTICAS DIARIAS ---
    fun registrarEventoEstadistico(idJuego: String, tipo: String, cantidad: Int = 1) {
        val updates = hashMapOf<String, Any>()
        val campoProducto = if (tipo == "VENTA") "rendimiento_ventas" else "rendimiento_vistas"

        // Actualiza el contador en el producto
        dbReference.child(idJuego).child(campoProducto).setValue(ServerValue.increment(cantidad.toLong()))

        // Actualiza la tabla independiente
        val nodoStats = statsReference.child("por_producto").child(idJuego)
        updates["$tipo/total"] = ServerValue.increment(cantidad.toLong())
        updates["$tipo/ultima_actualizacion"] = ServerValue.TIMESTAMP
        nodoStats.updateChildren(updates)
    }

    fun registrarVista(idJuego: String) {
        registrarEventoEstadistico(idJuego, "VISTA")
    }

    fun registrarVentaMecanica(idJuego: String, cantidadVendida: Int) {
        registrarEventoEstadistico(idJuego, "VENTA", cantidadVendida)
    }

    // --- CONSULTAS PARA EL DASHBOARD ---
    fun obtenerTopVentas(callback: InventoryCallback) {
        dbReference.orderByChild("rendimiento_ventas").limitToLast(5)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(s: DataSnapshot) {
                    val lista = s.children.mapNotNull { it.getValue(Juego::class.java) }.reversed()
                    callback.onDataLoaded(lista)
                }
                override fun onCancelled(e: DatabaseError) {}
            })
    }

    fun obtenerMenosVistos(callback: InventoryCallback) {
        dbReference.orderByChild("rendimiento_vistas").limitToFirst(5)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(s: DataSnapshot) {
                    val lista = s.children.mapNotNull { it.getValue(Juego::class.java) }
                    callback.onDataLoaded(lista)
                }
                override fun onCancelled(e: DatabaseError) {}
            })
    }

    // --- MÉTODOS DE GESTIÓN DE PRODUCTOS ---
    fun subirImagen(imageUri: Uri, callback: ImageUploadCallback) {
        val fileName = "img_${System.currentTimeMillis()}.jpg"
        val fileRef = storageReference.child(fileName)
        fileRef.putFile(imageUri).addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { uri -> callback.onUrlLoaded(uri.toString()) }
        }.addOnFailureListener { e -> callback.onError("Error: ${e.message}") }
    }

    fun subirProducto(videojuego: Juego, callback: ProductSaveCallback) {
        if (videojuego.id.isNotEmpty()) {
            dbReference.child(videojuego.id).setValue(videojuego)
                .addOnSuccessListener { callback.onSaveComplete(true) }
                .addOnFailureListener { callback.onSaveComplete(false) }
            return
        }
        dbReference.get().addOnSuccessListener { snapshot ->
            val nuevoNombreLimpio = videojuego.nombre.trim().lowercase()
            val duplicado = snapshot.children.any {
                it.child("nombre").getValue(String::class.java)?.trim()?.lowercase() == nuevoNombreLimpio
            }
            if (duplicado) callback.onSaveComplete(false)
            else {
                val id = dbReference.push().key ?: return@addOnSuccessListener
                dbReference.child(id).setValue(videojuego.copy(id = id))
                    .addOnSuccessListener {
                        registrarEnHistorial("Admin", "Añadió producto", videojuego.nombre, videojuego.stock)
                        callback.onSaveComplete(true)
                    }
            }
        }
    }

    fun consultarInventario(callback: InventoryCallback) {
        dbReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = snapshot.children.mapNotNull { it.getValue(Juego::class.java) }
                callback.onDataLoaded(lista)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun registrarEnHistorial(nombreUser: String, accion: String, producto: String, cant: Int = 1) {
        val ref = FirebaseDatabase.getInstance().getReference("historial")
        val idLog = ref.push().key ?: return
        val nuevoLog = AccionHistorial(idLog, nombreUser, accion, producto, cant, System.currentTimeMillis())
        ref.child(idLog).setValue(nuevoLog)
    }

    fun eliminarProducto(idJuego: String, callback: (Boolean) -> Unit) {
        dbReference.child(idJuego).removeValue().addOnCompleteListener { callback(it.isSuccessful) }
    }
}