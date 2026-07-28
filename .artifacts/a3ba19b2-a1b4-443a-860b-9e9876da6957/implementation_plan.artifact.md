# Plan: Sincronización de Identidad (Phone to Wear)

Este plan permitirá que el módulo Wear OS detecte automáticamente quién inició sesión en el teléfono, eliminando los datos fijos de "Vanessa".

## User Review Required

> [!IMPORTANT]
> **Requisito de Conexión**: Para que los datos se sincronicen, el reloj y el teléfono deben estar vinculados vía Bluetooth (o mediante el emulador configurado con Wear OS paired).
>
> **Seguridad**: Solo se enviará el Nombre y el Correo institucional; la contraseña nunca viajará entre dispositivos por seguridad.

## Proposed Changes

### [Mobile Module]

#### [MODIFY] [MainActivity.java](file:///C:/Users/vanes/AndroidStudioProjects/AsesoriasUTN-MOVIL/mobile/src/main/java/com/example/asesoriasutn/MainActivity.java)
- Implementar la interfaz `DataClient`.
- Crear el método `sendUserDataToWear(String nombre, String email)` que envíe los datos al path `/user_session`.

### [Wear Module]

#### [MODIFY] [MainActivity.kt](file:///C:/Users/vanes/AndroidStudioProjects/AsesoriasUTN-MOVIL/wear/src/main/java/com/example/asesoriasutn/presentation/MainActivity.kt)
- Implementar `DataClient.OnDataChangedListener`.
- Actualizar dinámicamente las variables de estado `alumnoConectadoNombre` y el correo usado en las peticiones de Supabase.

#### [NEW] [SessionManager.kt](file:///C:/Users/vanes/AndroidStudioProjects/AsesoriasUTN-MOVIL/wear/src/main/java/com/example/asesoriasutn/presentation/SessionManager.kt)
- Crear una clase para guardar los datos recibidos en el almacenamiento local del reloj para persistencia offline.

## Verification Plan

### Manual Verification
1. Iniciar sesión en el celular con una cuenta distinta (ej: `tic-123456@utnay.edu.mx`).
2. Abrir la app en el reloj.
3. Verificar que el nombre en el encabezado del reloj cambie automáticamente al nuevo usuario.
4. Enviar una solicitud desde el reloj y verificar en Supabase que el `correo_alumno` sea el correcto.
