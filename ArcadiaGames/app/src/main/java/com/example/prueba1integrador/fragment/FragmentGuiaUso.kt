package com.example.prueba1integrador.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.prueba1integrador.R

class FragmentGuiaUso : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Simplemente inflamos el layout de la guía
        return inflater.inflate(R.layout.fragment_guia_uso, container, false)
    }
}