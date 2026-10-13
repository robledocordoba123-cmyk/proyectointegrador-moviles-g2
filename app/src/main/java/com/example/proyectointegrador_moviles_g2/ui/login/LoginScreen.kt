package com.example.proyectointegrador_moviles_g2.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectointegrador_moviles_g2.data.CLAVE_DEMO
import com.example.proyectointegrador_moviles_g2.data.CORREO_DEMO_ESTUDIANTE
import com.example.proyectointegrador_moviles_g2.data.CORREO_DEMO_PROFESOR
import com.example.proyectointegrador_moviles_g2.data.Sesion
import com.example.proyectointegrador_moviles_g2.ui.theme.Proyectointegradormovilesg2Theme

// Inicio de sesión (RF-02). Según el rol que devuelve la API,
// la app abre el catálogo del estudiante o la pantalla del profesor.
@Composable
fun LoginScreen(
    onIngresar: (Sesion) -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    LoginContenido(
        uiState = uiState,
        onIngresar = { correo, clave -> viewModel.ingresar(correo, clave, onIngresar) }
    )
}

@Composable
fun LoginContenido(uiState: LoginUiState, onIngresar: (String, String) -> Unit) {
    var correo by rememberSaveable { mutableStateOf("") }
    var clave by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "RitmoApp",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Clases de baile en tu celular",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo") },
            singleLine = true,
            enabled = !uiState.cargando,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = clave,
            onValueChange = { clave = it },
            label = { Text("Contraseña") },
            singleLine = true,
            enabled = !uiState.cargando,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        uiState.error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = { onIngresar(correo, clave) },
            enabled = !uiState.cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.cargando) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Ingresar")
            }
        }
        if (uiState.cargando) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Conectando con el servidor. La primera vez puede tardar hasta un minuto.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        // Acceso rápido con los usuarios de la academia de ejemplo, igual que en la web de RitmoApp.
        Spacer(modifier = Modifier.height(28.dp))
        Text("Entrar con un usuario de prueba", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { onIngresar(CORREO_DEMO_ESTUDIANTE, CLAVE_DEMO) },
                enabled = !uiState.cargando
            ) { Text("Estudiante") }
            OutlinedButton(
                onClick = { onIngresar(CORREO_DEMO_PROFESOR, CLAVE_DEMO) },
                enabled = !uiState.cargando
            ) { Text("Profesor") }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    Proyectointegradormovilesg2Theme {
        LoginContenido(uiState = LoginUiState(), onIngresar = { _, _ -> })
    }
}
