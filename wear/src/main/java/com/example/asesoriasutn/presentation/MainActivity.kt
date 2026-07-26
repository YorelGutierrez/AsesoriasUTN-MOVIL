package com.example.asesoriasutn.presentation

import android.os.Bundle
import android.util.Log
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
import com.example.asesoriasutn.presentation.models.SolicitudAsesoriaWearRequest
import com.example.asesoriasutn.presentation.network.RetrofitClient
import com.example.asesoriasutn.presentation.theme.AsesoriasUTNTheme
import kotlinx.coroutines.delay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp()
        }
    }
}

@Composable
fun WearApp() {
    AsesoriasUTNTheme {
        AppScaffold {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()

            var currentTime by remember { mutableStateOf("") }
            var estadoMensaje by remember { mutableStateOf("") }
            var isLoading by remember { mutableStateOf(false) }
            var listaSolicitudes by remember { mutableStateOf<List<SolicitudAsesoriaWearRequest>>(listOf()) }

            // Actualizar reloj cada segundo
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
                            estadoMensaje = ""
                            listaSolicitudes = listOf()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0D9488),
                            contentColor = Color.White,
                        ),
                    ) {
                        Text(if (estadoMensaje.isEmpty() && listaSolicitudes.isEmpty()) "Listo" else "Limpiar")
                    }
                },
            ) { contentPadding ->
                TransformingLazyColumn(contentPadding = contentPadding, state = listState) {
                    // Reloj principal
                    item {
                        ListHeader(
                            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text(
                                text = if (currentTime.isEmpty()) "Cargando..." else currentTime,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF059669)
                            )
                        }
                    }

                    // Mensaje dinámico de estado
                    if (estadoMensaje.isNotEmpty()) {
                        item {
                            ListHeader(
                                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                                transformation = SurfaceTransformation(transformationSpec),
                            ) {
                                Text(
                                    text = estadoMensaje,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF0D9488)
                                )
                            }
                        }
                    }

                    // Botón 1: Pedir Asesoría
                    item {
                        Button(
                            onClick = {
                                if (!isLoading) {
                                    isLoading = true
                                    estadoMensaje = "Enviando..."

                                    val sdfFecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                    val fechaActualStr = sdfFecha.format(Date())

                                    val nuevaSolicitud = SolicitudAsesoriaWearRequest(
                                        correoAlumno = "vanessa@utnay.edu.mx",
                                        correoDocente = "docente@utnay.edu.mx",
                                        docenteId = 1L,
                                        queAprender = "Asesoría rápida desde Wear OS",
                                        conocimientoPrevio = true,
                                        necesitaMaterial = false,
                                        ejerciciosEspecificos = true,
                                        objetivo = "Consulta rápida de reloj",
                                        modalidad = "Virtual / En línea",
                                        fechaHora = fechaActualStr
                                    )

                                    enviarSolicitudASupabase(nuevaSolicitud) { exito, mensaje ->
                                        isLoading = false
                                        estadoMensaje = if (exito) "¡Enviado con éxito!" else "Error: $mensaje"
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0D9488),
                                contentColor = Color.White
                            )
                        ) {
                            Text(if (isLoading) "Enviando..." else "Pedir Asesoría")
                        }
                    }

                    // Botón 2: Ver Horarios
                    item {
                        Button(
                            onClick = { estadoMensaje = "Sin horarios nuevos." },
                            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF1F5F9),
                                contentColor = Color(0xFF0F172A)
                            )
                        ) {
                            Text("Ver Horarios")
                        }
                    }

                    // Botón 3: Mis Solicitudes
                    item {
                        Button(
                            onClick = {
                                if (!isLoading) {
                                    isLoading = true
                                    estadoMensaje = "Cargando solicitudes..."
                                    cargarSolicitudesDesdeSupabase("vanessa@utnay.edu.mx") { exito, solicitudes ->
                                        isLoading = false
                                        if (exito) {
                                            listaSolicitudes = solicitudes
                                            estadoMensaje = if (solicitudes.isEmpty()) "No tienes solicitudes." else ""
                                        } else {
                                            estadoMensaje = "Error al cargar."
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF1F5F9),
                                contentColor = Color(0xFF0F172A)
                            )
                        ) {
                            Text("Mis Solicitudes")
                        }
                    }

                    // Mostrar lista de solicitudes si existen
                    if (listaSolicitudes.isNotEmpty()) {
                        item {
                            Text(
                                text = "Tus Peticiones:",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        items(listaSolicitudes.size) { index ->
                            val solicitud = listaSolicitudes[index]
                            Card(
                                onClick = { /* Detalle opcional */ },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp).transformedHeight(this, transformationSpec),
                                transformation = SurfaceTransformation(transformationSpec),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF8FAFC)
                                )
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = solicitud.queAprender,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        ),
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

fun enviarSolicitudASupabase(solicitud: SolicitudAsesoriaWearRequest, callback: (Boolean, String) -> Unit) {
    RetrofitClient.apiService.registrarSolicitud(solicitud).enqueue(object : Callback<Void> {
        override fun onResponse(call: Call<Void>, response: Response<Void>) {
            if (response.isSuccessful) {
                callback(true, "OK")
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error"
                Log.e("WEAR_SUPABASE", "Error: $errorMsg")
                callback(false, "${response.code()}")
            }
        }

        override fun onFailure(call: Call<Void>, t: Throwable) {
            Log.e("WEAR_SUPABASE", "Fallo: ${t.message}")
            callback(false, t.message ?: "Fallo de red")
        }
    })
}

fun cargarSolicitudesDesdeSupabase(correo: String, callback: (Boolean, List<SolicitudAsesoriaWearRequest>) -> Unit) {
    RetrofitClient.apiService.getSolicitudesPorAlumno("eq.$correo").enqueue(object : Callback<List<SolicitudAsesoriaWearRequest>> {
        override fun onResponse(call: Call<List<SolicitudAsesoriaWearRequest>>, response: Response<List<SolicitudAsesoriaWearRequest>>) {
            if (response.isSuccessful && response.body() != null) {
                callback(true, response.body()!!)
            } else {
                Log.e("WEAR_SUPABASE", "Error al cargar: ${response.code()}")
                callback(false, listOf())
            }
        }

        override fun onFailure(call: Call<List<SolicitudAsesoriaWearRequest>>, t: Throwable) {
            Log.e("WEAR_SUPABASE", "Fallo al cargar: ${t.message}")
            callback(false, listOf())
        }
    })
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun DefaultPreview() {
    WearApp()
}
