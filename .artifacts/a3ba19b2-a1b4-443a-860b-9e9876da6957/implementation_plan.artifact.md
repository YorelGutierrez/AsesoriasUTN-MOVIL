# Plan de Resolución de Error 400 (Bad Request)

El error 400 indica que la estructura del JSON enviado no coincide con lo que Supabase espera en la base de datos.

## User Review Required

> [!IMPORTANT]
> **Detalle del Error**: Para solucionar esto rápido, necesito que revises tu **Logcat** en Android Studio después de aplicar los cambios de "Logging" que haré a continuación. Busca la etiqueta `SUPABASE_DETALLE`.

## Proposed Changes

### [Network Layer]

#### [MODIFY] [SolicitudDocente.kt](file:///C:/Users/vanes/AndroidStudioProjects/AsesoriasUTN-MOVIL/mobile/src/main/java/com/example/asesoriasutn/SolicitudDocente.kt)
- Mejorar el `onResponse` para extraer y mostrar el cuerpo del error (`response.errorBody().string()`).
- Verificar que el formato de `fechaHora` sea compatible con Supabase (ISO 8601).

#### [MODIFY] [AgendarAsesoria.java](file:///C:/Users/vanes/AndroidStudioProjects/AsesoriasUTN-MOVIL/mobile/src/main/java/com/example/asesoriasutn/AgendarAsesoria.java)
- Añadir registro detallado del error de respuesta para identificar campos mal mapeados.

### [Model Layer]

#### [REVIEW] [SolicitudAsesoriaRequest.kt](file:///C:/Users/vanes/AndroidStudioProjects/AsesoriasUTN-MOVIL/mobile/src/main/java/com/example/asesoriasutn/SolicitudAsesoriaRequest.kt)
- Comparar los nombres de `@SerializedName` con las columnas reales de tu tabla `solicitudes_asesoria`.

## Verification Plan

### Manual Verification
1. Aplicar cambios de logging.
2. Intentar enviar una solicitud.
3. Revisar el Logcat y copiar aquí el mensaje de error de Supabase.
