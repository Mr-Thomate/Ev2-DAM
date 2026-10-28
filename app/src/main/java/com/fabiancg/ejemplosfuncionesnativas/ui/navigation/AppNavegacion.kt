package com.fabiancg.ejemplosfuncionesnativas.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fabiancg.ejemplosfuncionesnativas.data.DocumentoRepository
import com.fabiancg.ejemplosfuncionesnativas.ui.screens.BienvenidaScreen
import com.fabiancg.ejemplosfuncionesnativas.ui.screens.DocumentosScreen
import com.fabiancg.ejemplosfuncionesnativas.viewmodel.BienvenidaViewModel
import com.fabiancg.ejemplosfuncionesnativas.viewmodel.DocumentosViewModel

private object Rutas {
    const val BIENVENIDA = "bienvenida"
    const val ARG_USUARIO_ID = "usuarioId"
    const val DOCUMENTOS = "documentos/{$ARG_USUARIO_ID}"
    fun documentos(usuarioId: Long) = "documentos/$usuarioId"
}

@Composable
fun AppNavegacion(repositorio: DocumentoRepository) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Rutas.BIENVENIDA) {

        composable(Rutas.BIENVENIDA) {
            val viewModel: BienvenidaViewModel = viewModel(
                factory = BienvenidaViewModel.Factory(repositorio)
            )
            BienvenidaScreen(
                viewModel = viewModel,
                onUsuarioElegido = { usuario ->
                    // launchSingleTop evita abrir dos pantallas si se toca la tarjeta dos veces rápido.
                    navController.navigate(Rutas.documentos(usuario.id)) { launchSingleTop = true }
                }
            )
        }

        composable(
            route = Rutas.DOCUMENTOS,
            arguments = listOf(navArgument(Rutas.ARG_USUARIO_ID) { type = NavType.LongType })
        ) { entrada ->
            val usuarioId = entrada.arguments?.getLong(Rutas.ARG_USUARIO_ID) ?: return@composable

            // El ViewModel queda atado a esta pantalla: al volver atrás se destruye junto con ella,
            // así otro usuario nunca hereda el estado del anterior. La "key" refuerza esa garantía.
            val viewModel: DocumentosViewModel = viewModel(
                key = "documentos_$usuarioId",
                factory = DocumentosViewModel.Factory(repositorio, usuarioId)
            )
            DocumentosScreen(
                viewModel = viewModel,
                onCerrarSesion = { navController.popBackStack() }
            )
        }
    }
}
