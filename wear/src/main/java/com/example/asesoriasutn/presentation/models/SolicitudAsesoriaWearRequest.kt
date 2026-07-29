package com.example.asesoriasutn.wear.presentation.models

import com.google.gson.annotations.SerializedName

data class SolicitudAsesoriaWearRequest(
    @SerializedName("correo_alumno")
    val correoAlumno: String,
    
    @SerializedName("correo_docente")
    val correoDocente: String,
    
    @SerializedName("docente_id")
    val docenteId: Long,
    
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
