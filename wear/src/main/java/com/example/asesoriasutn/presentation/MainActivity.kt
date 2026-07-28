package com.example.asesoriasutn.presentation

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
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
import com.example.asesoriasutn.presentation.theme.AsesoriasUTNTheme
import com.example.asesoriasutn.wear.R
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var rotationSensor: Sensor? = null

    // Alumno vinculado a este dispositivo (Puedes cambiarlo por Rubí, Sofía, Vanessa, etc. según la sesión actual)
    private val alumnoConectado = "Vanessa"

    var estadoAsesoria by mutableStateOf("Esperando asesoría...")
    var isAsesoriaAceptada by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        createNotificationChannel(this)

        setContent {
            WearApp(
                alumnoActual = alumnoConectado,
                estadoAsesoria = estadoAsesoria,
                isAceptada = isAsesoriaAceptada,
                onAceptarManual = {
                    if (!isAsesoriaAceptada) {
                        isAsesoriaAceptada = true
                        estadoAsesoria = "¡Asesoría Aceptada!"
                        enviarNotificacionPersonalizada(this, "Ing. Roberto", "Mañana", "11:00 AM", alumnoConectado)
                    }
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // Gesto de la muñeca para aceptar automáticamente
        if (!isAsesoriaAceptada && event != null) {
            val values = event.values
            if (values.isNotEmpty()) {
                if (Math.abs(values[0]) > 0.4f || Math.abs(values[1]) > 0.4f) {
                    isAsesoriaAceptada = true
                    estadoAsesoria = "¡Aceptada por Gesto!"
                    enviarNotificacionPersonalizada(this, "Ing. Roberto", "Mañana", "11:00 AM", alumnoConectado)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // Método que simula la recepción real desde la base de datos/backend del docente
    // Valida estrictamente que si va dirigido a otro alumno, este dispositivo no muestre nada.
    fun recibirSolicitudExclusiva(docente: String, fecha: String, hora: String, alumnoDestino: String) {
        if (alumnoDestino == alumnoConectado) {
            estadoAsesoria = "Asesoría de $docente"
            enviarNotificacionPersonalizada(this, docente, fecha, hora, alumnoDestino)
        } else {
            // Si la asesoría es para Rubí, Sofía u otro, este reloj (de Vanessa) la ignora por completo
            return
        }
    }
}

fun enviarNotificacionPersonalizada(context: Context, docente: String, fecha: String, hora: String, alumno: String) {
    val builder = NotificationCompat.Builder(context, "asesorias_channel")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Asesoría Asignada")
        .setContentText("$docente: $fecha - $hora")
        .setStyle(NotificationCompat.BigTextStyle().bigText("Hola $alumno, tienes una asesoría exclusiva con el docente $docente programada para el día $fecha a las $hora."))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)

    with(NotificationManagerCompat.from(context)) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            notify(2, builder.build())
        }
    }
}

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel("asesorias_channel", "Canal de Asesorias", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Notificaciones dirigidas y exclusivas"
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
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
    isAceptada: Boolean,
    onAceptarManual: () -> Unit
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
                        onClick = { onAceptarManual() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = androidx.compose.ui.graphics.Color(0xFF0D9488),
                            contentColor = androidx.compose.ui.graphics.Color.White,
                        ),
                    ) {
                        Text("Aceptar")
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
                                text = if (currentTime.isEmpty()) "Asesorías" else currentTime,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium,
                                color = androidx.compose.ui.graphics.Color(0xFF059669)
                            )
                        }
                    }

                    item {
                        ListHeader(
                            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text(
                                text = "$estadoAsesoria\n($alumnoActual)",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isAceptada) androidx.compose.ui.graphics.Color(0xFF059669) else androidx.compose.ui.graphics.Color(0xFF0D9488)
                            )
                        }
                    }

                    item {
                        Button(
                            onClick = { /* Acción estática de guía */ },
                            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = androidx.compose.ui.graphics.Color(0xFFF1F5F9),
                                contentColor = androidx.compose.ui.graphics.Color(0xFF0F172A)
                            )
                        ) {
                            Text(if (isAceptada) "✓ Asesoría Aceptada" else "Gira la muñeca para aceptar")
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
    WearApp("Vanessa", "Esperando asesoría...", false, {})
}
