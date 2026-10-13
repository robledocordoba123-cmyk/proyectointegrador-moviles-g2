package com.example.proyectointegrador_moviles_g2.data

// Una clase de baile tal como la usan las pantallas.
// Se arma a partir del JSON de GET /api/clases (ver RitmoRepositorio).
data class Clase(
    val id: String,
    val ritmo: String,
    val salon: String,
    val profesor: String,
    val horario: String,
    val cupoMaximo: Int,
    val cuposDisponibles: Int,
    val profesorId: String = "",
    // Inicio de la clase en milisegundos, para ordenar y saber si ya pasó o si es hoy.
    val inicioMillis: Long = 0L
)
