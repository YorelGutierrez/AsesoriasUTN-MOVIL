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

                // Lógica de validación según el tipo de correo
                procesarLogin(correo, password);
            }
        });
    }

    private void procesarLogin(String correo, String password) {
        Log.d("LOGIN_UTN", "Intentando acceder con: " + correo);
        String correoMinuscula = correo.toLowerCase();

        // Validar si el correo pertenece a la institución
        if (!correoMinuscula.endsWith("@utnay.edu.mx")) {
            Toast.makeText(this, "El correo debe pertenecer al dominio @utnay.edu.mx", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. CASO ADMINISTRADOR
        if (correoMinuscula.equals("admin@utnay.edu.mx")) {
            Toast.makeText(this, "¡Bienvenido Administrador!", Toast.LENGTH_SHORT).show();
            // Acceso total: lo mandamos a la programación de asesorías (vista docente)
            Intent intent = new Intent(MainActivity.this, AgendarAsesoria.class);
            startActivity(intent);
            return;
        }

        // 2. CASO ALUMNO (Contiene guion y números, ej: tic-310010)
        String usuario = correoMinuscula.split("@")[0];
        if (usuario.contains("-") || usuario.matches(".*\\d.*")) {
            Toast.makeText(this, "¡Bienvenido Alumno!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(MainActivity.this, SolicitudDocente.class);
            startActivity(intent);

        } else {
            // 3. CASO DOCENTE (ej: juan@utnay.edu.mx)
            Toast.makeText(this, "¡Bienvenido Docente!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(MainActivity.this, AgendarAsesoria.class);
            startActivity(intent);
        }
    }
}