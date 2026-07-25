package com.example.asesoriasutn;

import com.google.gson.annotations.SerializedName;

public class AsesoriaRequest {

    @SerializedName("alumno_id")
    private int alumnoId;

    @SerializedName("tema")
    private String tema;

    @SerializedName("objetivo")
    private String objetivo;

    @SerializedName("fecha_hora")
    private String fechaHora;

    @SerializedName("modalidad")
    private String modalidad;

    @SerializedName("docente_id")
    private Long docenteId;

    @SerializedName("correo_alumno")
    private String correoAlumno;

    @SerializedName("correo_docente")
    private String correoDocente;

    public AsesoriaRequest(int alumnoId, String tema, String objetivo, String fechaHora, String modalidad, Long docenteId, String correoAlumno, String correoDocente) {
        this.alumnoId = alumnoId;
        this.tema = tema;
        this.objetivo = objetivo;
        this.fechaHora = fechaHora;
        this.modalidad = modalidad;
        this.docenteId = docenteId;
        this.correoAlumno = correoAlumno;
        this.correoDocente = correoDocente;
    }

    public int getAlumnoId() { return alumnoId; }
    public String getTema() { return tema; }
    public String getObjetivo() { return objetivo; }
    public String getFechaHora() { return fechaHora; }
    public String getModalidad() { return modalidad; }
    public Long getDocenteId() { return docenteId; }
    public String getCorreoAlumno() { return correoAlumno; }
    public String getCorreoDocente() { return correoDocente; }
}
