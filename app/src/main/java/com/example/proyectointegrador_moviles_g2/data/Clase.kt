package com.example.proyectointegrador_moviles_g2.data

// Una clase de baile tal como la muestra el catálogo del estudiante.
// Los campos siguen el modelo Clase de la API de RitmoApp (GET /api/clases),
// para que después solo haya que cambiar de dónde salen los datos.
data class Clase(
    val id: String,
    val ritmo: String,
    val salon: String,
    val profesor: String,
    val horario: String,
    val cupoMaximo: Int,
    val cuposDisponibles: Int
)
