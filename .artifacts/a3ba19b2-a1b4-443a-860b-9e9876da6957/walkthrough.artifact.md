# Sincronización de Notificaciones: Alumno y Docente

He implementado la lógica necesaria para que los alumnos reciban notificaciones en tiempo real cuando un docente les agenda una asesoría.

## Cambios realizados

### 1. Consulta Multi-Tabla (Red)
- **[SolicitudDocente.kt](file:///C:/Users/vanes/AndroidStudioProjects/AsesoriasUTN-MOVIL/mobile/src/main/java/com/example/asesoriasutn/SolicitudDocente.kt)**: La aplicación del alumno ahora consulta dos fuentes de datos en Supabase simultáneamente:
    - `solicitudes_asesoria`: Sus propias peticiones enviadas.
    - `sesiones_de_asesoria`: Las citas ya confirmadas y programadas por los docentes.

### 2. Notificaciones Inteligentes (UI)
- **Icono de Notificaciones**: Se actualizó la lógica para dar prioridad a las asesorías agendadas por los docentes. Si hay una nueva sesión confirmada, el alumno verá un mensaje claro: *"¡Atención! El docente ha programado una asesoría para ti"*.
- **Agenda Unificada**: El icono del calendario ahora muestra dos secciones bien diferenciadas:
    - **ASESORÍAS CONFIRMADAS**: Citas ya programadas por los docentes.
    - **SOLICITUDES ENVIADAS**: Peticiones del alumno que aún están en espera.

### 3. Filtro de Seguridad
- Se implementó un filtro estricto por correo electrónico institucional para asegurar que el alumno solo vea sus propias citas y no las de otros compañeros.

## Verificación
- **Build**: El proyecto compila correctamente (`BUILD SUCCESSFUL`).
- **Lógica**: Se integró el método `getSesionesPorUsuario` que utiliza un operador `OR` para buscar al alumno en la base de datos de forma eficiente.

> [!TIP]
> **Prueba de flujo**: Inicia sesión como docente, agenda una cita para un alumno, y luego entra con la cuenta de ese alumno. Al presionar la campana de notificaciones, debería aparecer el aviso de la nueva asesoría.
