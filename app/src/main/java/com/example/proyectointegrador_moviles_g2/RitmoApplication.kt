package com.example.proyectointegrador_moviles_g2

import android.app.Application
import com.example.proyectointegrador_moviles_g2.data.ServiceLocator

class RitmoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.iniciar(this)
    }
}
