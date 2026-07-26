# Plan de Implementación: Consultar Solicitudes en Wear OS

Este plan detalla los pasos para que el botón "Mis Solicitudes" en el reloj realmente traiga y muestre la lista de asesorías pedidas desde Supabase.

## User Review Required

> [!IMPORTANT]
> **Navegación en el Reloj**: Dado que la pantalla es pequeña, mostraré las solicitudes como elementos adicionales en la lista principal debajo de los botones cuando se presione "Mis Solicitudes".
>
> **Identidad**: Al igual que en la función de "Pedir Asesoría", por ahora usaré el correo de prueba `vanessa@utnay.edu.mx` hasta que implementemos la sincronización real de sesión.

## Proposed Changes

### [Network Layer]

#### [MODIFY] [SupabaseWearApiService.kt](file:///C:/Users/vanes/AndroidStudioProjects/AsesoriasUTN-MOVIL/wear/src/main/java/com/example/asesoriasutn/presentation/network/SupabaseWearApiService.kt)
- Añadir el método `getSolicitudesPorAlumno` con el parámetro de filtrado por correo.

### [UI Layer]

#### [MODIFY] [MainActivity.kt](file:///C:/Users/vanes/AndroidStudioProjects/AsesoriasUTN-MOVIL/wear/src/main/java/com/example/asesoriasutn/presentation/MainActivity.kt)
- Crear una nueva variable de estado `listaSolicitudes` para almacenar los datos recibidos.
- Implementar la función `cargarSolicitudesDesdeSupabase()`.
- Actualizar la interfaz para que, si la lista no está vacía, se muestren tarjetas con el tema y la fecha de cada asesoría.

## Verification Plan

### Manual Verification
1. Abrir la app en el reloj.
2. Presionar el botón "Mis Solicitudes".
3. Verificar que el texto cambie a "Cargando..." y luego aparezcan los temas de las asesorías enviadas previamente debajo de los botones.
