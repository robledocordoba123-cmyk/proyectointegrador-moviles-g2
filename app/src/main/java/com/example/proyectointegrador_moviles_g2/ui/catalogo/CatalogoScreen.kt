package com.example.proyectointegrador_moviles_g2.ui.catalogo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectointegrador_moviles_g2.data.Clase
import com.example.proyectointegrador_moviles_g2.data.clasesDePrueba
import com.example.proyectointegrador_moviles_g2.ui.theme.Proyectointegradormovilesg2Theme

// Conecta la pantalla con su ViewModel.
@Composable
fun CatalogoScreen(
    nombreEstudiante: String,
    onCerrarSesion: () -> Unit,
    viewModel: CatalogoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    CatalogoContenido(
        uiState = uiState,
        nombreEstudiante = nombreEstudiante,
        onReservar = viewModel::reservar,
        onCancelar = viewModel::cancelar,
        onActualizar = viewModel::cargar,
        onMensajeMostrado = viewModel::mensajeMostrado,
        onCerrarSesion = onCerrarSesion
    )
}

// Solo dibuja: recibe el estado y avisa los clics hacia arriba (state hoisting).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoContenido(
    uiState: CatalogoUiState,
    nombreEstudiante: String,
    onReservar: (String) -> Unit,
    onCancelar: (String) -> Unit,
    onActualizar: () -> Unit = {},
    onMensajeMostrado: () -> Unit = {},
    onCerrarSesion: () -> Unit = {}
) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbar.showSnackbar(it)
            onMensajeMostrado()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Clases disponibles")
                        Text(nombreEstudiante, style = MaterialTheme.typography.bodySmall)
                    }
                },
                actions = {
                    TextButton(onClick = onActualizar, enabled = !uiState.cargando) { Text("Actualizar") }
                    TextButton(onClick = onCerrarSesion) { Text("Salir") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (uiState.cargando) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            if (!uiState.cargando && uiState.clases.isEmpty()) {
                Text(
                    "No hay clases próximas por ahora.",
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            }
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.clases, key = { it.id }) { clase ->
                    TarjetaClase(
                        clase = clase,
                        reservada = clase.id in uiState.reservadas,
                        procesando = uiState.procesando == clase.id,
                        onReservar = { onReservar(clase.id) },
                        onCancelar = { onCancelar(clase.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TarjetaClase(
    clase: Clase,
    reservada: Boolean,
    onReservar: () -> Unit,
    onCancelar: () -> Unit,
    procesando: Boolean = false
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = clase.ritmo,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(text = clase.horario, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "${clase.salon} · Prof. ${clase.profesor}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val sinCupos = clase.cuposDisponibles == 0
                Text(
                    text = when {
                        reservada -> "Reservada ✓"
                        sinCupos -> "Sin cupos"
                        else -> "Cupos: ${clase.cuposDisponibles} de ${clase.cupoMaximo}"
                    },
                    color = if (sinCupos && !reservada) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                when {
                    procesando -> CircularProgressIndicator()
                    reservada -> OutlinedButton(onClick = onCancelar) { Text("Cancelar") }
                    else -> Button(onClick = onReservar, enabled = !sinCupos) { Text("Reservar") }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CatalogoPreview() {
    Proyectointegradormovilesg2Theme {
        CatalogoContenido(
            uiState = CatalogoUiState(clases = clasesDePrueba, reservas = mapOf("1" to "r1")),
            nombreEstudiante = "Estudiante Demo",
            onReservar = {},
            onCancelar = {}
        )
    }
}
