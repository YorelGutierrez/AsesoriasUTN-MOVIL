package com.example.asesoriasutn.presentation.network

import com.example.asesoriasutn.presentation.models.SolicitudAsesoriaWearRequest
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface SupabaseWearApiService {
    @Headers(
        "apikey: sb_publishable_8hbEGvtOKw3SvnVz7apPlg_KWVdL5xe",
        "Authorization: Bearer sb_publishable_8hbEGvtOKw3SvnVz7apPlg_KWVdL5xe",
        "Content-Type: application/json",
        "Prefer: return=minimal"
    )
    @POST("rest/v1/solicitudes_asesoria")
    fun registrarSolicitud(@Body solicitud: SolicitudAsesoriaWearRequest): Call<Void>

    @Headers(
        "apikey: sb_publishable_8hbEGvtOKw3SvnVz7apPlg_KWVdL5xe",
        "Authorization: Bearer sb_publishable_8hbEGvtOKw3SvnVz7apPlg_KWVdL5xe",
        "Content-Type: application/json"
    )
    @GET("rest/v1/solicitudes_asesoria")
    fun getSolicitudesPorAlumno(
        @retrofit2.http.Query("correo_alumno") operadorIgual: String
    ): Call<List<SolicitudAsesoriaWearRequest>>
}

object RetrofitClient {
    private const val BASE_URL = "https://jxeftmhxwjiolbxiklyc.supabase.co/"

    private val okHttpClient = OkHttpClient.Builder().build()

    val apiService: SupabaseWearApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseWearApiService::class.java)
    }
}
