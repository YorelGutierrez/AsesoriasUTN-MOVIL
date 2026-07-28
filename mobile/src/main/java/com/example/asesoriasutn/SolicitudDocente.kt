package com.example.asesoriasutn

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class SolicitudDocente : AppCompatActivity() {

    // Vistas de la barra superior
    private lateinit var btnRegresar: ImageView
    private lateinit var btnIconoCalendario: ImageView
    private lateinit var btnIconoNotificaciones: ImageView
    private lateinit var tvAvatarUsuario: TextView

    private lateinit var spinnerDocentes: Spinner
    private lateinit var spModalidad: Spinner

    private lateinit var etQueAprender: EditText
    private lateinit var etObjetivo: EditText
    private lateinit var etHora: EditText

    private lateinit var chkConocimiento: CheckBox
    private lateinit var chkMaterial: CheckBox
    private lateinit var chkEjercicios: CheckBox

    private lateinit var calendarView: CalendarView
    private lateinit var btnEnviarSolicitud: Button

    private var fechaSeleccionada = ""
    private var listaDocentesGlobal: List<Docente> = listOf()
    private var listaSolicitudesBD: List<SolicitudAsesoriaRequest> = listOf()
    private var listaSesionesConfirmadasBD: List<AsesoriaRequest> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.solicitar_asesoria)

        // Vincular controles de la barra superior
        btnRegresar = findViewById(R.id.btnRegresar)
        btnIconoCalendario = findViewById(R.id.btnIconoCalendario)
        btnIconoNotificaciones = findViewById(R.id.btnIconoNotificaciones)
        tvAvatarUsuario = findViewById(R.id.tvAvatarUsuario)

        // Vincular controles del formulario
        spinnerDocentes = findViewById(R.id.spinnerDocentes)
        spModalidad = findViewById(R.id.spModalidad)

        etQueAprender = findViewById(R.id.etQueAprender)
        etObjetivo = findViewById(R.id.etObjetivo)
        etHora = findViewById(R.id.etHora)

        chkConocimiento = findViewById(R.id.chkConocimiento)
        chkMaterial = findViewById(R.id.chkMaterial)
        chkEjercicios = findViewById(R.id.chkEjercicios)

        calendarView = findViewById(R.id.calendarView)
        btnEnviarSolicitud = findViewById(R.id.btnEnviarSolicitud)

        // Configurar barra superior, sesión dinámica y eventos
        configurarBarraSuperiorYDinamismo()

        // Spinner Modalidad
        val adapterModalidad = ArrayAdapter.createFromResource(
            this,
            R.array.modalidad,
            android.R.layout.simple_spinner_item
        )
        adapterModalidad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spModalidad.adapter = adapterModalidad

        // Cargar datos reales desde Supabase
        cargarDocentesDesdeSupabase()
        cargarSolicitudesDelAlumnoDesdeSupabase()
        cargarSesionesConfirmadasDesdeSupabase()

        // Configurar calendario
        calendarView.firstDayOfWeek = Calendar.MONDAY
        calendarView.setShowWeekNumber(false)
        calendarView.minDate = System.currentTimeMillis()

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        fechaSeleccionada = sdf.format(Date(calendarView.date))

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            fechaSeleccionada = String.format(
                Locale.getDefault(),
                "%04d-%02d-%02d",
                year,
                month + 1,
                dayOfMonth
            )
        }

        // Selector de hora
        etHora.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hora = calendar.get(Calendar.HOUR_OF_DAY)
            val minuto = calendar.get(Calendar.MINUTE)

            val dialog = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    etHora.setText(
                        String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            hourOfDay,
                            minute
                        )
                    )
                },
                hora,
                minuto,
                true
            )
            dialog.show()
        }

        // Botón Enviar Solicitud
        btnEnviarSolicitud.setOnClickListener {
            val aprender = etQueAprender.text.toString().trim()
            val objetivo = etObjetivo.text.toString().trim()
            val hora = etHora.text.toString().trim()
            val modalidad = spModalidad.selectedItem.toString()

            val tieneConocimiento = chkConocimiento.isChecked
            val necesitaMaterial = chkMaterial.isChecked
            val tieneEjercicios = chkEjercicios.isChecked

            if (spinnerDocentes.selectedItemPosition == 0) {
                Toast.makeText(this, "Selecciona un docente", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (aprender.isEmpty() || objetivo.isEmpty() || hora.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val indiceReal = spinnerDocentes.selectedItemPosition - 1
            if (indiceReal < listaDocentesGlobal.size) {
                val docenteSeleccionado = listaDocentesGlobal[indiceReal]
                val idDocenteSeleccionado = docenteSeleccionado.id.toLong()

                val correoDocenteDestino = docenteSeleccionado.correo ?: "docente@utnay.edu.mx"
                val correoAlumnoActual = intent.getStringExtra("USUARIO_EMAIL") ?: "desconocido@utnay.edu.mx"

                val nuevaSolicitud = SolicitudAsesoriaRequest(
                    correoAlumno = correoAlumnoActual,
                    correoDocente = correoDocenteDestino,
                    docenteId = idDocenteSeleccionado,
                    queAprender = aprender,
                    conocimientoPrevio = tieneConocimiento,
                    necesitaMaterial = necesitaMaterial,
                    ejerciciosEspecificos = tieneEjercicios,
                    objetivo = objetivo,
                    modalidad = modalidad,
                    fechaHora = "$fechaSeleccionada $hora:00"
                )

                guardarSolicitudEnSupabase(nuevaSolicitud)
            } else {
                Toast.makeText(this, "Error en la selección del docente", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun configurarBarraSuperiorYDinamismo() {
        val usuarioSesion = intent.getStringExtra("MATRICULA_O_NOMBRE") ?: "Alumno"
        
        // Avatar Dinámico: Calcula iniciales automáticamente
        var iniciales = "AL" // Default
        if (usuarioSesion.length >= 2) {
            val partes = usuarioSesion.trim().split(Regex("\\s+"))
            iniciales = if (partes.size >= 2) {
                (partes[0].substring(0, 1) + partes[1].substring(0, 1)).uppercase()
            } else {
                usuarioSesion.substring(0, 2.coerceAtMost(usuarioSesion.length)).uppercase()
            }
        }
        tvAvatarUsuario.text = iniciales

        tvAvatarUsuario.setOnClickListener {
            mostrarDialogoCerrarSesionPersonalizado()
        }

        // El botón regresar redirige según el ROL del usuario
        val isAdmin = intent.getBooleanExtra("IS_ADMIN", false)

        btnRegresar.setOnClickListener {
            if (isAdmin) {
                // Si es Admin, regresa al panel de administración
                val intent = Intent(this, MenuAdminActivity::class.java)
                intent.putExtra("USUARIO_NOMBRE", usuarioSesion)
                intent.putExtra("USUARIO_EMAIL", getIntent().getStringExtra("USUARIO_EMAIL"))
                intent.putExtra("IS_ADMIN", true)
                startActivity(intent)
                finish()
            } else {
                // Si no es Admin, es su pantalla principal -> Ofrecer cerrar sesión
                mostrarDialogoCerrarSesionPersonalizado()
            }
        }

        // Botón Calendario con diseño personalizado: Unifica solicitudes y sesiones confirmadas
        btnIconoCalendario.setOnClickListener {
            if (listaSolicitudesBD.isEmpty() && listaSesionesConfirmadasBD.isEmpty()) {
                Toast.makeText(this, "No tienes asesorías programadas ni solicitudes pendientes.", Toast.LENGTH_SHORT).show()
            } else {
                val sb = StringBuilder()
                
                // 1. Mostrar Sesiones Confirmadas por Docentes (Prioridad)
                if (listaSesionesConfirmadasBD.isNotEmpty()) {
                    sb.append("✅ ASESORÍAS CONFIRMADAS:\n\n")
                    for (sesion in listaSesionesConfirmadasBD) {
                        val fecha = formatearFechaLegible(sesion.fechaHora)
                        sb.append("• Docente: ").append(sesion.correoDocente)
                            .append("\n  Tema: ").append(sesion.tema)
                            .append("\n  Modalidad: ").append(sesion.modalidad)
                            .append("\n  Fecha: ").append(fecha)
                            .append("\n\n")
                    }
                    sb.append("----------------------------------\n\n")
                }

                // 2. Mostrar Solicitudes enviadas por el alumno
                if (listaSolicitudesBD.isNotEmpty()) {
                    sb.append("⏳ SOLICITUDES ENVIADAS:\n\n")
                    for (solicitud in listaSolicitudesBD) {
                        val fecha = formatearFechaLegible(solicitud.fechaHora)
                        sb.append("• Para: ").append(solicitud.correoDocente)
                            .append("\n  Qué aprender: ").append(solicitud.queAprender)
                            .append("\n  Modalidad: ").append(solicitud.modalidad)
                            .append("\n  Fecha tentadora: ").append(fecha)
                            .append("\n\n")
                    }
                }
                
                mostrarDialogoPersonalizado("📅 Mi Agenda de Asesorías", sb.toString().trim(), "Cerrar", null, false, null)
            }
        }

        // Botón Notificaciones: Prioriza avisar sobre nuevas sesiones confirmadas por el docente
        btnIconoNotificaciones.setOnClickListener {
            if (listaSesionesConfirmadasBD.isNotEmpty()) {
                val ultimaSesion = listaSesionesConfirmadasBD.last()
                val fecha = formatearFechaLegible(ultimaSesion.fechaHora)
                val mensaje = "¡Atención! El docente ${ultimaSesion.correoDocente} ha programado una asesoría para ti.\n\n" +
                        "• Tema: ${ultimaSesion.tema}\n" +
                        "• Fecha: $fecha\n" +
                        "• Modalidad: ${ultimaSesion.modalidad}"
                
                mostrarDialogoPersonalizado("🔔 Nueva Asesoría Agendada", mensaje, "Entendido", null, false, null)
            } else if (listaSolicitudesBD.isNotEmpty()) {
                val ultimaSol = listaSolicitudesBD.last()
                val mensaje = "Tu solicitud para ${ultimaSol.correoDocente} ha sido enviada correctamente y está a la espera de confirmación."
                
                mostrarDialogoPersonalizado("🔔 Estado de Solicitud", mensaje, "Entendido", null, false, null)
            } else {
                Toast.makeText(this, "No tienes notificaciones nuevas.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Función auxiliar para separar y formatear la fecha y hora limpiamente
    private fun formatearFechaLegible(fechaSupabase: String): String {
        return try {
            val fechaLimpia = if (fechaSupabase.length > 19) fechaSupabase.substring(0, 19) else fechaSupabase
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val fecha = parser.parse(fechaLimpia)
            val formatter = SimpleDateFormat("dd 'de' MMMM 'de' yyyy, hh:mm a", Locale("es", "ES"))
            formatter.format(fecha!!)
        } catch (e: Exception) {
            fechaSupabase
        }
    }

    // Método centralizado para mostrar el diálogo personalizado con botones redondos
    private fun mostrarDialogoPersonalizado(
        titulo: String,
        mensaje: String,
        textoBotonAceptar: String,
        textoBotonCancelar: String?,
        esDecision: Boolean,
        accionAceptar: (() -> Unit)?
    ) {
        val vistaDialogo = layoutInflater.inflate(R.layout.dialogo_personalizado, null)

        val tvTitulo = vistaDialogo.findViewById<TextView>(R.id.tvTituloDialogo)
        val tvMensaje = vistaDialogo.findViewById<TextView>(R.id.tvMensajeDialogo)
        val btnCancelar = vistaDialogo.findViewById<AppCompatButton>(R.id.btnCancelarDialogo)
        val btnAceptar = vistaDialogo.findViewById<AppCompatButton>(R.id.btnAceptarDialogo)

        tvTitulo.text = titulo
        tvMensaje.text = mensaje
        btnAceptar.text = textoBotonAceptar

        if (esDecision && textoBotonCancelar != null) {
            btnCancelar.visibility = View.VISIBLE
            btnCancelar.text = textoBotonCancelar
        } else {
            btnCancelar.visibility = View.GONE
        }

        val dialog = AlertDialog.Builder(this)
            .setView(vistaDialogo)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnAceptar.setOnClickListener {
            dialog.dismiss()
            accionAceptar?.invoke()
        }

        dialog.show()
    }

    private fun mostrarDialogoCerrarSesionPersonalizado() {
        mostrarDialogoPersonalizado(
            "Cerrar Sesión",
            "¿Estás segura de que deseas cerrar sesión?",
            "Sí, salir",
            "Cancelar",
            true
        ) {
            cerrarSesionYIrALogin()
        }
    }

    private fun cerrarSesionYIrALogin() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun obtenerRetrofitConAuth(): Retrofit {
        val apiKey = "sb_publishable_8hbEGvtOKw3SvnVz7apPlg_KWVdL5xe"
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("apikey", apiKey)
                .header("Authorization", "Bearer $apiKey")
                .method(original.method(), original.body())
                .build()
            chain.proceed(request)
        }.build()

        return Retrofit.Builder()
            .baseUrl("https://jxeftmhxwjiolbxiklyc.supabase.co/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun guardarSolicitudEnSupabase(solicitud: SolicitudAsesoriaRequest) {
        val apiService = obtenerRetrofitConAuth().create(SupabaseApiService::class.java)
        apiService.registrarSolicitud(solicitud).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@SolicitudDocente, "¡Solicitud enviada con éxito!", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Sin detalle"
                        Log.e("SUPABASE_DETALLE", "Error detalle: $errorBody")
                        Toast.makeText(this@SolicitudDocente, "Error al enviar: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                runOnUiThread {
                    Toast.makeText(this@SolicitudDocente, "Fallo de red: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun cargarDocentesDesdeSupabase() {
        val apiService = obtenerRetrofitConAuth().create(SupabaseApiService::class.java)
        apiService.getDocentes().enqueue(object : Callback<List<Docente>> {
            override fun onResponse(call: Call<List<Docente>>, response: Response<List<Docente>>) {
                runOnUiThread {
                    if (response.isSuccessful && response.body() != null) {
                        listaDocentesGlobal = response.body()!!
                        val nombresDocentes = mutableListOf("Selecciona un docente...")
                        for (docente in listaDocentesGlobal) {
                            nombresDocentes.add(docente.getNombreCompleto())
                        }
                        val adapterDocentes = ArrayAdapter(
                            this@SolicitudDocente,
                            android.R.layout.simple_spinner_item,
                            nombresDocentes
                        )
                        adapterDocentes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinnerDocentes.adapter = adapterDocentes
                    }
                }
            }
            override fun onFailure(call: Call<List<Docente>>, t: Throwable) {
                runOnUiThread {
                    Toast.makeText(this@SolicitudDocente, "Fallo de red: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun cargarSolicitudesDelAlumnoDesdeSupabase() {
        val correoActual = intent.getStringExtra("USUARIO_EMAIL") ?: ""
        val apiService = obtenerRetrofitConAuth().create(SupabaseApiService::class.java)

        // Consultamos la tabla de solicitudes filtrando específicamente por el correo del alumno actual
        apiService.getSolicitudesPorAlumno("eq.$correoActual").enqueue(object : Callback<List<SolicitudAsesoriaRequest>> {
            override fun onResponse(call: Call<List<SolicitudAsesoriaRequest>>, response: Response<List<SolicitudAsesoriaRequest>>) {
                runOnUiThread {
                    if (response.isSuccessful && response.body() != null) {
                        listaSolicitudesBD = response.body()!!
                    }
                }
            }
            override fun onFailure(call: Call<List<SolicitudAsesoriaRequest>>, t: Throwable) {
                Log.e("SUPABASE_SOLICITUDES", "Error al cargar solicitudes del alumno: ${t.message}")
            }
        })
    }

    private fun cargarSesionesConfirmadasDesdeSupabase() {
        val correoActual = intent.getStringExtra("USUARIO_EMAIL") ?: ""
        val apiService = obtenerRetrofitConAuth().create(SupabaseApiService::class.java)

        // Filtramos las sesiones donde el alumno participe por su correo
        val filtroOr = "(correo_docente.eq.$correoActual,correo_alumno.eq.$correoActual)"
        
        apiService.getSesionesPorUsuario(filtroOr).enqueue(object : Callback<List<AsesoriaRequest>> {
            override fun onResponse(call: Call<List<AsesoriaRequest>>, response: Response<List<AsesoriaRequest>>) {
                runOnUiThread {
                    if (response.isSuccessful && response.body() != null) {
                        listaSesionesConfirmadasBD = response.body()!!
                        Log.d("SUPABASE_SESIONES", "Sesiones confirmadas cargadas: ${listaSesionesConfirmadasBD.size}")
                    }
                }
            }
            override fun onFailure(call: Call<List<AsesoriaRequest>>, t: Throwable) {
                Log.e("SUPABASE_SESIONES", "Error al cargar sesiones confirmadas: ${t.message}")
            }
        })
    }
}
