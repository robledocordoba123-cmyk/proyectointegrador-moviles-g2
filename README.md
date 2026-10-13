# proyectointegrador-moviles-g2

App Android de **RitmoApp** para estudiantes y profesores de academias de baile. Proyecto integrador de Aplicaciones Móviles Android, SENA ADSO, ficha 3229209, grupo 2.

Es la versión móvil de nuestro proyecto de grado [RitmoApp](https://github.com/robledocordoba123-cmyk/RitmoApp). La web ya tiene una API REST desplegada, y esta app la va a consumir para mostrar las clases, reservar cupos y tomar asistencia.

## Integrantes

- Manuela Córdoba Robledo
- Davier Andrés Quinto Bejarano

## Estado actual

- **Conectada a la API real de RitmoApp** (la misma de la web, desplegada en Render) con Retrofit. Lo que se reserva en la app aparece en la web y al revés.
- Inicio de sesión real (`POST /api/auth/login`) con los usuarios de la academia de ejemplo, y acceso rápido como Estudiante o Profesor.
- **Estudiante**: ve las clases que todavía no empiezan con sus cupos reales, reserva y cancela. Los mensajes de la API (por ejemplo, sin cupos o membresía vencida) se muestran en pantalla.
- **Profesor**: ve sus clases desde hoy, abre una y toma asistencia (Asistió, Falta, Excusa) de los estudiantes que reservaron. La API solo deja registrarla el día de la clase.
- Arquitectura MVVM con repositorio: las pantallas no conocen Retrofit, y las pruebas usan un repositorio falso.
- Pruebas unitarias de los ViewModels (`app/src/test`).

## Lo que sigue (según las sesiones del curso)

- [x] Separar la lógica en un ViewModel (MVVM)
- [ ] Guardar las clases en el celular con Room para verlas sin internet
- [ ] Inyección de dependencias con Hilt (reemplaza el ServiceLocator)
- [ ] Navegación con Navigation Compose
- [x] Conectar con la API de RitmoApp usando Retrofit
- [x] Inicio de sesión y pantallas por rol (estudiante y profesor)
- [x] Pantalla del profesor para tomar asistencia

## Tecnologías

Kotlin, Jetpack Compose, Material 3. SDK mínimo: API 24 (Android 7.0).

## Usuarios de prueba

Contraseña para todos: `Prueba123!`. Estudiante: `estudiante@ritmocentral.test`. Profesor: `profesor@ritmocentral.test`. La API está en un plan gratuito que se apaga sin uso, así que el primer ingreso puede tardar cerca de un minuto.

## Cómo correrla

1. Abrir la carpeta en Android Studio.
2. Esperar a que termine el Gradle Sync.
3. Escoger un emulador o un celular conectado y darle Run (▶).

Pruebas: `./gradlew testDebugUnitTest`
