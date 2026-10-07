package com.example.proyectointegrador_moviles_g2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.proyectointegrador_moviles_g2.ui.catalogo.CatalogoScreen
import com.example.proyectointegrador_moviles_g2.ui.theme.Proyectointegradormovilesg2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Proyectointegradormovilesg2Theme {
                CatalogoScreen()
            }
        }
    }
}
