# Restauración de Envío de Correos a Supabase

He restaurado la lógica para enviar los correos electrónicos de los alumnos y docentes a Supabase, ahora que las columnas correspondientes han sido agregadas a la base de datos. También he eliminado los diálogos de diagnóstico que usamos para identificar el error 400.

## Cambios realizados

### 1. Modelo de Datos (Java)
- **[AsesoriaRequest.java](file:///C:/Users/vanes/AndroidStudioProjects/AsesoriasUTN-MOVIL/mobile/src/main/java/com/example/asesoriasutn/AsesoriaRequest.java)**:
    - Se eliminó el modificador `transient` de los campos `correoAlumno` y `correoDocente`.
    - Se restauraron las anotaciones `@SerializedName("correo_alumno")` y `@SerializedName("correo_docente")`.
    - Se actualizó el constructor para incluir estos campos de forma obligatoria.

### 2. Limpieza de Diagnóstico (UI)
- **[AgendarAsesoria.java](file:///C:/Users/vanes/AndroidStudioProjects/AsesoriasUTN-MOVIL/mobile/src/main/java/com/example/asesoriasutn/AgendarAsesoria.java)**:
    - Se eliminó el cuadro de diálogo de error "Supabase dice..." que mostraba detalles técnicos del Error 400.
    - Se mantuvo la lógica de guardado que ahora envía los correos reales a las nuevas columnas de la tabla.
- **[SolicitudDocente.kt](file:///C:/Users/vanes/AndroidStudioProjects/AsesoriasUTN-MOVIL/mobile/src/main/java/com/example/asesoriasutn/SolicitudDocente.kt)**:
    - Se eliminó el cuadro de diálogo de diagnóstico técnico.

## Verificación
- **Build**: El proyecto compila correctamente (`BUILD SUCCESSFUL`).
- **Datos**: Las solicitudes ahora deberían guardarse con toda la información (ID y Correo) en las tablas de Supabase sin marcar error 400.

> [!TIP]
> **Próximos pasos**: Puedes realizar una prueba de guardado tanto desde la vista de Alumno como desde la de Docente para confirmar que los datos aparecen correctamente en tu panel de Supabase.
