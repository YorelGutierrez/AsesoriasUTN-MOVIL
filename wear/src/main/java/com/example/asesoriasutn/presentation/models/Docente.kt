package com.example.asesoriasutn.wear.presentation.models

import com.google.gson.annotations.SerializedName

data class Docente(
    @SerializedName("id") val id: Int,
    @SerializedName("nombres") val nombres: String?,
    @SerializedName("apellido_paterno") val apellidoPaterno: String?,
    @SerializedName("email") val correo: String?
) {
    fun getNombreCompleto(): String {
        return "$nombres ${apellidoPaterno ?: ""}".trim()
    }
}
