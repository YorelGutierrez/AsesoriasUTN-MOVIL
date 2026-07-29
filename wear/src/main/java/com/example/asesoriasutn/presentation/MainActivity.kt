package com.example.asesoriasutn.presentation

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.example.asesoriasutn.presentation.models.Docente
import com.example.asesoriasutn.presentation.models.SolicitudAsesoriaWearRequest
import com.example.asesoriasutn.presentation.network.RetrofitClient
import com.example.asesoriasutn.presentation.theme.AsesoriasUTNTheme
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.delay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity(), SensorEventListener, DataClient.OnDataChangedListener {

    private lateinit var sensorManager: SensorManager
    private var rotationSensor: Sensor? = null
    private lateinit var sessionManager: SessionManager

    // Datos del alumno (Dinámicos tras sincronización)
    private var alumnoConectadoNombre by mutableStateOf("Vanessa")
    private var alumnoConectadoEmail by mutableStateOf("vanessa@utnay.edu.mx")

    private var estadoAsesoria by mutableStateOf("Cargando docentes...")
    private var isAsesoriaAceptada by mutableStateOf(false)
    private var listaDocentesGlobal by mutableStateOf<List<Docente>>(listOf())
    private var docenteSeleccionado by mutableStateOf<Docente?>(null)
    private var listaSolicitudes by mutableStateOf<List<SolicitudAsesoriaWearRequest>>(listOf())
    private var isLoading by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)
        
        // Redirigir si el usuario es maestro
        val currentRole = sessionManager.getUserRole()
        if (currentRole == "docente" || currentRole == "admin") {
            startActivity(Intent(this, MaestroActivity::class.java))
            finish()
            return
        }

        alumnoConectadoNombre = sessionManager.getUserName() ?: "Vanessa"
        alumnoConectadoEmail = sessionManager.getUserEmail() ?: "vanessa@utnay.edu.mx"

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        createNotificationChannel(this)
        cargarDocentes()

        setContent {
            WearApp(
                alumnoActual = alumnoConectadoNombre,
                estadoAsesoria = estadoAsesoria,
                docentes = listaDocentesGlobal,
                docenteSeleccionado = docenteSeleccionado,
                listaSolicitudes = listaSolicitudes,
                isLoading = isLoading,
                onSeleccionarDocente = { nuevoDocente ->
                    docenteSeleccionado = nuevoDocente
                },
                onSolicitarAsesoria = {
                    docenteSeleccionado?.let { enviarSolicitudAlDocente(it) }
                },
                onCargarSolicitudes = {
                    cargarSolicitudes()
                },
                onLimpiar = {
                    listaSolicitudes = listOf()
                    estadoAsesoria = "Selecciona un docente"
                }
            )
        }
    }

    private fun cargarDocentes() {
        RetrofitClient.apiService.getDocentes().enqueue(object : Callback<List<Docente>> {
            override fun onResponse(call: Call<List<Docente>>, response: Response<List<Docente>>) {
                if (response.isSuccessful && response.body() != null) {
                    listaDocentesGlobal = response.body()!!
                    estadoAsesoria = if (listaDocentesGlobal.isEmpty()) "Sin docentes disponibles" else "Selecciona un docente"
                } else {
                    estadoAsesoria = "Error al cargar docentes"
                }
            }
            override fun onFailure(call: Call<List<Docente>>, t: Throwable) {
                estadoAsesoria = "Fallo de red"
            }
        })
    }

    private fun cargarSolicitudes() {
        isLoading = true
        RetrofitClient.apiService.getSolicitudesPorAlumno("eq.$alumnoConectadoEmail").enqueue(object : Callback<List<SolicitudAsesoriaWearRequest>> {
            override fun onResponse(call: Call<List<SolicitudAsesoriaWearRequest>>, response: Response<List<SolicitudAsesoriaWearRequest>>) {
                isLoading = false
                if (response.isSuccessful && response.body() != null) {
                    listaSolicitudes = response.body()!!
                    if (listaSolicitudes.isEmpty()) estadoAsesoria = "Sin solicitudes enviadas"
                }
            }
            override fun onFailure(call: Call<List<SolicitudAsesoriaWearRequest>>, t: Throwable) {
                isLoading = false
                Toast.makeText(this@MainActivity, "Fallo al cargar", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun enviarSolicitudAlDocente(docente: Docente) {
        isLoading = true
        val sdfFecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val fechaActualStr = sdfFecha.format(Date())

        val nuevaSolicitud = SolicitudAsesoriaWearRequest(
            correoAlumno = alumnoConectadoEmail,
            correoDocente = docente.correo ?: "docente@utnay.edu.mx",
            docenteId = docente.id.toLong(),
            queAprender = "Solicitud rápida desde Smartwatch",
            conocimientoPrevio = true,
            necesitaMaterial = false,
            ejerciciosEspecificos = true,
            objetivo = "Consulta urgente vía Reloj",
            modalidad = "Virtual / En línea",
            fechaHora = fechaActualStr
        )

        RetrofitClient.apiService.registrarSolicitud(nuevaSolicitud).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                isLoading = false
                if (response.isSuccessful) {
                    estadoAsesoria = "¡Solicitud enviada!"
                    enviarNotificacionPersonalizada(this@MainActivity, "Éxito", "Petición enviada a ${docente.getNombreCompleto()}")
                } else {
                    estadoAsesoria = "Error: ${response.code()}"
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                isLoading = false
                estadoAsesoria = "Fallo de envío"
            }
        })
    }

    override fun onResume() {
        super.onResume()
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        Wearable.getDataClient(this).addListener(this)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        Wearable.getDataClient(this).removeListener(this)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/user_session") {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val nombre = dataMap.getString("nombre", "Vanessa")
                val email = dataMap.getString("email", "vanessa@utnay.edu.mx")
                val role = dataMap.getString("role", "alumno")

                if (role == "logout") {
                    sessionManager.clearSession()
                    alumnoConectadoNombre = "Vanessa"
                    alumnoConectadoEmail = "vanessa@utnay.edu.mx"
                    return
                }

                sessionManager.saveSession(nombre, email, role)
                
                if (role == "docente" || role == "admin") {
                    startActivity(Intent(this@MainActivity, MaestroActivity::class.java))
                    finish()
                } else {
                    alumnoConectadoNombre = nombre
                    alumnoConectadoEmail = email
                }
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!isAsesoriaAceptada && event != null) {
            val values = event.values
            if (values.isNotEmpty()) {
                if (Math.abs(values[0]) > 0.6f || Math.abs(values[1]) > 0.6f) {
                    isAsesoriaAceptada = true
                    // Podrías gatillar una confirmación automática aquí
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

fun enviarNotificacionPersonalizada(context: Context, titulo: String, mensaje: String) {
    val builder = NotificationCompat.Builder(context, "asesorias_channel")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(titulo)
        .setContentText(mensaje)
        .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)

    with(NotificationManagerCompat.from(context)) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            notify(1, builder.build())
        }
    }
}

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel("asesorias_channel", "Canal de Asesorias", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Notificaciones de asesorías"
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}

@Composable
fun WearApp(
    alumnoActual: String,
    estadoAsesoria: String,
    docentes: List<Docente>,
    docenteSeleccionado: Docente?,
    listaSolicitudes: List<SolicitudAsesoriaWearRequest>,
    isLoading: Boolean,
    onSeleccionarDocente: (Docente) -> Unit,
    onSolicitarAsesoria: () -> Unit,
    onCargarSolicitudes: () -> Unit,
    onLimpiar: () -> Unit
) {
    AsesoriasUTNTheme {
        AppScaffold {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()

            var currentTime by remember { mutableStateOf("") }

            LaunchedEffect(Unit) {
                while (true) {
                    val sdf = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
                    currentTime = sdf.format(Date())
                    delay(1000L)
                }
            }

            ScreenScaffold(
                scrollState = listState,
                edgeButton = {
                    EdgeButton(
                        onClick = { 
                            if (docenteSeleccionado != null && listaSolicitudes.isEmpty()) onSolicitarAsesoria() 
                            else onLimpiar()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0D9488),
                            contentColor = Color.White,
                        ),
                    ) {
                        Text(if (docenteSeleccionado != null && listaSolicitudes.isEmpty()) "Solicitar" else "Listo")
                    }
                },
            ) { contentPadding ->
                TransformingLazyColumn(contentPadding = contentPadding, state = listState) {
                    item {
                        ListHeader(
                            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text(
                                text = if (currentTime.isEmpty()) "Asesorías UTN" else currentTime,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF059669)
                            )
                        }
                    }

                    item {
                        ListHeader(
                            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text(
                                text = if (isLoading) "Procesando..." else "$alumnoActual\n$estadoAsesoria",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF0D9488)
                            )
                        }
                    }

                    if (listaSolicitudes.isEmpty()) {
                        items(docentes.size) { index ->
                            val docente = docentes[index]
                            val esSeleccionado = docente.id == docenteSeleccionado?.id

                            Button(
                                onClick = { onSeleccionarDocente(docente) },
                                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                                transformation = SurfaceTransformation(transformationSpec),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (esSeleccionado) Color(0xFF0D9488) else Color(0xFFF1F5F9),
                                    contentColor = if (esSeleccionado) Color.White else Color(0xFF0F172A)
                                )
                            ) {
                                Text(if (esSeleccionado) "✓ ${docente.nombres}" else docente.getNombreCompleto())
                            }
                        }
                        
                        item {
                            Button(
                                onClick = { onCargarSolicitudes() },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).transformedHeight(this, transformationSpec),
                                transformation = SurfaceTransformation(transformationSpec),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF1F5F9),
                                    contentColor = Color(0xFF0F172A)
                                )
                            ) {
                                Text("Ver mis peticiones")
                            }
                        }
                    } else {
                        items(listaSolicitudes.size) { index ->
                            val solicitud = listaSolicitudes[index]
                            Card(
                                onClick = { },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp).transformedHeight(this, transformationSpec),
                                transformation = SurfaceTransformation(transformationSpec),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = solicitud.correoDocente,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = solicitud.fechaHora,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun DefaultPreview() {
    // Preview con datos mock
}
