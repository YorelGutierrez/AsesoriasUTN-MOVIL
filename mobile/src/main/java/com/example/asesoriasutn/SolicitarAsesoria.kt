package com.example.asesoriasutn

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SolicitarAsesoria : AppCompatActivity() {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicitar_asesoria)

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

        adapterModalidad.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spModalidad.adapter = adapterModalidad

        // Mock data for spinnerDocentes (Should be loaded from Supabase later)
        val docentes = arrayOf("Selecciona un docente...", "Ing. Juan Pérez", "Dra. María García", "Mtro. Luis Rodríguez")
        val adapterDocentes = ArrayAdapter(this, android.R.layout.simple_spinner_item, docentes)
        adapterDocentes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDocentes.adapter = adapterDocentes

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

        // Botón Enviar
        btnEnviarSolicitud.setOnClickListener {
            val docente = spinnerDocentes.selectedItem?.toString() ?: ""
            val aprender = etQueAprender.text.toString().trim()
            val objetivo = etObjetivo.text.toString().trim()
            val modalidad = spModalidad.selectedItem.toString()
            val hora = etHora.text.toString().trim()

            if (spinnerDocentes.selectedItemPosition == 0) {
                Toast.makeText(this, "Selecciona un docente", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (aprender.isEmpty() || objetivo.isEmpty() || hora.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Solicitud enviada correctamente", Toast.LENGTH_LONG).show()

            /*
             Aquí después agregaremos la lógica para guardar en Supabase
             */
        }
    }
}