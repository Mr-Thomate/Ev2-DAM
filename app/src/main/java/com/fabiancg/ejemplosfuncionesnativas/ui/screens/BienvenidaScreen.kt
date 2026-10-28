package com.fabiancg.ejemplosfuncionesnativas.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabiancg.ejemplosfuncionesnativas.data.Usuario
import com.fabiancg.ejemplosfuncionesnativas.viewmodel.BienvenidaViewModel

@Composable
fun BienvenidaScreen(
    viewModel: BienvenidaViewModel,
    onUsuarioElegido: (Usuario) -> Unit
) {
    // collectAsStateWithLifecycle: deja de escuchar la base de datos cuando la app está en segundo plano.
    val usuarios by viewModel.usuarios.collectAsStateWithLifecycle()
    val lista = usuarios // copia local: permite el "smart cast" de null a lista

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Encabezado() }

            if (lista == null) {
                item {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                items(lista, key = { it.id }) { usuario ->
                    UsuarioCard(usuario = usuario, onClick = { onUsuarioElegido(usuario) })
                }
            }

            item {
                Text(
                    text = "MVP académico: todos los datos son ficticios y se guardan solo en este teléfono. " +
                        "La app funciona sin conexión a internet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun Encabezado() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "¡Bienvenido/a!",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Punto Verde",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Guarda tus documentos de estudio y repásalos en cualquier lugar, incluso sin conexión.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Elige un perfil de prueba para ingresar",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun UsuarioCard(usuario: Usuario, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null, // decorativo: el nombre ya está escrito al lado
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(text = usuario.nombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Código: ${usuario.codigo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
