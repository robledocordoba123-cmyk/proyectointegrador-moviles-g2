package com.example.proyectointegrador_moviles_g2.ui.asistencia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectointegrador_moviles_g2.data.Clase
import com.example.proyectointegrador_moviles_g2.data.FechasApi
import com.example.proyectointegrador_moviles_g2.data.Inscrito
import com.example.proyectointegrador_moviles_g2.data.RitmoRepositorio
import com.example.proyectointegrador_moviles_g2.data.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Los tres estados que acepta la API al tomar asistencia (RF-07).
enum class EstadoAsistencia(val texto: String, val valorApi: String) {
    ASISTIO("Asistió", "ASISTIO"),
    FALTA("Falta", "INASISTENCIA"),
    EXCUSA("Excusa", "EXCUSA");

    companion object {
        fun desdeApi(valor: String?): EstadoAsistencia? = entries.find { it.valorApi == valor }
    }
}

data class AsistenciaUiState(
    val clases: List<Clase> = emptyList(),
    val claseSeleccionada: Clase? = null,
    val inscritos: List<Inscrito> = emptyList(),
    // estudianteId -> estado marcado. Si no está en el mapa, todavía no se ha marcado.
    val marcas: Map<String, EstadoAsistencia> = emptyMap(),
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val guardada: Boolean = false,
    val mensaje: String? = null
) {
    val todosMarcados: Boolean get() = inscritos.isNotEmpty() && inscritos.all { it.estudianteId in marcas }
}

class AsistenciaViewModel(
    private val repositorio: RitmoRepositorio = ServiceLocator.repositorio,
    private val ahora: () -> Long = System::currentTimeMillis
) : ViewModel() {

    private val _uiState = MutableStateFlow(AsistenciaUiState())
    val uiState: StateFlow<AsistenciaUiState> = _uiState.asStateFlow()

    // El profesor ve sus clases de hoy en adelante.
    fun cargarClases(profesorId: String) {
        _uiState.update { it.copy(cargando = true) }
        viewModelScope.launch {
            repositorio.clases()
                .onSuccess { clases ->
                    val propias = clases.filter {
                        it.profesorId == profesorId && (it.inicioMillis > ahora() || FechasApi.esHoy(it.inicioMillis, ahora()))
                    }
                    _uiState.update { it.copy(clases = propias, cargando = false) }
                }
                .onFailure { error -> _uiState.update { it.copy(cargando = false, mensaje = error.message) } }
        }
    }

    fun seleccionarClase(clase: Clase) {
        _uiState.update {
            it.copy(claseSeleccionada = clase, inscritos = emptyList(), marcas = emptyMap(), guardada = false, cargando = true)
        }
        viewModelScope.launch {
            repositorio.inscritos(clase.id)
                .onSuccess { inscritos ->
                    // Si la asistencia ya se había tomado, se muestran las marcas guardadas.
                    val guardadas = inscritos.mapNotNull { i ->
                        EstadoAsistencia.desdeApi(i.estadoAsistencia)?.let { i.estudianteId to it }
                    }.toMap()
                    _uiState.update { it.copy(inscritos = inscritos, marcas = guardadas, cargando = false) }
                }
                .onFailure { error -> _uiState.update { it.copy(cargando = false, mensaje = error.message) } }
        }
    }

    fun marcar(estudianteId: String, estado: EstadoAsistencia) {
        _uiState.update { it.copy(marcas = it.marcas + (estudianteId to estado), guardada = false) }
    }

    fun guardar() {
        val estado = _uiState.value
        val clase = estado.claseSeleccionada ?: return
        // Solo se guarda cuando todos los estudiantes tienen un estado.
        if (!estado.todosMarcados || estado.guardando) return

        _uiState.update { it.copy(guardando = true) }
        viewModelScope.launch {
            val estados = estado.marcas.mapValues { it.value.valorApi }
            repositorio.registrarAsistencia(clase.id, estados)
                .onSuccess { _uiState.update { it.copy(guardando = false, guardada = true, mensaje = "Asistencia guardada.") } }
                .onFailure { error -> _uiState.update { it.copy(guardando = false, mensaje = error.message) } }
        }
    }

    fun mensajeMostrado() {
        _uiState.update { it.copy(mensaje = null) }
    }

    fun volverALasClases() {
        _uiState.update { it.copy(claseSeleccionada = null, inscritos = emptyList(), marcas = emptyMap(), guardada = false) }
    }
}
