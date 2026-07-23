package com.example.asesoriasutn

import com.google.gson.annotations.SerializedName

data class SolicitudAsesoriaRequest(
    @SerializedName("docente_id")
    val docenteId: Int,
    
    @SerializedName("tema")
    val tema: String,
    
    @SerializedName("objetivo")
    val objetivo: String,
    
    @SerializedName("modalidad")
    val modalidad: String,
    
    @SerializedName("fecha_hora")
    val fechaHora: String,
    
    @SerializedName("tiene_conocimiento")
    val tieneConocimiento: Boolean,
    
    @SerializedName("necesita_material")
    val necesitaMaterial: Boolean,
    
    @SerializedName("tiene_ejercicios")
    val tieneEjercicios: Boolean
)
