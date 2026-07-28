package com.example.asesoriasutn

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var txtCorreo: EditText
    private lateinit var txtPassword: EditText
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. Vincular los elementos del XML usando IDs exactos
        txtCorreo = findViewById(R.id.txtCorreo)
        txtPassword = findViewById(R.id.txtPassword)
        btnLogin = findViewById(R.id.btnLogin)

        // 2. Configurar la acción del botón
        btnLogin.setOnClickListener {
            val correo = txtCorreo.text.toString().trim()
            val password = txtPassword.text.toString().trim()

            if (correo.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa tu correo y contraseña", Toast.LENGTH_SHORT).show()
            } else {
                // Lógica de validación según el tipo de correo
                procesarLogin(correo, password)
            }
        }
    }

    private fun procesarLogin(correo: String, password: String) {
        Log.d("LOGIN_UTN", "Intentando acceder con: $correo")
        val correoMinuscula = correo.lowercase()

        // Validar si el correo pertenece a la institución
        if (!correoMinuscula.endsWith("@utnay.edu.mx")) {
            Toast.makeText(this, "El correo debe pertenecer al dominio @utnay.edu.mx", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. CASO ADMINISTRADOR
        if (correoMinuscula == "admin@utnay.edu.mx") {
            Toast.makeText(this, "¡Bienvenido Administrador!", Toast.LENGTH_SHORT).show()

            // Redirige al nuevo menú con las dos opciones para el administrador
            val intent = Intent(this, MenuAdminActivity::class.java)
            startActivity(intent)
            return
        }

        // 2. CASO ALUMNO (Contiene guion y números, ej: tic-310010)
        val usuario = correoMinuscula.split("@")[0]
        if (usuario.contains("-") || usuario.contains(Regex("\\d"))) {
            Toast.makeText(this, "¡Bienvenido Alumno!", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, SolicitudDocente::class.java).apply {
                putExtra("USUARIO_EMAIL", correoMinuscula)
                putExtra("MATRICULA_O_NOMBRE", usuario)
            }
            startActivity(intent)

        } else {
            // 3. CASO DOCENTE (ej: juan@utnay.edu.mx)
            Toast.makeText(this, "¡Bienvenido Docente!", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, AgendarAsesoria::class.java).apply {
                putExtra("USUARIO_EMAIL", correoMinuscula)
                putExtra("USUARIO_NOMBRE", usuario)
            }
            startActivity(intent)
        }
    }
}
