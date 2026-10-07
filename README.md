# proyectointegrador-moviles-g2

App Android de **RitmoApp** para estudiantes y profesores de academias de baile. Proyecto integrador de Aplicaciones Móviles Android, SENA ADSO, ficha 3229209, grupo 2.

Es la versión móvil de nuestro proyecto de grado [RitmoApp](https://github.com/robledocordoba123-cmyk/RitmoApp). La web ya tiene una API REST desplegada, y esta app la va a consumir para mostrar las clases, reservar cupos y tomar asistencia.

## Integrantes

- Manuela Córdoba Robledo
- Davier Andrés Quinto Bejarano

## Estado actual

- Catálogo de clases del estudiante con los cupos disponibles.
- Reservar y cancelar un cupo. El contador baja y sube, y si una clase se queda sin cupos el botón se desactiva.
- Arquitectura MVVM: `CatalogoViewModel` guarda el estado y las reglas de cupos, y `CatalogoScreen` solo dibuja. Las reservas no se pierden al girar el celular.
- Pruebas unitarias del ViewModel (`app/src/test`).
- Por ahora usa datos de prueba (`data/DatosDePrueba.kt`).

## Lo que sigue (según las sesiones del curso)

- [x] Separar la lógica en un ViewModel (MVVM)
- [ ] Guardar las reservas en el celular con Room
- [ ] Conectar con la API de RitmoApp usando Retrofit (`GET /api/clases`, `POST /api/reservas`)
- [ ] Inicio de sesión y navegación entre pantallas (estudiante y profesor)
- [ ] Pantalla del profesor para tomar asistencia

## Tecnologías

Kotlin, Jetpack Compose, Material 3. SDK mínimo: API 24 (Android 7.0).

## Cómo correrla

1. Abrir la carpeta en Android Studio.
2. Esperar a que termine el Gradle Sync.
3. Escoger un emulador o un celular conectado y darle Run (▶).

Pruebas: `./gradlew testDebugUnitTest`
