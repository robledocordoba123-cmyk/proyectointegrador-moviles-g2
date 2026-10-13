package com.example.proyectointegrador_moviles_g2.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.proyectointegrador_moviles_g2.data.CLAVE_DE_PRUEBA
import com.example.proyectointegrador_moviles_g2.data.Usuario
import com.example.proyectointegrador_moviles_g2.data.usuariosDePrueba
import com.example.proyectointegrador_moviles_g2.data.validarCredenciales
import com.example.proyectointegrador_moviles_g2.ui.theme.Proyectointegradormovilesg2Theme

// Inicio de sesión (RF-02). Valida contra los usuarios de prueba y, según el rol,
// la app abre el catálogo del estudiante o la pantalla del profesor.
@Composable
fun LoginScreen(onIngresar: (Usuario) -> Unit) {
    var correo by rememberSaveable { mutableStateOf("") }
    var clave by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    fun intentarIngreso(correoIngresado: String, claveIngresada: String) {
        val usuario = validarCredenciales(correoIngresado, claveIngresada)
        if (usuario == null) {
            error = "Correo o contraseña incorrectos"
        } else {
            error = null
            onIngresar(usuario)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("RitmoApp", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = clave,
            onValueChange = { clave = it },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = { intentarIngreso(correo, clave) }, modifier = Modifier.fillMaxWidth()) {
            Text("Ingresar")
        }

        // Acceso rápido para la demo, igual que en la web de RitmoApp.
        Spacer(modifier = Modifier.height(28.dp))
        Text("Entrar con un usuario de prueba", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            usuariosDePrueba.forEach { usuario ->
                OutlinedButton(onClick = { intentarIngreso(usuario.correo, CLAVE_DE_PRUEBA) }) {
                    Text(usuario.rol.name.lowercase().replaceFirstChar { it.uppercase() })
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    Proyectointegradormovilesg2Theme {
        LoginScreen(onIngresar = {})
    }
}
