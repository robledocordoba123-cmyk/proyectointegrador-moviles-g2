package com.example.proyectointegrador_moviles_g2.ui.asistencia

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectointegrador_moviles_g2.data.Clase
import com.example.proyectointegrador_moviles_g2.data.inscritosDePrueba

// Pantalla del profesor: ve sus clases y toma asistencia (RF-07).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsistenciaScreen(
    profesor: String,
    onCerrarSesion: () -> Unit,
    viewModel: AsistenciaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(profesor) { viewModel.cargarClases(profesor) }

    val clase = uiState.claseSeleccionada
    // El botón atrás del celular vuelve a la lista de clases en vez de cerrar la app.
    BackHandler(enabled = clase != null) { viewModel.volverALasClases() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(if (clase == null) "Mis clases · Prof. $profesor" else "Asistencia · ${clase.ritmo}") },
                actions = {
                    if (clase == null) {
                        TextButton(onClick = onCerrarSesion) { Text("Salir") }
                    } else {
                        TextButton(onClick = viewModel::volverALasClases) { Text("Volver") }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (clase == null) {
            ListaClasesProfesor(
                clases = uiState.clases,
                onElegir = viewModel::seleccionarClase,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            TomarAsistencia(
                clase = clase,
                uiState = uiState,
                onMarcar = viewModel::marcar,
                onGuardar = viewModel::guardar,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun ListaClasesProfesor(clases: List<Clase>, onElegir: (Clase) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Toca una clase para tomar asistencia", style = MaterialTheme.typography.bodyMedium)
        }
        items(clases, key = { it.id }) { clase ->
            Card(modifier = Modifier.fillMaxWidth().clickable { onElegir(clase) }) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(clase.ritmo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(clase.horario, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${clase.salon} · ${inscritosDePrueba.size} estudiantes inscritos",
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
    onGuardar: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("${clase.horario} · ${clase.salon}", style = MaterialTheme.typography.bodyMedium)
        }
        items(uiState.inscritos) { estudiante ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(estudiante, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EstadoAsistencia.entries.forEach { estado ->
                            FilterChip(
                                selected = uiState.marcas[estudiante] == estado,
                                onClick = { onMarcar(estudiante, estado) },
                                label = { Text(estado.texto) }
                            )
                        }
                    }
                }
            }
        }
        item {
            Button(
                onClick = onGuardar,
                enabled = uiState.todosMarcados,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar asistencia") }
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
