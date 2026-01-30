package com.example.prueba1integrador

import android.net.Uri
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
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
    // Definen los métodos que se ejecutarán al terminar las tareas asíncronas

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

    /**
     * Sube una imagen usando callbacks clásicos (Interfaces).
     */
    fun subirImagen(imageUri: Uri, callback: ImageUploadCallback) {
        val fileName = "img_${System.currentTimeMillis()}.jpg"
        val fileRef = storageReference.child(fileName)


        fileRef.putFile(imageUri).addOnSuccessListener(object : OnSuccessListener<UploadTask.TaskSnapshot> {
            override fun onSuccess(taskSnapshot: UploadTask.TaskSnapshot?) {
                // Si sube bien, pedimos la URL pública
                fileRef.downloadUrl.addOnSuccessListener(object : OnSuccessListener<Uri> {
                    override fun onSuccess(uri: Uri?) {
                        val urlString = uri.toString()
                        callback.onUrlLoaded(urlString)
                    }
                })
            }
        }).addOnFailureListener(object : OnFailureListener {
            override fun onFailure(e: Exception) {
                callback.onError("Error subiendo imagen: ${e.message}")
            }
        })
    }

    /**
     * Sube un producto usando callbacks clásicos.
     */
    fun subirProducto(videojuego: Juego, callback: ProductSaveCallback) {
        val id = videojuego.id.ifEmpty { dbReference.push().key ?: "" }
        val productoConId = videojuego.copy(id = id)

        dbReference.child(id).setValue(productoConId)
            .addOnSuccessListener(object : OnSuccessListener<Void> {
                override fun onSuccess(aVoid: Void?) {
                    callback.onSaveComplete(true)
                }
            })
            .addOnFailureListener(object : OnFailureListener {
                override fun onFailure(e: Exception) {
                    callback.onSaveComplete(false)
                }
            })
    }

    /**
     * Consulta el inventario usando callbacks clásicos.
     */
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
                        // Si hay un dato corrupto o antiguo, lo ignoramos para que no cierre la app
                        e.printStackTrace()
                    }
                }
                callback.onDataLoaded(listaJuegos)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    // --- Nueva Interfaz para Eliminar ---
    interface DeleteCallback {
        fun onDeleteComplete(exito: Boolean)
    }

    /**
     * Elimina un producto de la base de datos por su ID.
     */
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
}