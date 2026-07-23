# Implementación de Guardado en Supabase para SolicitudDocente

Este plan detalla los pasos para que los alumnos puedan enviar sus solicitudes de asesoría a la base de datos de Supabase desde la nueva interfaz de usuario.

## User Review Required

> [!IMPORTANT]
> **Estructura de la Base de Datos**: He asumido que existe (o se creará) una tabla llamada `solicitudes_asesoria`. Si el nombre de la tabla o de las columnas es diferente, por favor dímelo.
>
> **Identidad del Alumno**: Actualmente la aplicación no guarda el ID del alumno logueado. Como solución temporal, usaré un ID estático o el correo, pero lo ideal sería que `MainActivity` pasara el ID real.

## Proposed Changes

### [Model Layer]

#### [NEW] [SolicitudAsesoriaRequest.kt](file:///C:/Users/vanes/AndroidStudioProjects/AsesoriasUTN-MOVIL/mobile/src/main/java/com/example/asesoriasutn/SolicitudAsesoriaRequest.kt)
Crearemos una clase de datos en Kotlin para enviar a Supabase con los siguientes campos:
- `docenteId`: ID del docente seleccionado.
- `tema`: Texto de "¿Qué quieres aprender?".
- `objetivo`: Objetivo de la asesoría.
- `modalidad`: Presencial, Virtual o Híbrida.
- `fechaHora`: Combinación de fecha y hora seleccionadas.
- `tieneConocimiento`: Boolean del checkbox.
- `necesitaMaterial`: Boolean del checkbox.
- `tieneEjercicios`: Boolean del checkbox.

### [Network Layer]

#### [MODIFY] [SupabaseApiService.java](file:///C:/Users/vanes/AndroidStudioProjects/AsesoriasUTN-MOVIL/mobile/src/main/java/com/example/asesoriasutn/SupabaseApiService.java)
Añadiremos el endpoint POST para registrar la solicitud:
```java
@POST("rest/v1/solicitudes_asesoria")
Call<Void> registrarSolicitud(@Body SolicitudAsesoriaRequest solicitud);
```

### [UI Layer]

#### [MODIFY] [SolicitudDocente.kt](file:///C:/Users/vanes/AndroidStudioProjects/AsesoriasUTN-MOVIL/mobile/src/main/java/com/example/asesoriasutn/SolicitudDocente.kt)
- Implementaremos la lógica dentro del listener del botón `btnEnviarSolicitud`.
- Recolectaremos los estados de los 3 CheckBoxes y el valor del Spinner de modalidad.
- Llamaremos a la API y mostraremos un Toast de éxito o error.

## Verification Plan

### Automated Tests
- Compilación del proyecto para asegurar que las nuevas clases y tipos coincidan.

### Manual Verification
- Abrir la pantalla de solicitar asesoría como alumno.
- Completar todos los campos, marcar los checkboxes y seleccionar un docente.
- Presionar "Enviar" y verificar que aparezca el mensaje de éxito.
