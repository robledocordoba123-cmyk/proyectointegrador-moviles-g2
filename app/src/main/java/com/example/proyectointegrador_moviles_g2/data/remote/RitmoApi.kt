package com.example.proyectointegrador_moviles_g2.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

// Endpoints de la API REST de RitmoApp que usa la app móvil.
// Todas las rutas van bajo /api; salvo el login, llevan el token JWT (lo agrega ApiClient).
interface RitmoApi {

    @POST("auth/login")
    suspend fun login(@Body cuerpo: LoginRequest): LoginResponse

    @GET("clases")
    suspend fun clases(): List<ClaseDto>

    @GET("reservas/mias")
    suspend fun misReservas(): List<ReservaDto>

    @POST("reservas")
    suspend fun reservar(@Body cuerpo: ReservarRequest): ReservaDto

    @PATCH("reservas/{id}/cancelar")
    suspend fun cancelarReserva(@Path("id") reservaId: String): MensajeDto

    @GET("clases/{id}/inscritos")
    suspend fun inscritos(@Path("id") claseId: String): List<InscritoDto>

    @POST("clases/{id}/asistencia")
    suspend fun registrarAsistencia(@Path("id") claseId: String, @Body cuerpo: AsistenciaRequest)
}

// DTOs: la forma exacta del JSON que envía y recibe la API.

data class LoginRequest(val email: String, val password: String)

data class LoginResponse(val token: String, val usuario: UsuarioDto)

data class UsuarioDto(val id: String, val nombre: String, val rol: String)

data class NombreDto(val nombre: String?)

data class ClaseDto(
    val id: String,
    val profesorId: String,
    val cupoMaximo: Int,
    val cuposDisponibles: Int,
    val fechaHoraInicio: String,
    val fechaHoraFin: String,
    val ritmo: NombreDto?,
    val salon: NombreDto?,
    val profesor: NombreDto?
)

data class ReservaDto(val id: String, val claseId: String, val estado: String)

data class ReservarRequest(val claseId: String)

data class MensajeDto(val mensaje: String?)

data class InscritoDto(val estudianteId: String, val nombre: String, val estadoAsistencia: String?)

data class AsistenciaRequest(val asistencias: List<RegistroAsistencia>)

data class RegistroAsistencia(val estudianteId: String, val estado: String)

data class ErrorDto(val error: String?)
