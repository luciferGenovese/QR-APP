# QR OFICIAL

Aplicación Android para escanear y crear códigos QR y códigos de barras.

## Compilar y ejecutar las pruebas

Instala un JDK 21, Android SDK Platform 36 y Android SDK Build-Tools 36.0.0. Las pruebas de Robolectric que ejecutan Android 36 requieren Java 21. Configura `JAVA_HOME` con la ruta del JDK y `ANDROID_HOME` con la ruta del SDK. Si abres el proyecto en Android Studio, el IDE también puede registrar la ruta del SDK en `local.properties`.

Desde la raíz del repositorio, en Windows:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```

En macOS o Linux:

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

El Wrapper descarga Gradle 9.3.1 en la primera ejecución. Android Gradle Plugin crea y utiliza la clave de depuración estándar. El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

## Verificar los recorridos principales

Conecta un teléfono Android con depuración USB habilitada e instala el APK con `adb install -r app/build/outputs/apk/debug/app-debug.apk`.

1. **Escanear y encontrar en el historial.** Abre el escáner, concede el permiso de cámara y escanea un QR de texto conocido. Guarda el resultado, abre el historial y verifica que aparezca como escaneado con el texto esperado. Cierra y vuelve a abrir la app para comprobar que el registro persiste.
2. **Crear, exportar y volver a escanear.** Crea un QR de texto con un contenido distinto. Genera el código y guárdalo como PNG. Abre el escáner, selecciona el PNG desde el selector de imágenes y comprueba que el resultado coincida exactamente con el texto original.
