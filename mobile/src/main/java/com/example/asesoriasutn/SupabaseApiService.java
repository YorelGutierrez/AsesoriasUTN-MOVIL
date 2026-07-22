package com.example.asesoriasutn;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface SupabaseApiService {

    // Cambia "alumnos" por el nombre exacto de tu tabla en Supabase si es diferente
    @GET("rest/v1/alumnos?select=*")
    Call<List<Alumno>> getAlumnos();

    @POST("rest/v1/asesorias") // Cambia por tu tabla de registros de asesorías
    Call<Void> registrarAsesoria(@Body AsesoriaRequest asesoria);
}