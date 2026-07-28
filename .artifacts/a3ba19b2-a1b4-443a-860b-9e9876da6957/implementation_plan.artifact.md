# Plan de Resolución: Error al Ejecutar la Aplicación

He detectado un conflicto potencial en la configuración del proyecto que podría estar impidiendo que Android Studio ejecute la aplicación correctamente. Los módulos `mobile` y `wear` comparten el mismo `namespace`, lo que puede causar confusiones al IDE durante el despliegue.

## User Review Required

> [!IMPORTANT]
> **Conflicto de Identificadores**: Ambos módulos (`mobile` y `wear`) tienen asignado el mismo espacio de nombres (`com.example.asesoriasutn`). Voy a diferenciar el módulo del reloj para que sea único.
>
> **Limpieza de Caché**: Después de aplicar estos cambios, es muy probable que necesites realizar un "Clean Project" y "Rebuild Project".

## Proposed Changes

### [Gradle Configuration]

#### [MODIFY] [wear/build.gradle.kts](file:///C:/Users/vanes/AndroidStudioProjects/AsesoriasUTN-MOVIL/wear/build.gradle.kts)
- Cambiar `namespace` a `com.example.asesoriasutn.wear`.
- Cambiar `applicationId` a `com.example.asesoriasutn.wear` para evitar conflictos de instalación en el mismo entorno de desarrollo.

### [UI Layer]

#### [MODIFY] [wear/src/main/java/com/example/asesoriasutn/presentation/MainActivity.kt](file:///C:/Users/vanes/AndroidStudioProjects/AsesoriasUTN-MOVIL/wear/src/main/java/com/example/asesoriasutn/presentation/MainActivity.kt)
- Actualizar los *imports* automáticos que utilicen el nuevo `namespace`.

#### [MODIFY] [mobile/src/main/res/layout/activity_perfil_docente.xml](file:///C:/Users/vanes/AndroidStudioProjects/AsesoriasUTN-MOVIL/mobile/src/main/res/layout/activity_perfil_docente.xml)
- Añadir el ID `android:id="@+id/main"` al contenedor raíz para evitar posibles fallos al aplicar insets de pantalla (bordes redondeados).

## Verification Plan

### Automated Tests
- Ejecutar `./gradlew clean :mobile:assembleDebug` para asegurar que el celular compila.
- Ejecutar `./gradlew :wear:assembleDebug` para asegurar que el reloj compila con su nuevo identificador.

### Manual Verification
1. Abrir Android Studio.
2. Ir a **Build > Clean Project**.
3. Seleccionar el módulo **mobile** en el menú desplegable de ejecución (cerca del botón Play).
4. Presionar el botón **Play (Run)**.
