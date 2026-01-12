package com.example.arcadiagames;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    private EditText usuario, contraseña;
    private TextInputLayout layoutusuario, layoutcontraseña;
    private Button boton, crear_cuenta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        usuario = findViewById(R.id.TextField_Usuario);
        contraseña = findViewById(R.id.TextField_Contraseña);
        layoutusuario = findViewById(R.id.TextLayout_Usuario);
        layoutcontraseña = findViewById(R.id.TextLayout_Contraseña);
        //boton = findViewById(R.id.boton);
        crear_cuenta = findViewById(R.id.boton2);

        crear_cuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CrearCuenta.class);
                startActivity(intent);
            }
        });
/*

        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = usuario.getText().toString();
                String pass = contraseña.getText().toString();

                String mensajeEG = "Hay campos vacíos, por favor introduzca los datos solicitados";
                String usuario_ya_registrado = "El Usuario introducido ya está registrado";
                String contraseña_vacio = "El campo de contraseña está vacío";

                if (user.isEmpty() || pass.isEmpty()) {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), mensajeEG, Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(getColor(R.color.Oscuro));
                    snackbar.show();
                    layoutusuario.setError("Campo vacío");
                    layoutcontraseña.setError("Campo vacío");

                    if (user.isEmpty()) {
                        layoutusuario.setError("Campo vacío");
                    }
                    if (pass.isEmpty()) {
                        layoutcontraseña.setError("Campo vacío");
                    }


                } else {
                    layoutusuario.setError(null);
                    layoutcontraseña.setError(null);
                    if (DatosUsuarios.UsuarioEstaRegistrado(user)) {
                        if (DatosUsuarios.findUsuario(user, pass) != null) {
                            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Bienvenido", Snackbar.LENGTH_SHORT);

                        }
                    }
                }

            }
        });*/

    }
}