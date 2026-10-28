package com.fabiancg.ejemplosfuncionesnativas.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabiancg.ejemplosfuncionesnativas.data.Documento
import com.fabiancg.ejemplosfuncionesnativas.util.formatearFecha
import com.fabiancg.ejemplosfuncionesnativas.util.formatearTamano
import com.fabiancg.ejemplosfuncionesnativas.viewmodel.DocumentosViewModel

// Tipos de archivo que el selector deja elegir. Para permitir cualquier archivo: arrayOf("*/*").
private val TIPOS_PERMITIDOS = arrayOf(
    "application/pdf",
    "text/plain",
    "application/msword",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "application/vnd.openxmlformats-officedocument.presentationml.presentation"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentosScreen(
    viewModel: DocumentosViewModel,
    onCerrarSesion: () -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var documentoAEliminar by remember { mutableStateOf<Documento?>(null) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // Mismo patrón del ejemplo EjemploArchivos(): OpenDocument abre el selector del sistema,
    // y no necesita pedir permisos de almacenamiento porque el usuario elige el archivo.
    val selectorArchivos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        // Si el usuario cancela el selector, uri es null.
        if (uri != null) viewModel.cargarDocumento(uri)
    }

    // Muestra el mensaje pendiente y, cuando termina, avisa al ViewModel para borrarlo.
    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let { texto ->
            snackbarHostState.showSnackbar(texto)
            viewModel.mensajeMostrado()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Mis documentos", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        estado.usuario?.let {
                            Text(
                                text = it.nombre,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onCerrarSesion) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Cerrar sesión y volver a la bienvenida"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { selectorArchivos.launch(TIPOS_PERMITIDOS) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Cargar documento") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                estado.cargando -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                estado.documentos.isEmpty() -> Text(
                    text = "Aún no tienes documentos.\nUsa «Cargar documento» para agregar el primero.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(innerPadding)
                        .padding(32.dp)
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(
                        top = innerPadding.calculateTopPadding() + 8.dp,
                        // espacio extra abajo para que el botón flotante no tape el último elemento
                        bottom = innerPadding.calculateBottomPadding() + 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(estado.documentos, key = { it.id }) { documento ->
                        DocumentoItem(
                            documento = documento,
                            onEliminar = { documentoAEliminar = documento }
                        )
                    }
                }
            }

            if (estado.ocupado) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = innerPadding.calculateTopPadding())
                )
            }
        }
    }

    // Confirmación: eliminar no se puede deshacer, así que se pide confirmar antes de actuar.
    documentoAEliminar?.let { documento ->
        AlertDialog(
            onDismissRequest = { documentoAEliminar = null },
            title = { Text("Eliminar documento") },
            text = { Text("¿Eliminar «${documento.nombre}»? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarDocumento(documento.id)
                    documentoAEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { documentoAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun DocumentoItem(documento: Documento, onEliminar: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = documento.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = documento.asignatura,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${formatearTamano(documento.tamanoBytes)} · ${formatearFecha(documento.fechaCarga)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (documento.esDePrueba) {
                    Text(
                        text = "Documento de ejemplo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            IconButton(onClick = onEliminar) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Eliminar ${documento.nombre}"
                )
            }
        }
    }
}
