# Sincronización de Sesión: Phone -> Wear OS

He implementado un sistema de sincronización de identidad que permite al reloj detectar automáticamente quién inició sesión en el teléfono, eliminando los datos fijos de "Vanessa".

## Cambios realizados

### 1. Módulo Móvil (Emisor)
- **[MainActivity.java](file:///C:/Users/vanes/AndroidStudioProjects/AsesoriasUTN-MOVIL/mobile/src/main/java/com/example/asesoriasutn/MainActivity.java)**:
    - Se integró la **Wearable Data Layer API**.
    - Al realizar un inicio de sesión exitoso, el teléfono ahora envía un "paquete de datos" al reloj con el nombre del usuario y su correo institucional.
    - Se añadió el método `sendUserDataToWear` para gestionar este envío de forma segura y urgente.

### 2. Módulo Wear OS (Receptor y Persistencia)
- **[MainActivity.kt](file:///C:/Users/vanes/AndroidStudioProjects/AsesoriasUTN-MOVIL/wear/src/main/java/com/example/asesoriasutn/presentation/MainActivity.kt)**:
    - Se implementó el escucha `OnDataChangedListener` para recibir los datos del teléfono en tiempo real.
    - Las variables `alumnoConectadoNombre` y `alumnoConectadoEmail` ahora son dinámicas y se actualizan solas al recibir la señal del celular.
    - Se vincularon estos datos con las peticiones a Supabase, asegurando que las solicitudes de asesoría se registren con la identidad correcta.
- **[SessionManager.kt](file:///C:/Users/vanes/AndroidStudioProjects/AsesoriasUTN-MOVIL/wear/src/main/java/com/example/asesoriasutn/presentation/SessionManager.kt)**:
    - Nueva clase encargada de guardar la identidad del usuario en el almacenamiento local del reloj. Esto permite que el reloj "recuerde" quién eres incluso si se reinicia o pierde conexión temporal con el teléfono.

## Cómo probar la sincronización
1. Asegúrate de que el reloj y el teléfono estén vinculados.
2. Abre la app en el celular e **inicia sesión** con cualquier cuenta (ej: `tic-310073@utnay.edu.mx`).
3. Verás que en el reloj, el encabezado cambia automáticamente de "Vanessa" al nombre de usuario correspondiente.
4. Cualquier solicitud que envíes desde el reloj ahora llegará a Supabase con tu correo real.

## Verificación
- El proyecto compila correctamente en ambos módulos (`BUILD SUCCESSFUL`).
- Se validó el flujo de datos desde Java (Mobile) hacia Kotlin (Wear).

> [!TIP]
> **Persistencia**: Una vez sincronizado, el reloj mantendrá tu sesión activa. No necesitas tener el celular encendido todo el tiempo para que el reloj sepa quién eres.
