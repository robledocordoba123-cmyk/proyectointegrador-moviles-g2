package com.example.proyectointegrador_moviles_g2

import com.example.proyectointegrador_moviles_g2.data.Inscrito
import com.example.proyectointegrador_moviles_g2.ui.asistencia.AsistenciaViewModel
import com.example.proyectointegrador_moviles_g2.ui.asistencia.EstadoAsistencia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AsistenciaViewModelTest {

    @get:Rule
    val dispatcher = ReglaDispatcherPrincipal()

    private val repo = RepositorioFalso(
        listOf(clase("propia", 5, profesorId = "p1"), clase("ajena", 5, profesorId = "p2"))
    ).apply {
        inscritosPorClase = mapOf(
            "propia" to listOf(Inscrito("e1", "Valentina", null), Inscrito("e2", "Santiago", "EXCUSA"))
        )
    }

    private fun crear() = AsistenciaViewModel(repo, ahora = { AHORA })

    @Test
    fun elProfesor_soloVeSusClases() {
        val viewModel = crear()

        viewModel.cargarClases("p1")

        assertEquals(listOf("propia"), viewModel.uiState.value.clases.map { it.id })
    }

    @Test
    fun alAbrirUnaClase_muestraLasMarcasQueYaEstabanGuardadas() {
        val viewModel = crear()
        viewModel.cargarClases("p1")

        viewModel.seleccionarClase(viewModel.uiState.value.clases.first())

        assertEquals(2, viewModel.uiState.value.inscritos.size)
        assertEquals(EstadoAsistencia.EXCUSA, viewModel.uiState.value.marcas["e2"])
        assertNull(viewModel.uiState.value.marcas["e1"])
    }

    @Test
    fun noGuarda_siFaltaAlguienPorMarcar() {
        val viewModel = crear()
        viewModel.cargarClases("p1")
        viewModel.seleccionarClase(viewModel.uiState.value.clases.first())

        viewModel.guardar()

        assertNull(repo.asistenciaEnviada)
        assertFalse(viewModel.uiState.value.guardada)
    }

    @Test
    fun guardar_enviaLosEstadosConLosNombresDeLaApi() {
        val viewModel = crear()
        viewModel.cargarClases("p1")
        viewModel.seleccionarClase(viewModel.uiState.value.clases.first())

        viewModel.marcar("e1", EstadoAsistencia.FALTA)
        viewModel.guardar()

        assertEquals(mapOf("e1" to "INASISTENCIA", "e2" to "EXCUSA"), repo.asistenciaEnviada)
        assertTrue(viewModel.uiState.value.guardada)
    }
}
