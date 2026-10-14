package com.example.proyectointegrador_moviles_g2

import com.example.proyectointegrador_moviles_g2.data.Clase
import com.example.proyectointegrador_moviles_g2.data.Inscrito
import com.example.proyectointegrador_moviles_g2.data.RitmoRepositorio
import com.example.proyectointegrador_moviles_g2.data.Rol
import com.example.proyectointegrador_moviles_g2.data.Sesion
import com.example.proyectointegrador_moviles_g2.data.SinConexionException
import com.example.proyectointegrador_moviles_g2.data.DatosGuardados
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

const val AHORA = 1_000_000L

// Imita a la API en memoria: descuenta cupos al reservar y los devuelve al cancelar.
class RepositorioFalso(clasesIniciales: List<Clase>) : RitmoRepositorio {
    val clases = clasesIniciales.toMutableList()
    val reservas = mutableMapOf<String, String>() // claseId -> reservaId
    var falla: String? = null
    // Si es true, la falla se comporta como si se hubiera caído el internet.
    var fallaDeRed = false
    var asistenciaEnviada: Map<String, String>? = null
    var inscritosPorClase: Map<String, List<Inscrito>> = emptyMap()

    private fun <T> resultado(valor: () -> T): Result<T> = when {
        falla == null -> Result.success(valor())
        fallaDeRed -> Result.failure(SinConexionException())
        else -> Result.failure(Exception(falla))
    }

    override suspend fun iniciarSesion(correo: String, clave: String) =
        resultado { Sesion("u1", "Estudiante Demo", Rol.ESTUDIANTE, "token") }

    override fun cerrarSesion() {}

    override suspend fun clases() = resultado { clases.toList() }

    override suspend fun misReservas() = resultado { reservas.toMap() }

    override suspend fun reservar(claseId: String) = resultado {
        val i = clases.indexOfFirst { it.id == claseId }
        clases[i] = clases[i].copy(cuposDisponibles = clases[i].cuposDisponibles - 1)
        reservas[claseId] = "r-$claseId"
    }

    override suspend fun cancelarReserva(reservaId: String) = resultado {
        val claseId = reservas.entries.first { it.value == reservaId }.key
        val i = clases.indexOfFirst { it.id == claseId }
        clases[i] = clases[i].copy(cuposDisponibles = clases[i].cuposDisponibles + 1)
        reservas.remove(claseId)
        Unit
    }

    override suspend fun datosGuardados() =
        DatosGuardados(clases.toList(), reservas.toMap(), AHORA)

    override suspend fun inscritos(claseId: String) = resultado { inscritosPorClase[claseId].orEmpty() }

    override suspend fun registrarAsistencia(claseId: String, estados: Map<String, String>) = resultado {
        asistenciaEnviada = estados
    }
}

fun clase(id: String, cupos: Int, inicio: Long = AHORA + 3_600_000, profesorId: String = "p1") =
    Clase(id, "Ritmo $id", "Salón 1", "Profe", "Lunes", 20, cupos, profesorId, inicio)

// Hace que el código de los ViewModels (viewModelScope) corra al instante en las pruebas.
@OptIn(ExperimentalCoroutinesApi::class)
class ReglaDispatcherPrincipal : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(UnconfinedTestDispatcher())
    override fun finished(description: Description) = Dispatchers.resetMain()
}
