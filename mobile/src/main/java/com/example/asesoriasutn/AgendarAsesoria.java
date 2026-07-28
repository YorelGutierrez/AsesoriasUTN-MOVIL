package com.example.asesoriasutn;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

    // Vistas de la barra superior
    private ImageView btnRegresar, btnIconoCalendario, btnIconoNotificaciones;
    private TextView tvAvatarUsuario;

    // Vistas del formulario de agendar asesoría
    private Spinner spinnerGrupos, spinnerAlumnos, spinnerModalidad;
    private EditText etTema, etObjetivo, etHora;
    private CalendarView calendarView;
    private Button btnAgendarSesion;
    private String fechaSeleccionada = "";

    private List<Alumno> listaAlumnosGlobal = new ArrayList<>();
    private List<Alumno> listaAlumnosFiltrada = new ArrayList<>();

    // Lista actualizada para almacenar las solicitudes hechas por los alumnos hacia este docente
    private List<SolicitudAsesoriaRequest> listaSolicitudesBD = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agendar_asesoria);

        // Vincular elementos de la barra superior
        btnRegresar = findViewById(R.id.btnRegresar);
        btnIconoCalendario = findViewById(R.id.btnIconoCalendario);
        btnIconoNotificaciones = findViewById(R.id.btnIconoNotificaciones);
        tvAvatarUsuario = findViewById(R.id.tvAvatarUsuario);

        // Vincular controles del formulario
        spinnerGrupos = findViewById(R.id.spinnerGrupos);
        spinnerAlumnos = findViewById(R.id.spinnerAlumnos);
        spinnerModalidad = findViewById(R.id.spinnerModalidad);
        etTema = findViewById(R.id.etTema);
        etObjetivo = findViewById(R.id.etObjetivo);
        etHora = findViewById(R.id.etHora);
        calendarView = findViewById(R.id.calendarView);
        btnAgendarSesion = findViewById(R.id.btnAgendarSesion);

        configurarBarraSuperiorYDinamismo();

        // Configuración del calendario
        calendarView.setFirstDayOfWeek(Calendar.MONDAY);
        calendarView.setShowWeekNumber(false);
        calendarView.setMinDate(System.currentTimeMillis() - 1000);

        cargarAlumnosDesdeSupabase();
        cargarSolicitudesDocenteDesdeSupabase();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        fechaSeleccionada = sdf.format(new Date(calendarView.getDate()));

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) ->
                fechaSeleccionada = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
        );

        etHora.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int horaActual = c.get(Calendar.HOUR_OF_DAY);
            int minutoActual = c.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(AgendarAsesoria.this, (view, hourOfDay, minute) -> {
                String horaFormateada = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                etHora.setText(horaFormateada);
            }, horaActual, minutoActual, true);

            timePickerDialog.show();
        });

        spinnerGrupos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    String grupoSeleccionado = parent.getItemAtPosition(position).toString();
                    filtrarAlumnosPorGrupo(grupoSeleccionado);
                } else {
                    listaAlumnosFiltrada.clear();
                    List<String> limpiar = new ArrayList<>();
                    limpiar.add("Selecciona un alumno...");
                    actualizarSpinnerAlumnos(limpiar);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnAgendarSesion.setOnClickListener(v -> {
            if (listaAlumnosFiltrada == null || listaAlumnosFiltrada.isEmpty()) {
                Toast.makeText(AgendarAsesoria.this, "Selecciona un grupo y un alumno válido", Toast.LENGTH_SHORT).show();
                return;
            }

            int posicionSeleccionada = spinnerAlumnos.getSelectedItemPosition();
            if (posicionSeleccionada <= 0) {
                Toast.makeText(AgendarAsesoria.this, "Por favor selecciona un alumno", Toast.LENGTH_SHORT).show();
                return;
            }

            String tema = etTema.getText().toString().trim();
            String objetivo = etObjetivo.getText().toString().trim();
            String hora = etHora.getText().toString().trim();
            String modalidad = spinnerModalidad.getSelectedItem().toString();

            if (tema.isEmpty() || objetivo.isEmpty() || hora.isEmpty()) {
                Toast.makeText(AgendarAsesoria.this, "Por favor completa todos los campos y la hora", Toast.LENGTH_SHORT).show();
                return;
            }

            int indiceReal = posicionSeleccionada - 1;
            if (indiceReal < listaAlumnosFiltrada.size()) {
                Alumno alumnoSeleccionado = listaAlumnosFiltrada.get(indiceReal);
                int idAlumnoSeleccionado = alumnoSeleccionado.getId();

                // Obtener datos del docente actual de forma segura desde el Intent
                long docenteIdActual = obtenerIdDocenteActual();
                String correoDocenteActual = getIntent().getStringExtra("USUARIO_EMAIL");
                if (correoDocenteActual == null) correoDocenteActual = "docente@utnay.edu.mx";

                // Construir el correo institucional fijo basado en la matrícula
                String matricula = alumnoSeleccionado.getMatricula();
                String correoAlumno;
                if (matricula != null && !matricula.isEmpty()) {
                    correoAlumno = matricula.toLowerCase().trim() + "@utnay.edu.mx";
                } else {
                    correoAlumno = alumnoSeleccionado.getCorreo();
                    if (correoAlumno == null || correoAlumno.isEmpty() || correoAlumno.contains("alumno@")) {
                        correoAlumno = "alumno@utnay.edu.mx";
                    }
                }

                guardarSesionEnSupabase(idAlumnoSeleccionado, tema, objetivo, fechaSeleccionada, hora, modalidad, docenteIdActual, correoAlumno, correoDocenteActual);
            } else {
                Toast.makeText(AgendarAsesoria.this, "Error en la selección del alumno", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private long obtenerIdDocenteActual() {
        String docenteStr = getIntent().getStringExtra("DOCENTE_ID");
        if (docenteStr != null && !docenteStr.isEmpty()) {
            try {
                return Long.parseLong(docenteStr);
            } catch (NumberFormatException ignored) {}
        }
        return 92L;
    }

    private void configurarBarraSuperiorYDinamismo() {
        String usuarioSesion = getIntent().getStringExtra("USUARIO_NOMBRE");
        if (usuarioSesion == null || usuarioSesion.isEmpty()) {
            usuarioSesion = "Docente";
        }

        // Avatar Dinámico: Calcula iniciales automáticamente
        String iniciales = "VM"; // Default
        if (usuarioSesion.length() >= 2) {
            String[] partes = usuarioSesion.trim().split("\\s+");
            if (partes.length >= 2) {
                iniciales = (partes[0].substring(0, 1) + partes[1].substring(0, 1)).toUpperCase();
            } else {
                iniciales = usuarioSesion.substring(0, Math.min(usuarioSesion.length(), 2)).toUpperCase();
            }
        }
        tvAvatarUsuario.setText(iniciales);

        // El avatar mantiene exclusivamente la ventana emergente de Cerrar Sesión
        tvAvatarUsuario.setOnClickListener(v -> mostrarDialogoCerrarSesionPersonalizado());

        // El botón regresar redirige según el ROL del usuario
        boolean isAdmin = getIntent().getBooleanExtra("IS_ADMIN", false);
        String finalUsuarioSesion = usuarioSesion;

        btnRegresar.setOnClickListener(v -> {
            if (isAdmin) {
                // Si es Admin, regresa al panel de administración
                Intent intent = new Intent(AgendarAsesoria.this, MenuAdminActivity.class);
                intent.putExtra("USUARIO_NOMBRE", finalUsuarioSesion);
                intent.putExtra("USUARIO_EMAIL", getIntent().getStringExtra("USUARIO_EMAIL"));
                intent.putExtra("IS_ADMIN", true);
                startActivity(intent);
                finish();
            } else {
                // Si no es Admin, es su pantalla principal -> Ofrecer cerrar sesión
                mostrarDialogoCerrarSesionPersonalizado();
            }
        });

        // Botón Calendario: Muestra las solicitudes de asesoría hechas por los alumnos hacia este docente
        btnIconoCalendario.setOnClickListener(v -> {
            if (listaSolicitudesBD.isEmpty()) {
                Toast.makeText(this, "No hay solicitudes de asesoría pendientes.", Toast.LENGTH_SHORT).show();
            } else {
                StringBuilder sb = new StringBuilder();
                String nombreDocenteActual = getIntent().getStringExtra("USUARIO_NOMBRE");
                if (nombreDocenteActual == null || nombreDocenteActual.isEmpty()) {
                    nombreDocenteActual = "Docente";
                }

                for (SolicitudAsesoriaRequest solicitud : listaSolicitudesBD) {
                    String fechaFormateada = formatearFechaLegible(solicitud.getFechaHora());

                    sb.append("• Alumno: ").append(solicitud.getCorreoAlumno())
                            .append("\n• Docente: ").append(nombreDocenteActual)
                            .append("\n• Qué aprender: ").append(solicitud.getQueAprender())
                            .append("\n• Objetivo: ").append(solicitud.getObjetivo())
                            .append("\n• Modalidad: ").append(solicitud.getModalidad())
                            .append("\n• Fecha y Hora: ").append(fechaFormateada)
                            .append("\n\n----------------------------------\n\n");
                }

                mostrarDialogoPersonalizado("📅 Solicitudes de Alumnos", sb.toString().trim(), "Cerrar", null, false, null);
            }
        });

        // Botón Notificaciones: Muestra la solicitud más reciente de los alumnos
        btnIconoNotificaciones.setOnClickListener(v -> {
            if (listaSolicitudesBD.isEmpty()) {
                Toast.makeText(this, "No tienes notificaciones nuevas.", Toast.LENGTH_SHORT).show();
            } else {
                SolicitudAsesoriaRequest ultima = listaSolicitudesBD.get(listaSolicitudesBD.size() - 1);
                String fechaFormateada = formatearFechaLegible(ultima.getFechaHora());

                String mensajeNotificacion = "• Alumno Solicitante: " + ultima.getCorreoAlumno() +
                        "\n• Qué aprender: " + ultima.getQueAprender() +
                        "\n• Objetivo: " + ultima.getObjetivo() +
                        "\n• Modalidad: " + ultima.getModalidad() +
                        "\n• Programada para:\n  " + fechaFormateada;

                mostrarDialogoPersonalizado("🔔 Solicitud Reciente", mensajeNotificacion, "Entendido", null, false, null);
            }
        });
    }

    private String formatearFechaLegible(String fechaSupabase) {
        try {
            String fechaLimpia = fechaSupabase.length() > 19 ? fechaSupabase.substring(0, 19) : fechaSupabase;
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date fecha = parser.parse(fechaLimpia);

            SimpleDateFormat formatter = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy, hh:mm a", new Locale("es", "ES"));
            return formatter.format(fecha);
        } catch (Exception e) {
            return fechaSupabase;
        }
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
                "¿Estás segura de que deseas cerrar sesión?",
                "Sí, salir",
                "Cancelar",
                true,
                this::cerrarSesionYIrALogin
        );
    }

    private void cerrarSesionYIrALogin() {
        Intent intent = new Intent(AgendarAsesoria.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
                        List<String> listaGrupos = new ArrayList<>();
                        listaGrupos.add("Selecciona un grupo...");

                        for (Alumno a : listaAlumnosGlobal) {
                            if (a.getGrupo() != null) {
                                String nombreGrupo = a.getGrupo().getNombre();
                                if (nombreGrupo != null && !listaGrupos.contains(nombreGrupo)) {
                                    listaGrupos.add(nombreGrupo);
                                }
                            }
                        }

                        ArrayAdapter<String> adapterGrupos = new ArrayAdapter<>(
                                AgendarAsesoria.this,
                                android.R.layout.simple_spinner_item,
                                listaGrupos
                        );
                        adapterGrupos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerGrupos.setAdapter(adapterGrupos);
                    } else {
                        Log.e("SUPABASE_ERROR", "Error al cargar alumnos: " + response.code());
                        Toast.makeText(AgendarAsesoria.this, "Error de datos: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<List<Alumno>> call, Throwable t) {
                runOnUiThread(() -> Toast.makeText(AgendarAsesoria.this, "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void cargarSolicitudesDocenteDesdeSupabase() {
        String correoDocenteActual = getIntent().getStringExtra("USUARIO_EMAIL");
        if (correoDocenteActual == null || correoDocenteActual.isEmpty()) {
            correoDocenteActual = "docente@utnay.edu.mx";
        }

        SupabaseApiService apiService = obtenerRetrofitConAuth().create(SupabaseApiService.class);

        apiService.getSolicitudesPorDocente("eq." + correoDocenteActual).enqueue(new Callback<List<SolicitudAsesoriaRequest>>() {
            @Override
            public void onResponse(Call<List<SolicitudAsesoriaRequest>> call, Response<List<SolicitudAsesoriaRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaSolicitudesBD = response.body();
                }
            }

            @Override
            public void onFailure(Call<List<SolicitudAsesoriaRequest>> call, Throwable t) {
                Log.e("SUPABASE_SOLICITUDES", "Error al cargar solicitudes del docente: " + t.getMessage());
            }
        });
    }

    private void filtrarAlumnosPorGrupo(String grupoSeleccionado) {
        listaAlumnosFiltrada.clear();
        List<String> nombresAlumnos = new ArrayList<>();
        nombresAlumnos.add("Selecciona un alumno...");

        for (Alumno a : listaAlumnosGlobal) {
            if (a.getGrupo() != null && grupoSeleccionado.equals(a.getGrupo().getNombre())) {
                listaAlumnosFiltrada.add(a);
                nombresAlumnos.add(a.getNombreCompleto());
            }
        }

        actualizarSpinnerAlumnos(nombresAlumnos);
    }

    private void actualizarSpinnerAlumnos(List<String> nombres) {
        ArrayAdapter<String> adapterAlumnos = new ArrayAdapter<>(
                AgendarAsesoria.this,
                android.R.layout.simple_spinner_item,
                nombres
        );
        adapterAlumnos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAlumnos.setAdapter(adapterAlumnos);
    }

    private void guardarSesionEnSupabase(int alumnoId, String tema, String objetivo, String fecha, String hora, String modalidad, long docenteId, String correoAlumno, String correoDocente) {
        AsesoriaRequest nuevaAsesoria = new AsesoriaRequest(alumnoId, tema, objetivo, fecha + " " + hora, modalidad, docenteId, correoAlumno, correoDocente);

        SupabaseApiService apiService = obtenerRetrofitConAuth().create(SupabaseApiService.class);

        apiService.registrarAsesoria(nuevaAsesoria).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(AgendarAsesoria.this, "¡Asesoría agendada con éxito!", Toast.LENGTH_LONG).show();
                        lanzarNotificacionAsesoria(tema, fecha + " a las " + hora);
                        finish();
                    } else {
                        Log.e("SUPABASE_ERROR", "Error al guardar: " + response.code());
                        Toast.makeText(AgendarAsesoria.this, "Error al guardar: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                runOnUiThread(() -> Toast.makeText(AgendarAsesoria.this, "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void lanzarNotificacionAsesoria(String tema, String fechaHora) {
        String CHANNEL_ID = "canal_asesorias";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Asesorías Canal";
            String description = "Notificaciones de nuevas asesorías agendadas";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_agenda)
                .setContentTitle("¡Nueva Asesoría Agendada!")
                .setContentText("Tema: " + tema + " | Programada para: " + fechaHora)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}
