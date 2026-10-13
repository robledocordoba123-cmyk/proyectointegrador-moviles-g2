package com.example.proyectointegrador_moviles_g2.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectointegrador_moviles_g2.data.RitmoRepositorio
import com.example.proyectointegrador_moviles_g2.data.ServiceLocator
import com.example.proyectointegrador_moviles_g2.data.Sesion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val cargando: Boolean = false,
    val error: String? = null
)

// Inicio de sesión real contra POST /api/auth/login (RF-02).
class LoginViewModel(
    private val repositorio: RitmoRepositorio = ServiceLocator.repositorio
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun ingresar(correo: String, clave: String, alIngresar: (Sesion) -> Unit) {
        if (correo.isBlank() || clave.isBlank()) {
            _uiState.update { it.copy(error = "Escribe tu correo y tu contraseña") }
            return
        }
        if (_uiState.value.cargando) return

        _uiState.update { LoginUiState(cargando = true) }
        viewModelScope.launch {
            repositorio.iniciarSesion(correo, clave)
                .onSuccess { sesion ->
                    _uiState.update { LoginUiState() }
                    alIngresar(sesion)
                }
                .onFailure { error ->
                    _uiState.update { LoginUiState(error = error.message ?: "No se pudo iniciar sesión") }
                }
        }
    }
}
