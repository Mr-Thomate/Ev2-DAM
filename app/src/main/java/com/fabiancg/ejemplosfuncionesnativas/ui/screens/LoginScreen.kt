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
    mensajeError: String? = null
) {


    var codigo by rememberSaveable { mutableStateOf("") }


    var clave by remember { mutableStateOf("") }

    var mostrarClave by remember { mutableStateOf(false) }


    var errorCampos by remember { mutableStateOf<String?>(null) }

    Scaffold { espacioPantalla ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioPantalla)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),

            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "ESFOCAR",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Escuela de Formación de Carabineros",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Accede a tu material de estudio.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

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
                        text = "Iniciar sesión",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = codigo,

                        onValueChange = {
                            codigo = it
                            errorCampos = null
                        },

                        label = { Text("Código de estudiante") },

                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !cargando
                    )

                    OutlinedTextField(
                        value = clave,

                        onValueChange = {
                            clave = it
                            errorCampos = null
                        },

                        label = { Text("Contraseña") },

                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !cargando,

                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),

                        visualTransformation =
                            if (mostrarClave) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },

                        trailingIcon = {
                            TextButton(
                                onClick = {
                                    mostrarClave = !mostrarClave
                                },
                                enabled = !cargando
                            ) {
                                Text(
                                    if (mostrarClave) "Ocultar"
                                    else "Ver"
                                )
                            }
                        }
                    )


                    val errorVisible = errorCampos ?: mensajeError

                    errorVisible?.let { texto ->
                        Text(
                            text = texto,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp),

                        enabled = !cargando,

                        onClick = {

                            val codigoLimpio = codigo.trim()

                            if (codigoLimpio.isBlank() || clave.isBlank()) {

                                errorCampos = "Completa el código y la contraseña."

                            } else {

                                errorCampos = null


                                onIniciarSesion(codigoLimpio, clave)
                            }
                        }
                    ) {
                        Text(
                            if (cargando) "Ingresando…"
                            else "Ingresar"
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LoginScreenPreview() {

    EjemplosFuncionesNativasTheme {

        LoginScreen(
            onIniciarSesion = { _, _ ->

            }
        )
    }
}