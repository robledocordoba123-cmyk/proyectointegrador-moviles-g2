package com.example.proyectointegrador_moviles_g2.ui.asistencia

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectointegrador_moviles_g2.data.Clase
import com.example.proyectointegrador_moviles_g2.data.FechasApi

// Pantalla del profesor: ve sus clases y toma asistencia (RF-07).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsistenciaScreen(
    profesorId: String,
    nombreProfesor: String,
    onCerrarSesion: () -> Unit,
    viewModel: AsistenciaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(profesorId) { viewModel.cargarClases(profesorId) }

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    val clase = uiState.claseSeleccionada
    // El botón atrás del celular vuelve a la lista de clases en vez de cerrar la app.
    BackHandler(enabled = clase != null) { viewModel.volverALasClases() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(if (clase == null) "Mis clases" else "Asistencia · ${clase.ritmo}")
                        Text(nombreProfesor, style = MaterialTheme.typography.bodySmall)
                    }
                },
                actions = {
                    if (clase == null) {
                        TextButton(onClick = onCerrarSesion) { Text("Salir") }
                    } else {
                        TextButton(onClick = viewModel::volverALasClases) { Text("Volver") }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (uiState.cargando) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            if (clase == null) {
                ListaClasesProfesor(clases = uiState.clases, cargando = uiState.cargando, onElegir = viewModel::seleccionarClase)
            } else {
                TomarAsistencia(
                    clase = clase,
                    uiState = uiState,
                    onMarcar = viewModel::marcar,
                    onGuardar = viewModel::guardar
                )
            }
        }
    }
}

@Composable
private fun ListaClasesProfesor(clases: List<Clase>, cargando: Boolean, onElegir: (Clase) -> Unit) {
    if (!cargando && clases.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No tienes clases programadas desde hoy.", textAlign = TextAlign.Center)
        }
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Toca una clase para tomar asistencia. Solo se puede registrar el día de la clase.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        items(clases, key = { it.id }) { clase ->
            Card(modifier = Modifier.fillMaxWidth().clickable { onElegir(clase) }) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(clase.ritmo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        if (FechasApi.esHoy(clase.inicioMillis)) {
                            Text(
                                "  · HOY",
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(clase.horario, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${clase.salon} · ${clase.cupoMaximo - clase.cuposDisponibles} estudiantes inscritos",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun TomarAsistencia(
    clase: Clase,
    uiState: AsistenciaUiState,
    onMarcar: (String, EstadoAsistencia) -> Unit,
    onGuardar: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("${clase.horario} · ${clase.salon}", style = MaterialTheme.typography.bodyMedium)
        }
        if (!uiState.cargando && uiState.inscritos.isEmpty()) {
            item { Text("Nadie ha reservado esta clase todavía.") }
        }
        items(uiState.inscritos, key = { it.estudianteId }) { inscrito ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(inscrito.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EstadoAsistencia.entries.forEach { estado ->
                            FilterChip(
                                selected = uiState.marcas[inscrito.estudianteId] == estado,
                                onClick = { onMarcar(inscrito.estudianteId, estado) },
                                label = { Text(estado.texto) }
                            )
                        }
                    }
                }
            }
        }
        if (uiState.inscritos.isNotEmpty()) {
            item {
                Button(
                    onClick = onGuardar,
                    enabled = uiState.todosMarcados && !uiState.guardando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.guardando) CircularProgressIndicator() else Text("Guardar asistencia")
                }
            }
            item {
                val asistieron = uiState.marcas.values.count { it == EstadoAsistencia.ASISTIO }
                when {
                    uiState.guardada -> Text(
                        "Asistencia guardada: $asistieron de ${uiState.inscritos.size} asistieron.",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    !uiState.todosMarcados -> Text(
                        "Marca a todos los estudiantes para poder guardar.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
