package com.example.asesoriasutn;

import com.google.gson.annotations.SerializedName;

public class AsesoriaRequest {
    @SerializedName("alumno")
    private String alumno;
    @SerializedName("tema")
    private String tema;
    @SerializedName("objetivo")
    private String objetivo;
    @SerializedName("fecha_hora")
    private String fechaHora;

    public AsesoriaRequest(String alumno, String tema, String objetivo, String fechaHora) {
        this.alumno = alumno;
        this.tema = tema;
        this.objetivo = objetivo;
        this.fechaHora = fechaHora;
    }
}
