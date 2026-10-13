package com.example.proyectointegrador_moviles_g2

import com.example.proyectointegrador_moviles_g2.ui.catalogo.CatalogoViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CatalogoViewModelTest {

    @get:Rule
    val dispatcher = ReglaDispatcherPrincipal()

    private fun crear(repo: RepositorioFalso) = CatalogoViewModel(repo, ahora = { AHORA })

    private fun cupos(viewModel: CatalogoViewModel, claseId: String) =
        viewModel.uiState.value.clases.first { it.id == claseId }.cuposDisponibles

    @Test
    fun alAbrir_cargaSoloLasClasesQueNoHanEmpezado() {
        val repo = RepositorioFalso(listOf(clase("1", 5), clase("pasada", 5, inicio = AHORA - 1)))
        val viewModel = crear(repo)

        assertEquals(listOf("1"), viewModel.uiState.value.clases.map { it.id })
        assertFalse(viewModel.uiState.value.cargando)
    }

    @Test
    fun reservar_descuentaUnCupoYMarcaLaClase() {
        val repo = RepositorioFalso(listOf(clase("1", 5)))
        val viewModel = crear(repo)

        viewModel.reservar("1")

        assertEquals(4, cupos(viewModel, "1"))
        assertTrue("1" in viewModel.uiState.value.reservadas)
        assertEquals("¡Listo! Tu cupo quedó reservado.", viewModel.uiState.value.mensaje)
    }

    @Test
    fun cancelar_devuelveElCupo() {
        val repo = RepositorioFalso(listOf(clase("1", 5)))
        val viewModel = crear(repo)

        viewModel.reservar("1")
        viewModel.cancelar("1")

        assertEquals(5, cupos(viewModel, "1"))
        assertFalse("1" in viewModel.uiState.value.reservadas)
    }

    @Test
    fun cancelarSinHaberReservado_noLlamaALaApi() {
        val repo = RepositorioFalso(listOf(clase("1", 5)))
        val viewModel = crear(repo)

        viewModel.cancelar("1")

        assertEquals(5, cupos(viewModel, "1"))
    }

    @Test
    fun siLaApiFalla_muestraSuMensaje() {
        val repo = RepositorioFalso(listOf(clase("1", 0)))
        val viewModel = crear(repo)
        repo.falla = "No quedan cupos disponibles para esta clase."

        viewModel.reservar("1")

        assertEquals("No quedan cupos disponibles para esta clase.", viewModel.uiState.value.mensaje)
        assertFalse("1" in viewModel.uiState.value.reservadas)
    }
}
