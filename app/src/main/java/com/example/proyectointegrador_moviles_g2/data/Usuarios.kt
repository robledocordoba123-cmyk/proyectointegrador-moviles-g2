package com.example.proyectointegrador_moviles_g2.data

enum class Rol { ESTUDIANTE, PROFESOR }

// Quién inició sesión. El token se usa en cada petición a la API.
data class Sesion(val id: String, val nombre: String, val rol: Rol, val token: String)

// Usuarios de la academia de ejemplo "Ritmo Central" de la demo de RitmoApp.
const val CLAVE_DEMO = "Prueba123!"
const val CORREO_DEMO_ESTUDIANTE = "estudiante@ritmocentral.test"
const val CORREO_DEMO_PROFESOR = "profesor@ritmocentral.test"

data class Inscrito(val estudianteId: String, val nombre: String, val estadoAsistencia: String?)
