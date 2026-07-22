package com.example.asesoriasutn;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface SupabaseApiService {
    @GET("rest/v1/alumnos")
    Call<List<Alumno>> getAlumnos();

    @POST("rest/v1/asesorias")
    Call<Void> registrarAsesoria(@Body AsesoriaRequest request);
}
