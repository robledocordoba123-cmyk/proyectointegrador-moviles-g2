package com.example.proyectointegrador_moviles_g2.data

enum class Rol { ESTUDIANTE, PROFESOR }

data class Usuario(val nombre: String, val correo: String, val rol: Rol)

// Usuarios de prueba: los mismos correos de la academia de ejemplo de RitmoApp.
// Cuando la app se conecte a la API, el login se hará con POST /api/auth/login.
const val CLAVE_DE_PRUEBA = "Prueba123!"

val usuariosDePrueba = listOf(
    Usuario("Sofía Martínez", "estudiante@ritmocentral.test", Rol.ESTUDIANTE),
    Usuario("Laura Gómez", "profesor@ritmocentral.test", Rol.PROFESOR)
)

// Estudiantes inscritos en las clases de la profesora (para tomar asistencia).
val inscritosDePrueba = listOf(
    "Sofía Martínez",
    "Juan Pablo Rivas",
    "Valentina Córdoba",
    "Andrés Palacios",
    "Mariana Lozano"
)

// Devuelve el usuario si el correo y la clave coinciden; si no, null.
fun validarCredenciales(correo: String, clave: String): Usuario? =
    usuariosDePrueba.find { it.correo.equals(correo.trim(), ignoreCase = true) }
        ?.takeIf { clave == CLAVE_DE_PRUEBA }
