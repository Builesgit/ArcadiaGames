package com.example.arcadiagames;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class CrearCuenta extends AppCompatActivity {

    private Button crear_cuenta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crearcuenta_layout);

        crear_cuenta = findViewById(R.id.boton_crear_cuenta);

        crear_cuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CrearCuenta.this, HomeUsuario.class);
                startActivity(intent);
            }
        });
    }
}
