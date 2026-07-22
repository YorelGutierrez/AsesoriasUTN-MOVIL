package com.example.asesoriasutn;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText txtCorreo, txtPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Vincular los elementos del XML usando tus IDs exactos
        txtCorreo = findViewById(R.id.txtCorreo);
        txtPassword = findViewById(R.id.txtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // 2. Configurar la acción del botón
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String correo = txtCorreo.getText().toString().trim();
                String password = txtPassword.getText().toString().trim();

                if (correo.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Por favor ingresa tu correo y contraseña", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Lógica de inicio de sesión aislada en esta rama
                procesarLogin(correo, password);
            }
        });
    }

    private void procesarLogin(String correo, String password) {
        Log.d("LOGIN_UTN", "Intentando acceder con: " + correo);
        Toast.makeText(this, "¡Bienvenido, Maestro!", Toast.LENGTH_SHORT).show();

        // Muestra la pantalla de agendar al ingresar
        Intent intent = new Intent(MainActivity.this, AgendarAsesoria.class);
        startActivity(intent);
    }
}