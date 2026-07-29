package com.example.asesoriasutn.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
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
import java.util.*

class MaestroActivity : ComponentActivity(), DataClient.OnDataChangedListener {

    private lateinit var sessionManager: SessionManager
    private var maestroNombre by mutableStateOf("")
    private var maestroEmail by mutableStateOf("")
    private var listaSolicitudes by mutableStateOf<List<SolicitudAsesoriaWearRequest>>(listOf())
    private var isLoading by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)
        maestroNombre = sessionManager.getUserName() ?: "Maestro"
        maestroEmail = sessionManager.getUserEmail() ?: ""

        setContent {
            MaestroApp(
                maestroNombre = maestroNombre,
                listaSolicitudes = listaSolicitudes,
                isLoading = isLoading,
                onCargarSolicitudes = { cargarSolicitudes() },
                onCerrarSesion = {
                    sessionManager.clearSession()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            )
        }
    }

    private fun cargarSolicitudes() {
        if (maestroEmail.isEmpty()) {
            Toast.makeText(this, "Email no disponible", Toast.LENGTH_SHORT).show()
            return
        }
        isLoading = true
        RetrofitClient.apiService.getSolicitudesPorDocente("eq.$maestroEmail").enqueue(object : Callback<List<SolicitudAsesoriaWearRequest>> {
            override fun onResponse(call: Call<List<SolicitudAsesoriaWearRequest>>, response: Response<List<SolicitudAsesoriaWearRequest>>) {
                isLoading = false
                if (response.isSuccessful && response.body() != null) {
                    listaSolicitudes = response.body()!!
                    if (listaSolicitudes.isEmpty()) {
                        Toast.makeText(this@MaestroActivity, "No hay solicitudes pendientes", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MaestroActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<SolicitudAsesoriaWearRequest>>, t: Throwable) {
                isLoading = false
                Toast.makeText(this@MaestroActivity, "Fallo de red", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        Wearable.getDataClient(this).addListener(this)
    }

    override fun onPause() {
        super.onPause()
        Wearable.getDataClient(this).removeListener(this)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/user_session") {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val role = dataMap.getString("role", "alumno")
                
                if (role == "logout" || role == "alumno") {
                    sessionManager.clearSession()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    val nombre = dataMap.getString("nombre", "Maestro")
                    val email = dataMap.getString("email", "")
                    
                    maestroNombre = nombre
                    maestroEmail = email
                    sessionManager.saveSession(nombre, email, role)
                }
            }
        }
    }
}

@Composable
fun MaestroApp(
    maestroNombre: String,
    listaSolicitudes: List<SolicitudAsesoriaWearRequest>,
    isLoading: Boolean,
    onCargarSolicitudes: () -> Unit,
    onCerrarSesion: () -> Unit
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
                        onClick = { if (listaSolicitudes.isEmpty()) onCargarSolicitudes() else onCerrarSesion() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (listaSolicitudes.isEmpty()) Color(0xFF0D9488) else Color(0xFFDC2626),
                            contentColor = Color.White,
                        ),
                    ) {
                        Text(if (listaSolicitudes.isEmpty()) "Ver Peticiones" else "Cerrar")
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
                                text = if (currentTime.isEmpty()) "Panel Maestro" else currentTime,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF0D9488)
                            )
                        }
                    }

                    item {
                        ListHeader(
                            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text(
                                text = if (isLoading) "Cargando..." else "Hola, $maestroNombre",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    if (listaSolicitudes.isNotEmpty()) {
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
                                        text = "De: ${solicitud.correoAlumno}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "Tema: ${solicitud.queAprender}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF0D9488),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = solicitud.fechaHora,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    } else if (!isLoading) {
                        item {
                            Text(
                                text = "Pulsa el botón inferior para ver tus solicitudes pendientes",
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }
    }
}
