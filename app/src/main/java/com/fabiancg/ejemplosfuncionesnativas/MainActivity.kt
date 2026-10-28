package com.fabiancg.ejemplosfuncionesnativas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fabiancg.ejemplosfuncionesnativas.data.DocumentoRepository
import com.fabiancg.ejemplosfuncionesnativas.ui.navigation.AppNavegacion
import com.fabiancg.ejemplosfuncionesnativas.ui.theme.EjemplosFuncionesNativasTheme

class MainActivity : ComponentActivity() {

    // "by lazy": el repositorio se crea la primera vez que se usa y se reutiliza después.
    private val repositorio by lazy { DocumentoRepository.crear(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EjemplosFuncionesNativasTheme {
                AppNavegacion(repositorio)
            }
        }
    }
}
