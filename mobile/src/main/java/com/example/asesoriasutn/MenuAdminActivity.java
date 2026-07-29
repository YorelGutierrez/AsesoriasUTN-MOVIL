package com.example.asesoriasutn;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

public class MenuAdminActivity extends AppCompatActivity {

    private MaterialCardView cardProgramacion, cardSolicitud, cardLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_admin);

        cardProgramacion = findViewById(R.id.cardProgramacion);
        cardSolicitud = findViewById(R.id.cardSolicitud);
        cardLogout = findViewById(R.id.cardLogout);

        // Recuperar datos de sesión del admin
        String nombre = getIntent().getStringExtra("USUARIO_NOMBRE");
        String email = getIntent().getStringExtra("USUARIO_EMAIL");
        boolean isAdmin = getIntent().getBooleanExtra("IS_ADMIN", false);

        // Opción 1: Programación (Vista Docente)
        cardProgramacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuAdminActivity.this, AgendarAsesoria.class);
                intent.putExtra("USUARIO_NOMBRE", nombre);
                intent.putExtra("USUARIO_EMAIL", email);
                intent.putExtra("IS_ADMIN", isAdmin);
                startActivity(intent);
            }
        });

        // Opción 2: Solicitudes (Vista Alumno/Solicitud)
        cardSolicitud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuAdminActivity.this, SolicitudDocente.class);
                intent.putExtra("MATRICULA_O_NOMBRE", nombre);
                intent.putExtra("USUARIO_EMAIL", email);
                intent.putExtra("IS_ADMIN", isAdmin);
                startActivity(intent);
            }
        });

        // Opción 3: Cerrar Sesión
        cardLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoCerrarSesionPersonalizado();
            }
        });
    }

    private void mostrarDialogoPersonalizado(String titulo, String mensaje, String textoBotonAceptar, String textoBotonCancelar, boolean esDecision, Runnable accionAceptar) {
        View vistaDialogo = getLayoutInflater().inflate(R.layout.dialogo_personalizado, null);

        TextView tvTitulo = vistaDialogo.findViewById(R.id.tvTituloDialogo);
        TextView tvMensaje = vistaDialogo.findViewById(R.id.tvMensajeDialogo);
        Button btnCancelar = vistaDialogo.findViewById(R.id.btnCancelarDialogo);
        Button btnAceptar = vistaDialogo.findViewById(R.id.btnAceptarDialogo);

        tvTitulo.setText(titulo);
        tvMensaje.setText(mensaje);
        btnAceptar.setText(textoBotonAceptar);

        if (esDecision && textoBotonCancelar != null) {
            btnCancelar.setVisibility(View.VISIBLE);
            btnCancelar.setText(textoBotonCancelar);
        } else {
            btnCancelar.setVisibility(View.GONE);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(vistaDialogo)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnAceptar.setOnClickListener(v -> {
            dialog.dismiss();
            if (accionAceptar != null) {
                accionAceptar.run();
            }
        });

        dialog.show();
    }

    private void mostrarDialogoCerrarSesionPersonalizado() {
        mostrarDialogoPersonalizado(
                "Cerrar Sesión",
                "¿Estás seguro de que deseas cerrar sesión?",
                "Sí, salir",
                "Cancelar",
                true,
                this::cerrarSesionYIrALogin
        );
    }

    private void cerrarSesionYIrALogin() {
        MainActivity.clearWearSession(this);
        Intent intent = new Intent(MenuAdminActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
