# Implementación de Consulta de Solicitudes en Wear OS

He habilitado la funcionalidad para que el botón "Mis Solicitudes" en el reloj realmente consulte la base de datos de Supabase y muestre tus peticiones recientes.

## Cambios realizados

### 1. Capa de Red
- **[SupabaseWearApiService.kt](file:///C:/Users/vanes/AndroidStudioProjects/AsesoriasUTN-MOVIL/wear/src/main/java/com/example/asesoriasutn/presentation/network/SupabaseWearApiService.kt)**: Se añadió el método `getSolicitudesPorAlumno` para permitir la lectura de datos desde el reloj.

### 2. Interfaz Dinámica en el Reloj
- **[MainActivity.kt](file:///C:/Users/vanes/AndroidStudioProjects/AsesoriasUTN-MOVIL/wear/src/main/java/com/example/asesoriasutn/presentation/MainActivity.kt)**:
    - Se implementó una lista reactiva que muestra las solicitudes debajo de los botones principales.
    - Cada solicitud se presenta en una **Tarjeta (Card)** con el tema de la asesoría en negrita y la fecha/hora debajo.
    - Se añadió un estado de carga para informar al usuario mientras se obtienen los datos.

## Verificación
- **Build**: El módulo `wear` compila correctamente (`BUILD SUCCESSFUL`).
- **Lógica**: Al presionar "Mis Solicitudes", el reloj ahora realiza una petición GET a Supabase filtrando por el correo del alumno.

> [!TIP]
> **Limpieza de Pantalla**: He configurado el botón inferior ("Limpiar") para que también oculte la lista de solicitudes, permitiéndote regresar a la vista simplificada del reloj en cualquier momento.
