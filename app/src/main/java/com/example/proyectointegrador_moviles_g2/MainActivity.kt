package com.example.proyectointegrador_moviles_g2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.proyectointegrador_moviles_g2.data.Rol
import com.example.proyectointegrador_moviles_g2.data.usuariosDePrueba
import com.example.proyectointegrador_moviles_g2.ui.asistencia.AsistenciaScreen
import com.example.proyectointegrador_moviles_g2.ui.catalogo.CatalogoScreen
import com.example.proyectointegrador_moviles_g2.ui.login.LoginScreen
import com.example.proyectointegrador_moviles_g2.ui.theme.Proyectointegradormovilesg2Theme

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

// Decide qué pantalla mostrar según quién inició sesión.
@Composable
fun RitmoApp() {
    // Se guarda solo el correo para que la sesión sobreviva al girar el celular.
    var correoSesion by rememberSaveable { mutableStateOf<String?>(null) }
    val usuario = usuariosDePrueba.find { it.correo == correoSesion }

    when (usuario?.rol) {
        null -> LoginScreen(onIngresar = { correoSesion = it.correo })
        Rol.ESTUDIANTE -> CatalogoScreen(onCerrarSesion = { correoSesion = null })
        Rol.PROFESOR -> AsistenciaScreen(
            profesor = usuario.nombre,
            onCerrarSesion = { correoSesion = null }
        )
    }
}
