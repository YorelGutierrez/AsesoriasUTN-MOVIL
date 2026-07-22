package com.example.asesoriasutn;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AgendarAsesoria extends AppCompatActivity {

    private Spinner spinnerAlumnos;
    private TextInputEditText etTema, etObjetivo, etHora;
    private CalendarView calendarView;
    private Button btnAgendarSesion;
    private String fechaSeleccionada = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agendar_asesoria);

        // 1. Vincular los elementos del XML con Java
        spinnerAlumnos = findViewById(R.id.spinnerAlumnos);
        etTema = findViewById(R.id.etTema);
        etObjetivo = findViewById(R.id.etObjetivo);
        etHora = findViewById(R.id.etHora);
        calendarView = findViewById(R.id.calendarView);
        btnAgendarSesion = findViewById(R.id.btnAgendarSesion);

        // Configuración inicial del calendario
        calendarView.setFirstDayOfWeek(Calendar.MONDAY);
        calendarView.setShowWeekNumber(false);
        calendarView.setMinDate(System.currentTimeMillis() - 1000);

        // 2. Cargar los alumnos desde Supabase para llenar el Spinner
        cargarAlumnosDesdeSupabase();

        // 3. Obtener la fecha inicial del calendario
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        fechaSeleccionada = sdf.format(new java.util.Date(calendarView.getDate()));

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                fechaSeleccionada = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            }
        });

        // 4. Configurar el selector de Hora al hacer clic en el campo de texto de hora
        etHora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                int horaActual = c.get(Calendar.HOUR_OF_DAY);
                int minutoActual = c.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(AgendarAsesoria.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Formato de hora am/pm o 24 hrs limpio
                        String horaFormateada = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        etHora.setText(horaFormateada);
                    }
                }, horaActual, minutoActual, true); // true para formato de 24 horas, false para AM/PM

                timePickerDialog.show();
            }
        });

        // 5. Acción al presionar el botón "Agendar Sesión"
        btnAgendarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spinnerAlumnos.getSelectedItem() == null) {
                    Toast.makeText(AgendarAsesoria.this, "Cargando alumnos...", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validación para evitar que envíen el texto por defecto del Paso 3
                if (spinnerAlumnos.getSelectedItemPosition() == 0) {
                    Toast.makeText(AgendarAsesoria.this, "Por favor selecciona un alumno", Toast.LENGTH_SHORT).show();
                    return;
                }

                String alumnoSeleccionado = spinnerAlumnos.getSelectedItem().toString();
                String tema = etTema.getText().toString().trim();
                String objetivo = etObjetivo.getText().toString().trim();
                String hora = etHora.getText().toString().trim();

                if (tema.isEmpty() || objetivo.isEmpty() || hora.isEmpty()) {
                    Toast.makeText(AgendarAsesoria.this, "Por favor completa todos los campos y la hora", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Llamar a la función para guardar la asesoría con fecha y hora
                guardarSesionEnSupabase(alumnoSeleccionado, tema, objetivo, fechaSeleccionada, hora);
            }
        });
    }

    private void cargarAlumnosDesdeSupabase() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://jxeftmhxwjiolbxiklyc.supabase.co/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SupabaseApiService apiService = retrofit.create(SupabaseApiService.class);

        apiService.getAlumnos().enqueue(new Callback<List<Alumno>>() {
            @Override
            public void onResponse(Call<List<Alumno>> call, Response<List<Alumno>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Alumno> listaAlumnos = response.body();
                    List<String> nombresAlumnos = new ArrayList<>();

                    // PASO 3: Agregar opción por defecto en el Spinner
                    nombresAlumnos.add("Selecciona un alumno...");

                    for (Alumno a : listaAlumnos) {
                        nombresAlumnos.add(a.getNombre());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            AgendarAsesoria.this,
                            android.R.layout.simple_spinner_item,
                            nombresAlumnos
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerAlumnos.setAdapter(adapter);

                } else {
                    Log.e("SUPABASE_ERROR", "Error al cargar alumnos: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Alumno>> call, Throwable t) {
                Log.e("SUPABASE_FALLO", "Fallo de red al buscar alumnos: " + t.getMessage());
            }
        });
    }

    private void guardarSesionEnSupabase(String alumno, String tema, String objetivo, String fecha, String hora) {
        // Asegúrate de incluir la hora en tu clase AsesoriaRequest si lo requiere tu base de datos
        AsesoriaRequest nuevaAsesoria = new AsesoriaRequest(alumno, tema, objetivo, fecha + " " + hora);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://jxeftmhxwjiolbxiklyc.supabase.co/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SupabaseApiService apiService = retrofit.create(SupabaseApiService.class);

        apiService.registrarAsesoria(nuevaAsesoria).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AgendarAsesoria.this, "¡Asesoría agendada con éxito!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(AgendarAsesoria.this, "Error al guardar: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AgendarAsesoria.this, "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}