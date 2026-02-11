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

    // --- INTERFACES ( callbacks clásicos ) ---
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

    // --- MÉTODOS DE IMAGEN ---
    fun subirImagen(imageUri: Uri, callback: ImageUploadCallback) {
        val fileName = "img_${System.currentTimeMillis()}.jpg"
        val fileRef = storageReference.child(fileName)

        fileRef.putFile(imageUri).addOnSuccessListener(object : OnSuccessListener<UploadTask.TaskSnapshot> {
            override fun onSuccess(taskSnapshot: UploadTask.TaskSnapshot?) {
                fileRef.downloadUrl.addOnSuccessListener(object : OnSuccessListener<Uri> {
                    override fun onSuccess(uri: Uri?) {
                        callback.onUrlLoaded(uri.toString())
                    }
                })
            }
        }).addOnFailureListener(object : OnFailureListener {
            override fun onFailure(e: Exception) {
                callback.onError("Error subiendo imagen: ${e.message}")
            }
        })
    }

    // --- MÉTODOS DE PRODUCTO ---
    fun subirProducto(videojuego: Juego, callback: ProductSaveCallback) {
        val id = videojuego.id.ifEmpty { dbReference.push().key ?: "" }
        val productoConId = videojuego.copy(id = id)

        dbReference.child(id).setValue(productoConId)
            .addOnSuccessListener(object : OnSuccessListener<Void> {
                override fun onSuccess(aVoid: Void?) {
                    // Se registra la acción. El adapter/activity determinará si es "añadió" o "actualizó"
                    registrarEnHistorial("Admin", "gestionó producto", videojuego.nombre, videojuego.stock)
                    callback.onSaveComplete(true)
                }
            })
            .addOnFailureListener(object : OnFailureListener {
                override fun onFailure(e: Exception) {
                    callback.onSaveComplete(false)
                }
            })
    }

    fun consultarInventario(callback: InventoryCallback) {
        dbReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaJuegos = mutableListOf<Juego>()
                for (data in snapshot.children) {
                    try {
                        val juego = data.getValue(Juego::class.java)
                        if (juego != null) {
                            listaJuegos.add(juego)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                callback.onDataLoaded(listaJuegos)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun eliminarProducto(idJuego: String, callback: DeleteCallback) {
        dbReference.child(idJuego).removeValue()
            .addOnSuccessListener(object : OnSuccessListener<Void> {
                override fun onSuccess(aVoid: Void?) {
                    callback.onDeleteComplete(true)
                }
            })
            .addOnFailureListener(object : OnFailureListener {
                override fun onFailure(e: Exception) {
                    callback.onDeleteComplete(false)
                }
            })
    }

    // --- MÉTODOS DE HISTORIAL Y ALQUILERES ---

    fun registrarEnHistorial(nombreUser: String, accion: String, producto: String, cant: Int = 1) {
        val ref = FirebaseDatabase.getInstance().getReference("historial")
        val idLog = ref.push().key ?: return

        // Creamos el objeto siguiendo tu data class AccionHistorial
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

                                // CAMBIO AQUÍ: Mensaje específico de devolución
                                registrarEnHistorial("Sistema", "Alquiler devuelto", nombreJuego)
                            }
                        }
                    }
                }
            }
        }
    }
}