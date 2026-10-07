package com.example.proyectointegrador_moviles_g2

import com.example.proyectointegrador_moviles_g2.ui.catalogo.CatalogoViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogoViewModelTest {

    private fun cupos(viewModel: CatalogoViewModel, claseId: String) =
        viewModel.uiState.value.clases.first { it.id == claseId }.cuposDisponibles

    @Test
    fun reservar_descuentaUnCupoYMarcaLaClase() {
        val viewModel = CatalogoViewModel()
        val antes = cupos(viewModel, "1")

        viewModel.reservar("1")

        assertEquals(antes - 1, cupos(viewModel, "1"))
        assertTrue("1" in viewModel.uiState.value.reservadas)
    }

    @Test
    fun reservarDosVeces_soloDescuentaUnCupo() {
        val viewModel = CatalogoViewModel()
        val antes = cupos(viewModel, "1")

        viewModel.reservar("1")
        viewModel.reservar("1")

        assertEquals(antes - 1, cupos(viewModel, "1"))
    }

    @Test
    fun claseSinCupos_noSePuedeReservar() {
        val viewModel = CatalogoViewModel()

        viewModel.reservar("3") // Champeta no tiene cupos

        assertEquals(0, cupos(viewModel, "3"))
        assertFalse("3" in viewModel.uiState.value.reservadas)
    }

    @Test
    fun cancelar_devuelveElCupo() {
        val viewModel = CatalogoViewModel()
        val antes = cupos(viewModel, "2")

        viewModel.reservar("2")
        viewModel.cancelar("2")

        assertEquals(antes, cupos(viewModel, "2"))
        assertFalse("2" in viewModel.uiState.value.reservadas)
    }

    @Test
    fun cancelarSinHaberReservado_noCambiaNada() {
        val viewModel = CatalogoViewModel()
        val antes = cupos(viewModel, "4")

        viewModel.cancelar("4")

        assertEquals(antes, cupos(viewModel, "4"))
    }
}
