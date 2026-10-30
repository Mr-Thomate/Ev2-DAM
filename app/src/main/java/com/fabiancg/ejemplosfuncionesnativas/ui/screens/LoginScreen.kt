package com.fabiancg.ejemplosfuncionesnativas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fabiancg.ejemplosfuncionesnativas.ui.theme.EjemplosFuncionesNativasTheme

@Composable
fun LoginScreen(
    onIniciarSesion: (String, String) -> Unit,
    cargando: Boolean = false,
    mensajeError: String? = null,
    esRegistro: Boolean = false,
    onRegistrar: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    onCambiarModo: () -> Unit = {},
    onLimpiarError: () -> Unit = {}
) {
    var nombre by rememberSaveable(esRegistro) {
        mutableStateOf("")
    }

    var codigo by rememberSaveable(esRegistro) {
        mutableStateOf("")
    }

    // Las contraseñas no se guardan en el estado persistente de Android.
    var clave by remember(esRegistro) {
        mutableStateOf("")
    }

    var confirmacion by remember(esRegistro) {
        mutableStateOf("")
    }

    Scaffold { espacio ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacio)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "ESFOCAR",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Escuela de Formación de Carabineros",
                textAlign = TextAlign.Center
            )

            Text("Punto Verde · Material de estudio")

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
                        text = if (esRegistro) {
                            "Crear cuenta"
                        } else {
                            "Iniciar sesión"
                        },
                        style = MaterialTheme.typography.headlineSmall
                    )

                    if (esRegistro) {
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = {
                                nombre = it
                                onLimpiarError()
                            },
                            label = { Text("Nombre de prueba") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !cargando
                        )
                    }

                    OutlinedTextField(
                        value = codigo,
                        onValueChange = {
                            codigo = it
                            onLimpiarError()
                        },
                        label = { Text("Código de estudiante") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !cargando
                    )

                    CampoClave(
                        valor = clave,
                        etiqueta = "Contraseña",
                        habilitado = !cargando,
                        onCambio = {
                            clave = it
                            onLimpiarError()
                        }
                    )

                    if (esRegistro) {
                        Text(
                            text = "Usa entre 8 y 128 caracteres.",
                            style = MaterialTheme.typography.bodySmall
                        )

                        CampoClave(
                            valor = confirmacion,
                            etiqueta = "Repetir contraseña",
                            habilitado = !cargando,
                            onCambio = {
                                confirmacion = it
                                onLimpiarError()
                            }
                        )
                    }

                    mensajeError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Button(
                        onClick = {
                            if (esRegistro) {
                                onRegistrar(
                                    nombre,
                                    codigo,
                                    clave,
                                    confirmacion
                                )
                            } else {
                                onIniciarSesion(codigo, clave)
                            }
                        },
                        enabled = !cargando,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                    ) {
                        Text(
                            when {
                                cargando -> "Procesando…"
                                esRegistro -> "Crear cuenta"
                                else -> "Ingresar"
                            }
                        )
                    }

                    TextButton(
                        onClick = onCambiarModo,
                        enabled = !cargando
                    ) {
                        Text(
                            if (esRegistro) {
                                "Ya tengo una cuenta"
                            } else {
                                "Crear una cuenta"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CampoClave(
    valor: String,
    etiqueta: String,
    habilitado: Boolean,
    onCambio: (String) -> Unit
) {
    var visible by remember {
        mutableStateOf(false)
    }

    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = habilitado,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password
        ),
        visualTransformation = if (visible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            TextButton(
                onClick = { visible = !visible },
                enabled = habilitado
            ) {
                Text(if (visible) "Ocultar" else "Ver")
            }
        }
    )
}

@Preview(
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun LoginScreenPreview() {
    EjemplosFuncionesNativasTheme {
        LoginScreen(
            onIniciarSesion = { _, _ -> }
        )
    }
}