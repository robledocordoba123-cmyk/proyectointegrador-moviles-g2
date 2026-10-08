package com.example.proyectointegrador_moviles_g2.ui.catalogo

import androidx.lifecycle.ViewModel
import com.example.proyectointegrador_moviles_g2.data.Clase
import com.example.proyectointegrador_moviles_g2.data.clasesDePrueba
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Todo lo que la pantalla del catálogo necesita para dibujarse.
data class CatalogoUiState(
    val clases: List<Clase> = emptyList(),
    val reservadas: Set<String> = emptySet()
)

// El ViewModel guarda el estado y las reglas del catálogo (RF-06).
// La pantalla solo lo observa y le avisa cuando el estudiante toca un botón.
// Como el ViewModel sobrevive a la rotación del celular, las reservas no se pierden.
class CatalogoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogoUiState(clases = clasesDePrueba))
    val uiState: StateFlow<CatalogoUiState> = _uiState.asStateFlow()

    fun reservar(claseId: String) {
        _uiState.update { estado ->
            val clase = estado.clases.find { it.id == claseId }
            // RN-01: no se reserva si no quedan cupos o si ya la tiene reservada.
            if (clase == null || clase.cuposDisponibles == 0 || claseId in estado.reservadas) {
                estado
            } else {
                estado.copy(
                    clases = estado.clases.cambiarCupos(claseId, -1),
                    reservadas = estado.reservadas + claseId
                )
            }
        }
    }

    fun cancelar(claseId: String) {
        _uiState.update { estado ->
            if (claseId !in estado.reservadas) {
                estado
            } else {
                // Al cancelar, el cupo queda libre para otro estudiante.
                estado.copy(
                    clases = estado.clases.cambiarCupos(claseId, +1),
                    reservadas = estado.reservadas - claseId
                )
            }
        }
    }

    private fun List<Clase>.cambiarCupos(claseId: String, cambio: Int) = map {
        if (it.id == claseId) it.copy(cuposDisponibles = it.cuposDisponibles + cambio) else it
    }
}
