package com.example.asesoriasutn;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

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
                Log.d("LOGIN_UTN", "Botón Iniciar Sesión presionado");
                String correo = txtCorreo.getText().toString().trim();
                String password = txtPassword.getText().toString().trim();

                if (correo.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Por favor ingresa tu correo y contraseña", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    // Lógica de validación según el tipo de correo
                    procesarLogin(correo, password);
                } catch (Exception e) {
                    Log.e("LOGIN_UTN", "Error inesperado en procesarLogin", e);
                    Toast.makeText(MainActivity.this, "Error al procesar el inicio de sesión", Toast.LENGTH_SHORT).show();
                }
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

        String usuario = correoMinuscula.split("@")[0];

        // 1. CASO ADMINISTRADOR
        if (usuario.equals("admin")) {
            if (password.equals("12345678")) {
                Toast.makeText(this, "¡Bienvenido Administrador!", Toast.LENGTH_SHORT).show();

                sendUserDataToWear("Administrador", correoMinuscula);

                Intent intent = new Intent(MainActivity.this, MenuAdminActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "usuario o contraseña incorrecto", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // 2. CASO ALUMNO (Contiene guion y números, ej: tic-310010)
        if (usuario.contains("-") || usuario.matches(".*\\d.*")) {
            if (password.equals(usuario)) {
                Toast.makeText(this, "¡Bienvenido Alumno!", Toast.LENGTH_SHORT).show();

                sendUserDataToWear(usuario, correoMinuscula);

                Intent intent = new Intent(MainActivity.this, SolicitudDocente.class);
                intent.putExtra("USUARIO_EMAIL", correoMinuscula);
                intent.putExtra("MATRICULA_O_NOMBRE", usuario);
                startActivity(intent);
            } else {
                Toast.makeText(this, "usuario o contraseña incorrecto", Toast.LENGTH_SHORT).show();
            }
        } else {
            // 3. CASO DOCENTE (ej: juan@utnay.edu.mx)
            if (password.equals("12345678")) {
                Toast.makeText(this, "¡Bienvenido Docente!", Toast.LENGTH_SHORT).show();

                sendUserDataToWear(usuario, correoMinuscula);

                Intent intent = new Intent(MainActivity.this, AgendarAsesoria.class);
                intent.putExtra("USUARIO_EMAIL", correoMinuscula);
                intent.putExtra("USUARIO_NOMBRE", usuario);
                startActivity(intent);
            } else {
                Toast.makeText(this, "usuario o contraseña incorrecto", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendUserDataToWear(String nombre, String email) {
        try {
            Log.d("WEAR_SYNC", "Iniciando sincronización para: " + nombre);
            PutDataMapRequest dataMap = PutDataMapRequest.create("/user_session");
            dataMap.getDataMap().putString("nombre", nombre);
            dataMap.getDataMap().putString("email", email);
            dataMap.getDataMap().putLong("timestamp", System.currentTimeMillis());

            PutDataRequest request = dataMap.asPutDataRequest();
            request.setUrgent();

            Wearable.getDataClient(this).putDataItem(request)
                    .addOnSuccessListener(dataItem -> Log.d("WEAR_SYNC", "Datos enviados al reloj: " + nombre))
                    .addOnFailureListener(e -> Log.e("WEAR_SYNC", "Fallo al sincronizar con el reloj", e));
        } catch (Exception e) {
            Log.e("WEAR_SYNC", "Error crítico al intentar conectar con Wearable API", e);
            // No bloqueamos el flujo principal si el reloj falla
        }
    }
}

