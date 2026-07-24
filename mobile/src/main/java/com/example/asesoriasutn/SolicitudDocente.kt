package com.example.asesoriasutn

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SolicitudDocente : AppCompatActivity() {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.solicitar_asesoria)

        // Vincular controles
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

        // Spinner Modalidad
        val adapterModalidad = ArrayAdapter.createFromResource(
            this,
            R.array.modalidad,
            android.R.layout.simple_spinner_item
        )
        adapterModalidad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spModalidad.adapter = adapterModalidad

        // Cargar docentes reales desde Supabase
        cargarDocentesDesdeSupabase()

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

            // Obtener el ID del docente seleccionado de la lista filtrada
            val indiceReal = spinnerDocentes.selectedItemPosition - 1
            if (indiceReal < listaDocentesGlobal.size) {
                val idDocenteSeleccionado = listaDocentesGlobal[indiceReal].id

                // Mapeo corregido acorde a la tabla solicitudes_asesoria en Supabase
                val nuevaSolicitud = SolicitudAsesoriaRequest(
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

    private fun guardarSolicitudEnSupabase(solicitud: SolicitudAsesoriaRequest) {
        val apiService = obtenerRetrofitConAuth().create(SupabaseApiService::class.java)

        apiService.registrarSolicitud(solicitud).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@SolicitudDocente, "¡Solicitud enviada con éxito!", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        val errorBody = response.errorBody()?.string() ?: ""
                        Log.e("SUPABASE_ERROR", "Error al guardar solicitud: ${response.code()} - $errorBody")
                        Toast.makeText(this@SolicitudDocente, "Error al enviar: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                runOnUiThread {
                    Log.e("SUPABASE_FALLO", "Fallo de red: ${t.message}")
                    Toast.makeText(this@SolicitudDocente, "Fallo de red: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun obtenerRetrofitConAuth(): Retrofit {
        val apiKey = "sb_publishable_8hbEGvtOKw3SvnVz7apPlg_KWVdL5xe"

        val client = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
            val original: Request = chain.request()
            val request: Request = original.newBuilder()
                .header("apikey", apiKey)
                .header("Authorization", "Bearer $apiKey")
                .method(original.method(), original.body())
                .build()
            chain.proceed(request)
        }).build()

        return Retrofit.Builder()
            .baseUrl("https://jxeftmhxwjiolbxiklyc.supabase.co/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
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

                    } else {
                        Toast.makeText(this@SolicitudDocente, "Error al cargar docentes: " + response.code(), Toast.LENGTH_SHORT).show()
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
}