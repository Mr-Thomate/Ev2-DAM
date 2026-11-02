package com.fabiancg.ejemplosfuncionesnativas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fabiancg.ejemplosfuncionesnativas.data.Usuario

@Composable
fun PerfilScreen(
    usuario: Usuario,
    cargando: Boolean,
    error: String?,
    mensaje: String?,
    onGuardar: (String, String) -> Unit,
    onDocumentos: () -> Unit,
    onCerrarSesion: () -> Unit,
    onLimpiarMensajes: () -> Unit
) {
    var nombre by rememberSaveable(
        usuario.id,
        usuario.nombre
    ) {
        mutableStateOf(usuario.nombre)
    }

    var codigo by rememberSaveable(
        usuario.id,
        usuario.codigo
    ) {
        mutableStateOf(usuario.codigo)
    }

    Scaffold { espacio ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacio)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Punto Verde · ESFOCAR",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "¡Bienvenido/a, ${usuario.nombre}!",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Mi perfil",
                        style = MaterialTheme.typography.titleLarge
                    )

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            nombre = it
                            onLimpiarMensajes()
                        },
                        label = { Text("Nombre de prueba") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !cargando
                    )

                    OutlinedTextField(
                        value = codigo,
                        onValueChange = {
                            codigo = it
                            onLimpiarMensajes()
                        },
                        label = { Text("Código de estudiante") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !cargando
                    )

                    error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    mensaje?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = {
                            onGuardar(nombre, codigo)
                        },
                        enabled = !cargando && (
                                nombre != usuario.nombre ||
                                        codigo != usuario.codigo
                                ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (cargando) {
                                "Guardando…"
                            } else {
                                "Guardar cambios"
                            }
                        )
                    }
                }
            }

            Button(
                onClick = onDocumentos,
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Mis documentos")
            }

            OutlinedButton(
                onClick = onCerrarSesion,
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}