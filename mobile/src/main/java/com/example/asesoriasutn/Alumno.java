package com.example.asesoriasutn;

import com.google.gson.annotations.SerializedName;

public class Alumno {
    @SerializedName("id")
    private int id;

    @SerializedName("nombre") // Ajusta según el nombre de la columna en tu tabla (ej: "nombre", "correo", etc.)
    private String nombre;

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}