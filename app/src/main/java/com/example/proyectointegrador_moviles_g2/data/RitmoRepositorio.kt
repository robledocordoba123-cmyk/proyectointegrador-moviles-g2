package com.example.proyectointegrador_moviles_g2.data

import com.example.proyectointegrador_moviles_g2.data.local.ReservaEntity
import com.example.proyectointegrador_moviles_g2.data.local.RitmoDao
import com.example.proyectointegrador_moviles_g2.data.local.RitmoDatabase
import com.example.proyectointegrador_moviles_g2.data.local.aClase
import com.example.proyectointegrador_moviles_g2.data.local.aEntidad
import com.example.proyectointegrador_moviles_g2.data.remote.ApiClient
import com.example.proyectointegrador_moviles_g2.data.remote.AsistenciaRequest
import com.example.proyectointegrador_moviles_g2.data.remote.ClaseDto
import com.example.proyectointegrador_moviles_g2.data.remote.ErrorDto
import com.example.proyectointegrador_moviles_g2.data.remote.LoginRequest
import com.example.proyectointegrador_moviles_g2.data.remote.RegistroAsistencia
import com.example.proyectointegrador_moviles_g2.data.remote.ReservarRequest
import com.example.proyectointegrador_moviles_g2.data.remote.RitmoApi
import android.content.Context
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// Lo que las pantallas necesitan de los datos. Los ViewModels dependen de esta
// interfaz y no de Retrofit, así en las pruebas se usa un repositorio falso.
interface RitmoRepositorio {
    suspend fun iniciarSesion(correo: String, clave: String): Result<Sesion>
    fun cerrarSesion()
    suspend fun clases(): Result<List<Clase>>

    // claseId -> reservaId de las reservas confirmadas del estudiante.
    suspend fun misReservas(): Result<Map<String, String>>
    suspend fun reservar(claseId: String): Result<Unit>
    suspend fun cancelarReserva(reservaId: String): Result<Unit>
    suspend fun inscritos(claseId: String): Result<List<Inscrito>>
    suspend fun registrarAsistencia(claseId: String, estados: Map<String, String>): Result<Unit>

    // Lo último que se guardó en el celular, para mostrarlo cuando no hay internet.
    suspend fun datosGuardados(): DatosGuardados
}

data class DatosGuardados(
    val clases: List<Clase>,
    val reservas: Map<String, String>,
    // Cuándo se guardaron (milisegundos), o null si todavía no hay nada guardado.
    val guardadoEn: Long?
)

// Falla de red: no se pudo hablar con el servidor. Los ViewModels la usan para pasar a modo sin internet.
class SinConexionException :
    Exception("No hay conexión con el servidor. Revisa el internet e intenta de nuevo.")

class RitmoRepositorioApi(
    private val dao: RitmoDao,
    private val api: RitmoApi = ApiClient.api,
    private val ahora: () -> Long = System::currentTimeMillis
) : RitmoRepositorio {

    // Usuario de la sesión actual: las copias guardadas se separan por usuario.
    private var usuarioId: String? = null

    override suspend fun iniciarSesion(correo: String, clave: String): Result<Sesion> = llamar {
        val respuesta = api.login(LoginRequest(correo.trim(), clave))
        val rol = when (respuesta.usuario.rol) {
            "ESTUDIANTE" -> Rol.ESTUDIANTE
            "PROFESOR" -> Rol.PROFESOR
            else -> throw RolNoSoportado()
        }
        ApiClient.token = respuesta.token
        usuarioId = respuesta.usuario.id
        Sesion(respuesta.usuario.id, respuesta.usuario.nombre, rol, respuesta.token)
    }

    override fun cerrarSesion() {
        ApiClient.token = null
        usuarioId = null
    }

    override suspend fun clases(): Result<List<Clase>> = llamar {
        val clases = api.clases().map { it.aClase() }
        usuarioId?.let { id -> dao.reemplazarClases(id, clases.map { it.aEntidad(id, ahora()) }) }
        clases
    }

    override suspend fun misReservas(): Result<Map<String, String>> = llamar {
        val reservas = api.misReservas()
            .filter { it.estado == "CONFIRMADA" }
            .associate { it.claseId to it.id }
        usuarioId?.let { id ->
            dao.reemplazarReservas(id, reservas.map { (claseId, reservaId) -> ReservaEntity(id, claseId, reservaId) })
        }
        reservas
    }

    override suspend fun datosGuardados(): DatosGuardados {
        val id = usuarioId ?: return DatosGuardados(emptyList(), emptyMap(), null)
        val clases = dao.clases(id)
        return DatosGuardados(
            clases = clases.map { it.aClase() },
            reservas = dao.reservas(id).associate { it.claseId to it.reservaId },
            guardadoEn = clases.maxOfOrNull { it.guardadaEn }
        )
    }

    override suspend fun reservar(claseId: String): Result<Unit> = llamar {
        api.reservar(ReservarRequest(claseId))
        Unit
    }

    override suspend fun cancelarReserva(reservaId: String): Result<Unit> = llamar {
        api.cancelarReserva(reservaId)
        Unit
    }

    override suspend fun inscritos(claseId: String): Result<List<Inscrito>> = llamar {
        api.inscritos(claseId).map { Inscrito(it.estudianteId, it.nombre, it.estadoAsistencia) }
    }

    override suspend fun registrarAsistencia(claseId: String, estados: Map<String, String>): Result<Unit> = llamar {
        api.registrarAsistencia(claseId, AsistenciaRequest(estados.map { (id, estado) -> RegistroAsistencia(id, estado) }))
    }

    // Ejecuta la petición y convierte cualquier falla en un mensaje que se le puede mostrar al usuario.
    private suspend fun <T> llamar(bloque: suspend () -> T): Result<T> = try {
        Result.success(bloque())
    } catch (e: HttpException) {
        val mensajeApi = e.response()?.errorBody()?.string()
            ?.let { runCatching { Gson().fromJson(it, ErrorDto::class.java).error }.getOrNull() }
        Result.failure(Exception(mensajeApi ?: "El servidor respondió con un error (${e.code()})."))
    } catch (e: IOException) {
        Result.failure(SinConexionException())
    } catch (e: RolNoSoportado) {
        Result.failure(Exception(e.message))
    }
}

// Convierte el JSON de la API en el modelo de la app, con la hora de Colombia.
private fun ClaseDto.aClase(): Clase {
    val inicio = FechasApi.leer(fechaHoraInicio)
    val fin = FechasApi.leer(fechaHoraFin)
    return Clase(
        id = id,
        ritmo = ritmo?.nombre ?: "Clase",
        salon = salon?.nombre ?: "Sin salón",
        profesor = profesor?.nombre ?: "Sin profesor",
        horario = FechasApi.horario(inicio, fin),
        cupoMaximo = cupoMaximo,
        cuposDisponibles = cuposDisponibles,
        profesorId = profesorId,
        inicioMillis = inicio
    )
}

object FechasApi {
    val zonaColombia: TimeZone = TimeZone.getTimeZone("America/Bogota")
    private val español = Locale("es", "CO")

    // La API manda las fechas en UTC con formato ISO, por ejemplo 2026-10-14T23:00:00.000Z.
    fun leer(iso: String): Long = runCatching {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .parse(iso)!!.time
    }.getOrDefault(0L)

    // "Martes 14 oct · 6:00 p. m. a 7:00 p. m."
    fun horario(inicio: Long, fin: Long): String {
        val dia = SimpleDateFormat("EEEE d MMM", español).apply { timeZone = zonaColombia }.format(inicio)
        val hora = SimpleDateFormat("h:mm a", español).apply { timeZone = zonaColombia }
        return "${dia.replaceFirstChar { it.uppercase() }} · ${hora.format(inicio)} a ${hora.format(fin)}"
    }

    // "13 oct, 3:45 p. m."
    fun fechaYHora(millis: Long): String =
        SimpleDateFormat("d MMM, h:mm a", español).apply { timeZone = zonaColombia }.format(millis)

    fun esHoy(millis: Long, ahora: Long = System.currentTimeMillis()): Boolean {
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = zonaColombia }
        return formato.format(millis) == formato.format(ahora)
    }
}

// Un solo repositorio para toda la app (en la sesión 6 se reemplaza por inyección con Hilt).
object ServiceLocator {
    lateinit var repositorio: RitmoRepositorio
        private set

    // Se llama una vez al abrir la app (RitmoApplication), porque la base de datos necesita el Context.
    fun iniciar(context: Context) {
        if (!::repositorio.isInitialized) {
            repositorio = RitmoRepositorioApi(RitmoDatabase.crear(context.applicationContext).dao())
        }
    }
}

private class RolNoSoportado : Exception("La app es para estudiantes y profesores. Los administradores usan la web.")
