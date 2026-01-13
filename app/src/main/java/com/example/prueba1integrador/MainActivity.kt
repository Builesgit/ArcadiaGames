package com.example.prueba1integrador

import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.prueba1integrador.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Configurar UI básica de inmediato
        setupInitialState()

        // 2. Cargar el GIF con un pequeño delay para dejar respirar al procesador
        binding.root.post {
            loadGifOptimized()
        }

        // 3. Ejecutar transición
        binding.root.postDelayed({
            executeFlipTransition()
        }, 6000) // Aqui cambiar el tiempo del gif
    }

    private fun setupInitialState() {
        val scale = resources.displayMetrics.density
        val distance = 8000 * scale

        binding.initialScreenLayout.cameraDistance = distance
        binding.loginScreenLayout.cameraDistance = distance
        binding.loginScreenLayout.visibility = View.GONE
    }

    private fun loadGifOptimized() {
        // Al quitar centerCrop y usar DiskCacheStrategy.RESOURCE,
        // Glide hace mucho menos trabajo de CPU.
        Glide.with(this)
            .asGif()
            .load(R.drawable.prueba3)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE) // Cachea el GIF ya decodificado
            .into(binding.logoImageView)
    }

    private fun executeFlipTransition() {
        val duration = 600L
        val interpolator = AccelerateDecelerateInterpolator()

        binding.initialScreenLayout.animate()
            .rotationY(90f)
            .setDuration(duration)
            .setInterpolator(interpolator)
            .withEndAction {
                binding.initialScreenLayout.visibility = View.GONE

                binding.loginScreenLayout.apply {
                    rotationY = -90f
                    visibility = View.VISIBLE
                    animate()
                        .rotationY(0f)
                        .setDuration(duration)
                        .setInterpolator(interpolator)
                        .start()
                }
            }
            .start()
    }
}