package com.example.prueba1integrador

import android.net.Uri
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
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

    // --- MÉTODO MODIFICADO PARA NOMBRE ÚNICO ---
    fun subirProducto(videojuego: Juego, callback: ProductSaveCallback) {

        // 1. Si ya tiene ID, es una edición (viene de la pantalla de detalles/edición)
        if (videojuego.id.isNotEmpty()) {
            dbReference.child(videojuego.id).setValue(videojuego)
                .addOnSuccessListener { callback.onSaveComplete(true) }
                .addOnFailureListener { callback.onSaveComplete(false) }
            return
        }

        // 2. Si NO tiene ID, es un producto nuevo. Validamos que el nombre no exista.
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
                // REGLA: Si el nombre ya existe, NO permitimos añadir.
                // El Admin debe editar el existente.
                callback.onSaveComplete(false)
            } else {
                // Si el nombre es nuevo, creamos el registro
                val id = dbReference.push().key ?: return@addOnSuccessListener
                val productoConId = videojuego.copy(id = id)

                dbReference.child(id).setValue(productoConId)
                    .addOnSuccessListener { callback.onSaveComplete(true) }
                    .addOnFailureListener { callback.onSaveComplete(false) }
            }
        }.addOnFailureListener {
            callback.onSaveComplete(false)
        }
    }

    fun actualizarPreciosSegunPlataforma(callback: (Boolean) -> Unit) {
        dbReference.get().addOnSuccessListener { snapshot ->
            for (data in snapshot.children) {
                val juego = data.getValue(Juego::class.java)
                val id = data.key ?: continue
                if (juego != null) {
                    val nuevoPrecio = when {
                        juego.plataforma.contains("PlayStation", true) ||
                                juego.plataforma.contains("PC", true) -> "69.99 €"
                        juego.plataforma.contains("Xbox", true) -> "59.99 €"
                        juego.plataforma.contains("Nintendo", true) -> "49.99 €"
                        else -> juego.precio
                    }
                    dbReference.child(id).child("precio").setValue(nuevoPrecio)
                }
            }
            callback(true)
        }.addOnFailureListener { callback(false) }
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

    fun eliminarProducto(idJuego: String, callback: DeleteCallback) {
        dbReference.child(idJuego).removeValue()
            .addOnSuccessListener { callback.onDeleteComplete(true) }
            .addOnFailureListener { callback.onDeleteComplete(false) }
    }

    fun registrarEnHistorial(nombreUser: String, accion: String, producto: String, cant: Int = 1) {
        val ref = FirebaseDatabase.getInstance().getReference("historial")
        val idLog = ref.push().key ?: return
        val nuevoLog = AccionHistorial(
            id = idLog,
            usuarioNombre = nombreUser,
            accion = accion,
            productoNombre = producto,
            cantidad = cant,
            fecha = System.currentTimeMillis()
        )
        ref.child(idLog).setValue(nuevoLog)
    }

    fun revisarAlquileresVencidos() {
        val ahora = System.currentTimeMillis()
        val refAlquileres = FirebaseDatabase.getInstance().getReference("alquileres")
        refAlquileres.get().addOnSuccessListener { snapshot ->
            for (usuarioSnap in snapshot.children) {
                for (alquilerSnap in usuarioSnap.children) {
                    val expiracion = alquilerSnap.child("fechaExpiracion").getValue(Long::class.java) ?: 0L
                    val juegoId = alquilerSnap.child("juegoId").getValue(String::class.java) ?: ""
                    val nombreJuego = alquilerSnap.child("nombre").getValue(String::class.java) ?: "Juego"
                    val devuelto = alquilerSnap.child("devuelto").getValue(Boolean::class.java) ?: false

                    if (ahora > expiracion && !devuelto) {
                        val productoRef = dbReference.child(juegoId)
                        productoRef.child("stock").get().addOnSuccessListener { stockSnap ->
                            val stockActual = stockSnap.getValue(Int::class.java) ?: 0
                            productoRef.child("stock").setValue(stockActual + 1).addOnSuccessListener {
                                alquilerSnap.ref.child("devuelto").setValue(true)
                                registrarEnHistorial("Sistema", "Alquiler devuelto", nombreJuego)
                            }
                        }
                    }
                }
            }
        }
    }
}