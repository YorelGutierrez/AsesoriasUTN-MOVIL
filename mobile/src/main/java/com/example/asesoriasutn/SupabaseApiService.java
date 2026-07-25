package com.example.asesoriasutn;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Body;
import retrofit2.http.Query;

public interface SupabaseApiService {

    @Headers("Accept: application/json")
    @GET("rest/v1/alumnos?select=id,users!user_id(nombres,apellido_paterno),grupos!grupo_id(id,nombre)")
    Call<List<Alumno>> getAlumnos();

    @Headers("Accept: application/json")
    @GET("rest/v1/users?select=id,nombres,apellido_paterno,email&rol=eq.docente")
    Call<List<Docente>> getDocentes();

    @Headers("Accept: application/json")
    @GET("rest/v1/sesiones_de_asesoria?select=*")
    Call<List<AsesoriaRequest>> getSesionesAsesoria();

    @Headers("Accept: application/json")
    @POST("rest/v1/sesiones_de_asesoria")
    Call<Void> registrarAsesoria(@Body AsesoriaRequest asesoria);

    @Headers("Accept: application/json")
    @POST("rest/v1/solicitudes_asesoria")
    Call<Void> registrarSolicitud(@Body SolicitudAsesoriaRequest solicitud);

    // Método para filtrar solicitudes únicamente para el docente indicado (ej: "eq.correo@utn.edu.mx")
    @Headers("Accept: application/json")
    @GET("rest/v1/solicitudes_asesoria")
    Call<List<SolicitudAsesoriaRequest>> getSolicitudesPorDocente(
            @Query("correo_docente") String operadorIgual
    );

    // Nuevo: Método para filtrar solicitudes únicamente para el alumno indicado (ej: "eq.alumno@utn.edu.mx")
    @Headers("Accept: application/json")
    @GET("rest/v1/solicitudes_asesoria")
    Call<List<SolicitudAsesoriaRequest>> getSolicitudesPorAlumno(
            @Query("correo_alumno") String operadorIgual
    );

    // Nuevo: Método general para consultar sesiones filtrando de forma segura donde el usuario sea alumno o docente
    @Headers("Accept: application/json")
    @GET("rest/v1/sesiones_de_asesoria")
    Call<List<AsesoriaRequest>> getSesionesPorUsuario(
            @Query("or") String filtroOr // Ej: "(correo_docente.eq.correo@utn.edu.mx,correo_alumno.eq.correo@utn.edu.mx)"
    );
}