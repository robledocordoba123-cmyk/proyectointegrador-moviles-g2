package com.example.proyectointegrador_moviles_g2.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// API de RitmoApp desplegada en Render (la misma que usa la web).
const val URL_API = "https://ritmoapp-api.onrender.com/api/"

object ApiClient {

    // Token JWT de la sesión actual; se llena al iniciar sesión y se borra al salir.
    @Volatile
    var token: String? = null

    private val http = OkHttpClient.Builder()
        .addInterceptor { cadena ->
            val peticion = cadena.request()
            val conToken = token?.let { peticion.newBuilder().header("Authorization", "Bearer $it").build() }
            cadena.proceed(conToken ?: peticion)
        }
        // La API está en un plan gratuito que se duerme: la primera petición puede tardar cerca de un minuto.
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .callTimeout(90, TimeUnit.SECONDS)
        .build()

    val api: RitmoApi = Retrofit.Builder()
        .baseUrl(URL_API)
        .client(http)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(RitmoApi::class.java)
}
