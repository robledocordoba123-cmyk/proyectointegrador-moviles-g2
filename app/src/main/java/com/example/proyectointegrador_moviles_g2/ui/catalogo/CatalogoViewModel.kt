package com.example.proyectointegrador_moviles_g2.ui.catalogo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectointegrador_moviles_g2.data.Clase
import com.example.proyectointegrador_moviles_g2.data.RitmoRepositorio
import com.example.proyectointegrador_moviles_g2.data.ServiceLocator
import com.example.proyectointegrador_moviles_g2.data.SinConexionException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Todo lo que la pantalla del catálogo necesita para dibujarse.
data class CatalogoUiState(
    val clases: List<Clase> = emptyList(),
    // claseId -> reservaId de las clases que el estudiante ya reservó.
    val reservas: Map<String, String> = emptyMap(),
    val cargando: Boolean = false,
    // Clase en la que se está reservando o cancelando, para bloquear su botón mientras responde la API.
    val procesando: String? = null,
    val mensaje: String? = null,
    // Sin internet se muestran las clases guardadas en el celular y no se puede reservar.
    val sinConexion: Boolean = false,
    val guardadoEn: Long? = null
) {
    val reservadas: Set<String> get() = reservas.keys
}

// El ViewModel trae las clases de la API y aplica la reserva y la cancelación (RF-06).
// Las reglas de cupos las valida el servidor; aquí se muestra su respuesta.
class CatalogoViewModel(
    private val repositorio: RitmoRepositorio = ServiceLocator.repositorio,
    private val ahora: () -> Long = System::currentTimeMillis
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogoUiState())
    val uiState: StateFlow<CatalogoUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        _uiState.update { it.copy(cargando = true) }
        viewModelScope.launch {
            val clases = repositorio.clases()
            val reservas = repositorio.misReservas()
            val error = clases.exceptionOrNull() ?: reservas.exceptionOrNull()
            when {
                error is SinConexionException -> mostrarGuardado()
                error != null -> _uiState.update { it.copy(cargando = false, mensaje = error.message) }
                else -> _uiState.update {
                    it.copy(
                        clases = clases.getOrThrow().proximas(),
                        reservas = reservas.getOrThrow(),
                        cargando = false,
                        sinConexion = false,
                        guardadoEn = null
                    )
                }
            }
        }
    }

    // Modo sin internet: lo último que se guardó en el celular (Room).
    private suspend fun mostrarGuardado() {
        val guardado = repositorio.datosGuardados()
        _uiState.update {
            it.copy(
                clases = guardado.clases.proximas(),
                reservas = guardado.reservas,
                cargando = false,
                sinConexion = true,
                guardadoEn = guardado.guardadoEn
            )
        }
    }

    // El estudiante solo ve las clases que todavía no empiezan.
    private fun List<Clase>.proximas() = filter { it.inicioMillis > ahora() }

    fun reservar(claseId: String) = ejecutar(claseId, "¡Listo! Tu cupo quedó reservado.") {
        repositorio.reservar(claseId)
    }

    fun cancelar(claseId: String) {
        val reservaId = _uiState.value.reservas[claseId] ?: return
        ejecutar(claseId, "Reserva cancelada. El cupo quedó libre.") {
            repositorio.cancelarReserva(reservaId)
        }
    }

    fun mensajeMostrado() {
        _uiState.update { it.copy(mensaje = null) }
    }

    // Llama a la API, muestra el resultado y vuelve a cargar para ver los cupos reales.
    private fun ejecutar(claseId: String, exito: String, accion: suspend () -> Result<Unit>) {
        if (_uiState.value.procesando != null) return
        _uiState.update { it.copy(procesando = claseId) }
        viewModelScope.launch {
            val resultado = accion()
            _uiState.update {
                it.copy(procesando = null, mensaje = resultado.exceptionOrNull()?.message ?: exito)
            }
            cargar()
        }
    }
}
