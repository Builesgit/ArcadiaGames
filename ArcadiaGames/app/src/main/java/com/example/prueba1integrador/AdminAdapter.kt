package com.example.prueba1integrador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdminAdapter(
    private val listaAdmins: List<Usuario>,
    private val onDegradarClick: (Usuario) -> Unit
) : RecyclerView.Adapter<AdminAdapter.AdminViewHolder>() {

    class AdminViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // CAMBIO AQUÍ: Debe coincidir con el ID de item_admin.xml
        val tvEmail: TextView = view.findViewById(R.id.tvEmailAdminItem)
        val btnDegradar: Button = view.findViewById(R.id.btnDegradarAdmin)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin, parent, false)
        return AdminViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminViewHolder, position: Int) {
        val admin = listaAdmins[position]
        // Se muestra el campo 'usuario' que contiene el email
        holder.tvEmail.text = admin.usuario
        holder.btnDegradar.setOnClickListener { onDegradarClick(admin) }
    }

    override fun getItemCount() = listaAdmins.size
}