package com.example.asesoriasutn;

import com.google.gson.annotations.SerializedName;

public class Grupo {
    @SerializedName("id")
    private int id;

    // Cambia "nombre" por el nombre exacto de la columna en tu tabla grupos de Supabase
    // (Ej: "nombre", "nombre_grupo", "codigo", etc.)
    @SerializedName("nombre")
    private String nombre;

    public Grupo() {}

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre != null ? nombre : String.valueOf(id); // Si el nombre viene nulo, muestra el ID como respaldo
    }
}
