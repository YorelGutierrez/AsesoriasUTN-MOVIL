package com.example.asesoriasutn;

import com.google.gson.annotations.SerializedName;

public class UserData {
    @SerializedName("nombres")
    private String nombres;

    @SerializedName("apellido_paterno")
    private String apellidoPaterno;

    @SerializedName("email")
    private String email;

    public UserData() {}

    public String getNombres() {
        return nombres;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public String getEmail() {
        return email;
    }
}
