package com.example.asesoriasutn;

import com.google.gson.annotations.SerializedName;

public class Alumno {
    @SerializedName("id")
    private int id;

    @SerializedName("users")
    private UserData user;

    @SerializedName("grupos")
    private Grupo grupo;

    public Alumno() {}

    public int getId() {
        return id;
    }

    public UserData getUser() {
        return user;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public String getNombreCompleto() {
        if (user != null) {
            String nombres = user.getNombres() != null ? user.getNombres() : "";
            String apellidoPaterno = user.getApellidoPaterno() != null ? " " + user.getApellidoPaterno() : "";
            return (nombres + apellidoPaterno).trim();
        }
        return "Sin nombre";
    }
}