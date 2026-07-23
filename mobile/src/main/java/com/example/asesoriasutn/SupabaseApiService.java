package com.example.asesoriasutn;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Body;

public interface SupabaseApiService {

    @Headers("Accept: application/json")
    @GET("rest/v1/users?select=*")
    Call<List<Alumno>> getAlumnos();

    @POST("rest/v1/asesorias")
    Call<Void> registrarAsesoria(@Body AsesoriaRequest asesoria);
}