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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
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

    private List<Alumno> listaAlumnosGlobal = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agendar_asesoria);

        spinnerAlumnos = findViewById(R.id.spinnerAlumnos);
        etTema = findViewById(R.id.etTema);
        etObjetivo = findViewById(R.id.etObjetivo);
        etHora = findViewById(R.id.etHora);
        calendarView = findViewById(R.id.calendarView);
        btnAgendarSesion = findViewById(R.id.btnAgendarSesion);

        calendarView.setFirstDayOfWeek(Calendar.MONDAY);
        calendarView.setShowWeekNumber(false);
        calendarView.setMinDate(System.currentTimeMillis() - 1000);

        cargarAlumnosDesdeSupabase();

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        fechaSeleccionada = sdf.format(new java.util.Date(calendarView.getDate()));

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                fechaSeleccionada = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            }
        });

        etHora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                int horaActual = c.get(Calendar.HOUR_OF_DAY);
                int minutoActual = c.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(AgendarAsesoria.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String horaFormateada = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        etHora.setText(horaFormateada);
                    }
                }, horaActual, minutoActual, true);

                timePickerDialog.show();
            }
        });

        btnAgendarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listaAlumnosGlobal == null || listaAlumnosGlobal.isEmpty()) {
                    Toast.makeText(AgendarAsesoria.this, "La lista de alumnos aún está cargando", Toast.LENGTH_SHORT).show();
                    return;
                }

                int posicionSeleccionada = spinnerAlumnos.getSelectedItemPosition();
                if (posicionSeleccionada <= 0) {
                    Toast.makeText(AgendarAsesoria.this, "Por favor selecciona un alumno válido", Toast.LENGTH_SHORT).show();
                    return;
                }

                String tema = etTema.getText().toString().trim();
                String objetivo = etObjetivo.getText().toString().trim();
                String hora = etHora.getText().toString().trim();

                if (tema.isEmpty() || objetivo.isEmpty() || hora.isEmpty()) {
                    Toast.makeText(AgendarAsesoria.this, "Por favor completa todos los campos y la hora", Toast.LENGTH_SHORT).show();
                    return;
                }

                int indiceReal = posicionSeleccionada - 1;
                if (indiceReal < listaAlumnosGlobal.size()) {
                    int idAlumnoSeleccionado = listaAlumnosGlobal.get(indiceReal).getId();
                    guardarSesionEnSupabase(String.valueOf(idAlumnoSeleccionado), tema, objetivo, fechaSeleccionada, hora);
                } else {
                    Toast.makeText(AgendarAsesoria.this, "Error en la selección del alumno", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private Retrofit obtenerRetrofitConAuth() {
        final String apiKey = "sb_publishable_8hbEGvtOKw3SvnVz7apPlg_KWVdL5xe";

        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request request = original.newBuilder()
                        .header("apikey", apiKey)
                        .header("Authorization", "Bearer " + apiKey)
                        .method(original.method(), original.body())
                        .build();
                return chain.proceed(request);
            }
        }).build();

        return new Retrofit.Builder()
                .baseUrl("https://jxeftmhxwjiolbxiklyc.supabase.co/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private void cargarAlumnosDesdeSupabase() {
        SupabaseApiService apiService = obtenerRetrofitConAuth().create(SupabaseApiService.class);

        apiService.getAlumnos().enqueue(new Callback<List<Alumno>>() {
            @Override
            public void onResponse(Call<List<Alumno>> call, Response<List<Alumno>> response) {
                runOnUiThread(() -> {
                    if (response.isSuccessful() && response.body() != null) {
                        listaAlumnosGlobal = response.body();
                        List<String> nombresAlumnos = new ArrayList<>();

                        nombresAlumnos.add("Selecciona un alumno...");

                        for (Alumno a : listaAlumnosGlobal) {
                            nombresAlumnos.add(a.getNombreCompleto());
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
                        Toast.makeText(AgendarAsesoria.this, "Error al cargar alumnos: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<List<Alumno>> call, Throwable t) {
                runOnUiThread(() -> {
                    Log.e("SUPABASE_FALLO", "Fallo de red al buscar alumnos: " + t.getMessage());
                    Toast.makeText(AgendarAsesoria.this, "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void guardarSesionEnSupabase(String alumnoId, String tema, String objetivo, String fecha, String hora) {
        AsesoriaRequest nuevaAsesoria = new AsesoriaRequest(alumnoId, tema, objetivo, fecha + " " + hora);

        SupabaseApiService apiService = obtenerRetrofitConAuth().create(SupabaseApiService.class);

        apiService.registrarAsesoria(nuevaAsesoria).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(AgendarAsesoria.this, "¡Asesoría agendada con éxito!", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(AgendarAsesoria.this, "Error al guardar la asesoría: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                runOnUiThread(() -> {
                    Toast.makeText(AgendarAsesoria.this, "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}