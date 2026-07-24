package com.example.asesoriasutn;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

public class MenuAdminActivity extends AppCompatActivity {

    private MaterialCardView cardProgramacion, cardSolicitud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_admin);

        cardProgramacion = findViewById(R.id.cardProgramacion);
        cardSolicitud = findViewById(R.id.cardSolicitud);

        // Opción 1: Programación (Vista Docente)
        cardProgramacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuAdminActivity.this, AgendarAsesoria.class);
                startActivity(intent);
            }
        });

        // Opción 2: Solicitudes (Vista Alumno/Solicitud)
        cardSolicitud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuAdminActivity.this, SolicitudDocente.class);
                startActivity(intent);
            }
        });
    }
}