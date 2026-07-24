package com.example.asesoriasutn

import com.google.gson.annotations.SerializedName

data class SolicitudAsesoriaRequest(
    @SerializedName("docente_id")
    val docenteId: Int,

    @SerializedName("que_aprender")
    val queAprender: String,

    @SerializedName("conocimiento_previo")
    val conocimientoPrevio: Boolean,

    @SerializedName("necesita_material")
    val necesitaMaterial: Boolean,

    @SerializedName("ejercicios_especificos")
    val ejerciciosEspecificos: Boolean,

    @SerializedName("objetivo")
    val objetivo: String,

    @SerializedName("modalidad")
    val modalidad: String,

    @SerializedName("fecha_hora")
    val fechaHora: String
)
