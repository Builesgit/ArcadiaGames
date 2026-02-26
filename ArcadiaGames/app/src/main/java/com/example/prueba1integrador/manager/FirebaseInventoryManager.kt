package com.example.prueba1integrador.manager

import android.net.Uri
import com.example.prueba1integrador.model.AccionHistorial
import com.example.prueba1integrador.model.Juego
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

    // --- LÓGICA DE ESTADÍSTICAS ---
    fun registrarEventoEstadistico(idJuego: String, tipo: String, cantidad: Int = 1) {
        val campoProducto = if (tipo == "VENTA") "rendimiento_ventas" else "rendimiento_vistas"

        // Incremento atómico en el producto
        dbReference.child(idJuego).child(campoProducto).setValue(ServerValue.increment(cantidad.toLong()))

        // Incremento en el nodo global de estadísticas
        val updates = hashMapOf<String, Any>(
            "$tipo/total" to ServerValue.increment(cantidad.toLong()),
            "$tipo/ultima_actualizacion" to ServerValue.TIMESTAMP
        )
        statsReference.child("por_producto").child(idJuego).updateChildren(updates)
    }

    fun registrarVista(idJuego: String) = registrarEventoEstadistico(idJuego, "VISTA")
    fun registrarVentaMecanica(idJuego: String, cantidad: Int) = registrarEventoEstadistico(idJuego, "VENTA", cantidad)

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

    // --- GESTIÓN DE PRODUCTOS ---
    fun subirImagen(imageUri: Uri, callback: ImageUploadCallback) {
        val fileName = "img_${System.currentTimeMillis()}.jpg"
        val fileRef = storageReference.child(fileName)
        fileRef.putFile(imageUri).addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { uri -> callback.onUrlLoaded(uri.toString()) }
        }.addOnFailureListener { callback.onError("Error: ${it.message}") }
    }

    fun subirProducto(videojuego: Juego, callback: ProductSaveCallback) {
        if (videojuego.id.isNotEmpty()) {
            dbReference.child(videojuego.id).setValue(videojuego)
                .addOnSuccessListener { callback.onSaveComplete(true) }
                .addOnFailureListener { callback.onSaveComplete(false) }
            return
        }
        val id = dbReference.push().key ?: return
        dbReference.child(id).setValue(videojuego.copy(id = id))
            .addOnSuccessListener {
                registrarEnHistorial("Admin", "Añadió nuevo producto", videojuego.nombre, videojuego.stock)
                callback.onSaveComplete(true)
            }
    }

    fun consultarInventario(callback: InventoryCallback) {
        dbReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback.onDataLoaded(snapshot.children.mapNotNull { it.getValue(Juego::class.java) })
            }
            override fun onCancelled(e: DatabaseError) {}
        })
    }

    fun registrarEnHistorial(nombreUser: String, accion: String, producto: String, cant: Int = 1) {
        val ref = FirebaseDatabase.getInstance().getReference("historial")
        val idLog = ref.push().key ?: return
        val nuevoLog = AccionHistorial(idLog, nombreUser, accion, producto, cant, System.currentTimeMillis())
        ref.child(idLog).setValue(nuevoLog)
    }

    fun eliminarProducto(idJuego: String, callback: ProductSaveCallback) {
        dbReference.child(idJuego).removeValue().addOnCompleteListener { callback.onSaveComplete(it.isSuccessful) }
    }
}