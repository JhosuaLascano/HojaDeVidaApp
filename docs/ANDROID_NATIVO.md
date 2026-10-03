# App móvil: Mi Hoja de Vida (Android + Docker)

Aplicación Android nativa (Kotlin + Jetpack Compose) que muestra una hoja de vida personal:
perfil, contacto (correo, teléfono, LinkedIn y GitHub se pueden tocar), experiencia, educación,
habilidades, idiomas y referencias.

## Estructura

```
├── Dockerfile               # "Máquina virtual": JDK 17 + Android SDK 34 + Gradle 8.7
├── docker-compose.yml       # Compila el APK y lo deja en ./output
├── app/src/main/java/com/hojadevida/app/
│   ├── CvData.kt            # ← TUS DATOS PERSONALES (edita este archivo)
│   └── MainActivity.kt      # Interfaz de la app
└── output/HojaDeVida.apk    # APK generado
```

## 1. Personalizar la hoja de vida

Abre `app/src/main/java/com/hojadevida/app/CvData.kt` y reemplaza los datos de ejemplo.

## 2. Compilar con Docker

Requiere Docker Desktop en ejecución.

```bash
docker compose up --build
```

El APK queda en `output/HojaDeVida.apk`. Cópialo a tu celular e instálalo
(activa "Instalar apps de origen desconocido").

## 3. Abrir y ejecutar en Android Studio

1. Android Studio → **File › Open** → selecciona esta carpeta.
2. Espera a que termine el *Gradle Sync* (descarga Gradle 8.7 automáticamente).
3. Crea un emulador en **Device Manager** (ej. Pixel, API 34) o conecta tu celular.
4. Presiona **Run ▶**.

También puedes ver la interfaz sin emulador abriendo `MainActivity.kt` y usando la vista **Split/Design** (función `VistaPrevia`).

## Instalar el APK del Docker en el emulador de Android Studio

Con el emulador abierto, arrastra `output/HojaDeVida.apk` sobre la ventana del emulador, o:

```bash
adb install output/HojaDeVida.apk
```

## Corregir o agregar información

1. Edita `app/src/main/java/com/hojadevida/app/CvData.kt`.
2. Vuelve a compilar: `docker compose up --build`.
3. Instala el nuevo `output/HojaDeVida.apk` encima del anterior (se actualiza sin desinstalar).
