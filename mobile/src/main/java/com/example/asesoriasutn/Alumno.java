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

    public String getCorreo() {
        return (user != null) ? user.getEmail() : null;
    }

    // MÉTODO AÑADIDO: Permite extraer la matrícula del usuario vinculado para formar el correo institucional
    public String getMatricula() {
        return (user != null) ? user.getMatricula() : null;
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