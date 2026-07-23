package com.example.asesoriasutn

import com.google.gson.annotations.SerializedName

data class Docente(
    @SerializedName("id") val id: Int,
    @SerializedName("nombres") val nombres: String?,
    @SerializedName("apellido_paterno") val apellidoPaterno: String?
) {
    fun getNombreCompleto(): String {
        return "$nombres ${apellidoPaterno ?: ""}".trim()
    }
}