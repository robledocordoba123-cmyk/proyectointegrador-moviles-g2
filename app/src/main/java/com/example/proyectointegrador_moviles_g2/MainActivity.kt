package com.example.proyectointegrador_moviles_g2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectointegrador_moviles_g2.data.Rol
import com.example.proyectointegrador_moviles_g2.data.ServiceLocator
import com.example.proyectointegrador_moviles_g2.data.Sesion
import com.example.proyectointegrador_moviles_g2.ui.asistencia.AsistenciaScreen
import com.example.proyectointegrador_moviles_g2.ui.asistencia.AsistenciaViewModel
import com.example.proyectointegrador_moviles_g2.ui.catalogo.CatalogoScreen
import com.example.proyectointegrador_moviles_g2.ui.catalogo.CatalogoViewModel
import com.example.proyectointegrador_moviles_g2.ui.login.LoginScreen
import com.example.proyectointegrador_moviles_g2.ui.theme.Proyectointegradormovilesg2Theme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Proyectointegradormovilesg2Theme {
                RitmoApp()
            }
        }
    }
}

// Guarda quién inició sesión. Vive en un ViewModel para no perder la sesión al girar el celular.
class SesionViewModel : ViewModel() {
    private val _sesion = MutableStateFlow<Sesion?>(null)
    val sesion: StateFlow<Sesion?> = _sesion.asStateFlow()

    fun iniciar(sesion: Sesion) {
        _sesion.value = sesion
    }

    fun cerrar() {
        ServiceLocator.repositorio.cerrarSesion()
        _sesion.value = null
    }
}

// Decide qué pantalla mostrar según quién inició sesión.
@Composable
fun RitmoApp(sesionViewModel: SesionViewModel = viewModel()) {
    val sesion by sesionViewModel.sesion.collectAsState()
    val actual = sesion

    when (actual?.rol) {
        null -> LoginScreen(onIngresar = sesionViewModel::iniciar)
        Rol.ESTUDIANTE -> CatalogoScreen(
            nombreEstudiante = actual.nombre,
            // Un ViewModel por sesión: si entra otro usuario, no ve los datos del anterior.
            viewModel = viewModel<CatalogoViewModel>(key = "catalogo-${actual.token}"),
            onCerrarSesion = sesionViewModel::cerrar
        )
        Rol.PROFESOR -> AsistenciaScreen(
            profesorId = actual.id,
            nombreProfesor = actual.nombre,
            viewModel = viewModel<AsistenciaViewModel>(key = "asistencia-${actual.token}"),
            onCerrarSesion = sesionViewModel::cerrar
        )
    }
}
