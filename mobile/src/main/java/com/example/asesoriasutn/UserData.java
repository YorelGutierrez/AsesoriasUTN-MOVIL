package com.example.asesoriasutn;

import com.google.gson.annotations.SerializedName;

public class UserData {
    @SerializedName("nombres")
    private String nombres;

    @SerializedName("apellido_paterno")
    private String apellidoPaterno;

    public UserData() {}

    public String getNombres() {
        return nombres;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }
}