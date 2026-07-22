package com.example.asesoriasutn;

import com.google.gson.annotations.SerializedName;

public class Alumno {
    @SerializedName("id")
    private int id;

    @SerializedName("nombre")
    private String nombre;

    // Constructor vacío requerido por Gson
    public Alumno() {}

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}