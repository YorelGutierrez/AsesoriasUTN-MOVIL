package com.example.asesoriasutn;

import com.google.gson.annotations.SerializedName;

public class Alumno {
    @SerializedName("id")
    private int id;

    @SerializedName("nombres")
    private String nombres;

    @SerializedName("apellido_paterno")
    private String apellidoPaterno;

    // Constructor vacío requerido por Gson
    public Alumno() {}

    public int getId() {
        return id;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    // Opcional: Si quieres mostrar el nombre completo en el Spinner
    public String getNombreCompleto() {
        return nombres + (apellidoPaterno != null ? " " + apellidoPaterno : "");
    }
}