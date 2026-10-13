package com.example.proyectointegrador_moviles_g2.ui.asistencia

import androidx.lifecycle.ViewModel
import com.example.proyectointegrador_moviles_g2.data.Clase
import com.example.proyectointegrador_moviles_g2.data.clasesDePrueba
import com.example.proyectointegrador_moviles_g2.data.inscritosDePrueba
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Los tres estados que permite RitmoApp al tomar asistencia (RF-07).
enum class EstadoAsistencia(val texto: String) {
    ASISTIO("Asistió"),
    FALTA("Falta"),
    EXCUSA("Excusa")
}

data class AsistenciaUiState(
    val clases: List<Clase> = emptyList(),
    val claseSeleccionada: Clase? = null,
    val inscritos: List<String> = emptyList(),
    // Estudiante -> estado marcado. Si no está en el mapa, todavía no se ha marcado.
    val marcas: Map<String, EstadoAsistencia> = emptyMap(),
    val guardada: Boolean = false
) {
    val todosMarcados: Boolean get() = inscritos.isNotEmpty() && inscritos.all { it in marcas }
}

class AsistenciaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AsistenciaUiState())
    val uiState: StateFlow<AsistenciaUiState> = _uiState.asStateFlow()

    // El profesor solo ve las clases que tiene asignadas.
    fun cargarClases(profesor: String) {
        _uiState.update { it.copy(clases = clasesDePrueba.filter { clase -> clase.profesor == profesor }) }
    }

    fun seleccionarClase(clase: Clase) {
        _uiState.update {
            it.copy(claseSeleccionada = clase, inscritos = inscritosDePrueba, marcas = emptyMap(), guardada = false)
        }
    }

    fun marcar(estudiante: String, estado: EstadoAsistencia) {
        _uiState.update { it.copy(marcas = it.marcas + (estudiante to estado), guardada = false) }
    }

    fun guardar() {
        // Solo se guarda cuando todos los estudiantes tienen un estado.
        _uiState.update { if (it.todosMarcados) it.copy(guardada = true) else it }
    }

    fun volverALasClases() {
        _uiState.update { it.copy(claseSeleccionada = null, marcas = emptyMap(), guardada = false) }
    }
}
