package com.example.prueba1integrador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdminAdapter(
    private val listaAdmins: List<Usuario>,
    private val onDegradarClick: (Usuario) -> Unit // Acción para quitar el rango
) : RecyclerView.Adapter<AdminAdapter.AdminViewHolder>() {

    class AdminViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Vinculamos con el ID del correo en el item
        val tvEmail: TextView = view.findViewById(R.id.tvEmailAdminItem)
        val btnDegradar: Button = view.findViewById(R.id.btnDegradarAdmin)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminViewHolder {
        // Inflamos el layout item_admin.xml
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin, parent, false)
        return AdminViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminViewHolder, position: Int) {
        val admin = listaAdmins[position]
        // MODIFICADO: Ahora usamos la propiedad .correo
        holder.tvEmail.text = admin.correo
        holder.btnDegradar.setOnClickListener { onDegradarClick(admin) }
    }

    override fun getItemCount() = listaAdmins.size
}