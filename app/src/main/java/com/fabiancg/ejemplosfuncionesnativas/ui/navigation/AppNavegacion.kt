package com.fabiancg.ejemplosfuncionesnativas.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fabiancg.ejemplosfuncionesnativas.data.AppDatabase
import com.fabiancg.ejemplosfuncionesnativas.data.DocumentoRepository
import com.fabiancg.ejemplosfuncionesnativas.data.UsuarioRepository
import com.fabiancg.ejemplosfuncionesnativas.ui.screens.DocumentosScreen
import com.fabiancg.ejemplosfuncionesnativas.ui.screens.LoginScreen
import com.fabiancg.ejemplosfuncionesnativas.ui.screens.PerfilScreen
import com.fabiancg.ejemplosfuncionesnativas.viewmodel.DocumentosViewModel
import com.fabiancg.ejemplosfuncionesnativas.viewmodel.SesionViewModel

@Composable
fun AppNavegacion(repositorio: DocumentoRepository) {
    val contexto = LocalContext.current.applicationContext

    val usuarios = remember(contexto) {
        UsuarioRepository(
            AppDatabase.obtener(contexto).usuarioDao()
        )
    }

    val sesion: SesionViewModel = viewModel(
        factory = SesionViewModel.Factory(usuarios)
    )

    val estado by sesion.estado.collectAsStateWithLifecycle()
    val usuario = estado.usuario

    if (usuario == null) {
        var esRegistro by rememberSaveable {
            mutableStateOf(false)
        }

        BackHandler(
            enabled = esRegistro || estado.ocupado
        ) {
            if (!estado.ocupado) {
                esRegistro = false
                sesion.limpiarMensajes()
            }
        }

        LoginScreen(
            onIniciarSesion = sesion::iniciarSesion,
            cargando = estado.ocupado,
            mensajeError = estado.error,
            esRegistro = esRegistro,
            onRegistrar = sesion::registrar,
            onCambiarModo = {
                esRegistro = !esRegistro
                sesion.limpiarMensajes()
            },
            onLimpiarError = sesion::limpiarMensajes
        )
    } else {
        // Las pantallas privadas requieren una sesión válida.
        key(usuario.id) {
            val navController = rememberNavController()

            val salir: () -> Unit = {
                navController.popBackStack(
                    "inicio",
                    inclusive = true
                )
                sesion.cerrarSesion()
            }

            NavHost(
                navController = navController,
                startDestination = "inicio"
            ) {
                composable("inicio") {
                    PerfilScreen(
                        usuario = usuario,
                        cargando = estado.ocupado,
                        error = estado.error,
                        mensaje = estado.mensaje,
                        onGuardar = sesion::editarPerfil,
                        onDocumentos = {
                            navController.navigate("documentos") {
                                launchSingleTop = true
                            }
                        },
                        onCerrarSesion = salir,
                        onLimpiarMensajes = sesion::limpiarMensajes
                    )
                }

                composable("documentos") {
                    val documentos: DocumentosViewModel = viewModel(
                        key = "documentos_${usuario.id}",
                        factory = DocumentosViewModel.Factory(
                            repositorio,
                            usuario.id
                        )
                    )

                    DocumentosScreen(
                        viewModel = documentos,
                        onCerrarSesion = salir
                    )
                }
            }
        }
    }
}