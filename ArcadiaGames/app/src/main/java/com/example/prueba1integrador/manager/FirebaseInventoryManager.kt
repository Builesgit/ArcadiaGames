package com.example.prueba1integrador.manager

import android.net.Uri
import com.example.prueba1integrador.model.AccionHistorial
import com.example.prueba1integrador.model.Juego
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.lang.Exception

class FirebaseInventoryManager {

    private val dbReference = Firebase.database.getReference("productos")
    private val storageReference = FirebaseStorage.getInstance().reference.child("imagenes_productos")

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

    interface DeleteCallback {
        fun onDeleteComplete(exito: Boolean)
    }

    fun subirImagen(imageUri: Uri, callback: ImageUploadCallback) {
        val fileName = "img_${System.currentTimeMillis()}.jpg"
        val fileRef = storageReference.child(fileName)

        fileRef.putFile(imageUri).addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                callback.onUrlLoaded(uri.toString())
            }
        }.addOnFailureListener { e ->
            callback.onError("Error subiendo imagen: ${e.message}")
        }
    }

    fun subirProducto(videojuego: Juego, callback: ProductSaveCallback) {
        // 1. Edición: Si tiene ID, guardamos directamente
        if (videojuego.id.isNotEmpty()) {
            dbReference.child(videojuego.id).setValue(videojuego)
                .addOnSuccessListener { callback.onSaveComplete(true) }
                .addOnFailureListener { callback.onSaveComplete(false) }
            return
        }

        // 2. Nuevo Producto: Validamos que el nombre no exista (case insensitive)
        dbReference.get().addOnSuccessListener { snapshot ->
            var nombreDuplicado = false
            val nuevoNombreLimpio = videojuego.nombre.trim().lowercase()

            for (data in snapshot.children) {
                val nombreDB = data.child("nombre").getValue(String::class.java)?.trim()?.lowercase()
                if (nombreDB == nuevoNombreLimpio) {
                    nombreDuplicado = true
                    break
                }
            }

            if (nombreDuplicado) {
                callback.onSaveComplete(false)
            } else {
                val id = dbReference.push().key ?: return@addOnSuccessListener
                val productoConId = videojuego.copy(id = id)

                dbReference.child(id).setValue(productoConId)
                    .addOnSuccessListener {
                        registrarEnHistorial("Admin", "Añadió nuevo producto", videojuego.nombre, videojuego.stock)
                        callback.onSaveComplete(true)
                    }
                    .addOnFailureListener { callback.onSaveComplete(false) }
            }
        }.addOnFailureListener { callback.onSaveComplete(false) }
    }

    fun consultarInventario(callback: InventoryCallback) {
        dbReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaJuegos = mutableListOf<Juego>()
                for (data in snapshot.children) {
                    try {
                        val juego = data.getValue(Juego::class.java)
                        if (juego != null) listaJuegos.add(juego)
                    } catch (e: Exception) { e.printStackTrace() }
                }
                callback.onDataLoaded(listaJuegos)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Busca esta función en FirebaseInventoryManager.kt y reemplázala:
    fun registrarEnHistorial(nombreUser: String, accion: String, producto: String, cant: Int = 1) {
        val ref = FirebaseDatabase.getInstance().getReference("historial")
        val idLog = ref.push().key ?: return

        // GUARDAR ACCIÓN PURA: Sin añadidos manuales aquí
        val nuevoLog = AccionHistorial(
            id = idLog,
            usuarioNombre = nombreUser,
            accion = accion, // Aquí llegará solo "compró", "vació", etc.
            productoNombre = producto,
            cantidad = cant,
            fecha = System.currentTimeMillis()
        )
        ref.child(idLog).setValue(nuevoLog)
    }

    fun eliminarProducto(idJuego: String, callback: DeleteCallback) {
        dbReference.child(idJuego).removeValue()
            .addOnSuccessListener { callback.onDeleteComplete(true) }
            .addOnFailureListener { callback.onDeleteComplete(false) }
    }
}