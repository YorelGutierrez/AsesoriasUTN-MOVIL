package com.example.asesoriasutn;

import com.google.gson.annotations.SerializedName;

public class AsesoriaRequest {

    @SerializedName("alumno_id") // Debe coincidir con la columna en Supabase
    private String alumnoId;

    @SerializedName("tema") // Debe coincidir con la columna en Supabase
    private String tema;

    @SerializedName("objetivo") // Debe coincidir con la columna en Supabase
    private String objetivo;

    @SerializedName("fecha_hora") // Debe coincidir con la columna en Supabase (o fecha / hora separadas según tu tabla)
    private String fechaHora;

    public AsesoriaRequest(String alumnoId, String tema, String objetivo, String fechaHora) {
        this.alumnoId = alumnoId;
        this.tema = tema;
        this.objetivo = objetivo;
        this.fechaHora = fechaHora;
    }

    // Getters y Setters opcionales
}