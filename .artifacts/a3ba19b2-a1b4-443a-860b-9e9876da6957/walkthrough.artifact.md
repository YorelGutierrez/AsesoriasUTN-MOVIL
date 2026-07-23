# Resumen de Cambios: Solicitud de Asesoría (Alumno)

He completado la implementación necesaria para que los alumnos puedan enviar solicitudes de asesoría a Supabase.

## Cambios realizados

### 1. Modelo de Datos
- **SolicitudAsesoriaRequest.kt**: Creada una nueva clase de datos en Kotlin que mapea todos los campos del formulario, incluyendo los CheckBoxes (`tiene_conocimiento`, `necesita_material`, `tiene_ejercicios`) y la modalidad.

### 2. Capa de Red
- **SupabaseApiService.java**: Se añadió el endpoint `registrarSolicitud` que apunta a la tabla `solicitudes_asesoria` en Supabase.

### 3. Interfaz de Usuario
- **SolicitudDocente.kt**:
    - Se implementó la lógica del botón "Enviar Solicitud".
    - Se capturan ahora los estados de los 3 CheckBoxes y el valor del Spinner de modalidad.
    - Se integró la llamada a la API con manejo de errores y mensajes Toast de confirmación.
    - Se corrigieron errores de sintaxis en el interceptor de OkHttp (uso de métodos en lugar de acceso directo a campos).

## Verificación
- El proyecto compila correctamente (`BUILD SUCCESSFUL`).
- Se validó que todos los campos del formulario se incluyan en el objeto de envío.

> [!WARNING]
> **Base de Datos**: Asegúrate de tener creada la tabla `solicitudes_asesoria` en tu proyecto de Supabase con los nombres de columna especificados en el modelo (ej: `docente_id`, `tema`, `modalidad`, `tiene_conocimiento`, etc.) para que el guardado sea efectivo.
