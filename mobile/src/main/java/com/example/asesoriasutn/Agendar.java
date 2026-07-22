package com.example.asesoriasutn;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Agendar extends AppCompatActivity {

    private EditText etBuscarAlumno, etTema, etObjetivo;
    private CalendarView calendarView;
    private Button btnAgendarSesion;
    private String fechaSeleccionada = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agendar);

        // 1. Vincular los elementos del XML con Java
        etBuscarAlumno = findViewById(R.id.etBuscarAlumno);
        etTema = findViewById(R.id.etTema);
        etObjetivo = findViewById(R.id.etObjetivo);
        calendarView = findViewById(R.id.calendarView);
        calendarView.setFirstDayOfWeek(Calendar.MONDAY);
        calendarView.setShowWeekNumber(false);
        calendarView.setMinDate(System.currentTimeMillis() - 1000); // Evitar fechas pasadas
        calendarView.setSelectedDateVerticalBar(R.color.teal_700);

        btnAgendarSesion = findViewById(R.id.btnAgendarSesion);

        // 2. Obtener la fecha seleccionada del calendario
        // Inicializamos con la fecha actual formateada
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        fechaSeleccionada = sdf.format(new java.util.Date(calendarView.getDate()));

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                // El mes empieza en 0, por eso se suma 1
                fechaSeleccionada = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            }
        });

        // 3. Acción al presionar el botón "Agendar Sesión"
        btnAgendarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String alumno = etBuscarAlumno.getText().toString().trim();
                String tema = etTema.getText().toString().trim();
                String objetivo = etObjetivo.getText().toString().trim();

                if (alumno.isEmpty() || tema.isEmpty() || objetivo.isEmpty()) {
                    Toast.makeText(Agendar.this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Llamamos a la función que envía los datos a Supabase
                guardarSesionEnSupabase(alumno, tema, objetivo, fechaSeleccionada);
            }
        });
    }

    private void guardarSesionEnSupabase(String alumno, String tema, String objetivo, String fecha) {
        // Creamos el objeto con los datos (asegúrate de que tu AsesoriaRequest tenga estos campos)
        AsesoriaRequest nuevaAsesoria = new AsesoriaRequest(alumno, tema, objetivo, fecha);

        // Configuración de Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://jxeftmhxwjiolbxiklyc.supabase.co/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SupabaseApiService apiService = retrofit.create(SupabaseApiService.class);

        // Ejecutar la petición POST
        apiService.registrarAsesoria(nuevaAsesoria).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(Agendar.this, "¡Asesoría agendada con éxito!", Toast.LENGTH_LONG).show();
                    Log.d("SUPABASE_AGENDAR", "Guardado correctamente en la base de datos");
                    finish(); // Cierra la pantalla y regresa
                } else {
                    Toast.makeText(Agendar.this, "Error al guardar: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e("SUPABASE_AGENDAR", "Error en respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(Agendar.this, "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("SUPABASE_AGENDAR", "Fallo de red", t);
            }
        });
    }
}