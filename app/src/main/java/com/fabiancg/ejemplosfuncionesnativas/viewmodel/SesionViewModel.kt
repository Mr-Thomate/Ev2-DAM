package com.fabiancg.ejemplosfuncionesnativas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fabiancg.ejemplosfuncionesnativas.data.Usuario
import com.fabiancg.ejemplosfuncionesnativas.data.UsuarioRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SesionUiState(
    val usuario: Usuario? = null,
    val ocupado: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)

class SesionViewModel(
    private val repositorio: UsuarioRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(SesionUiState())
    val estado = _estado.asStateFlow()

    private var tarea: Job? = null

    fun iniciarSesion(
        codigo: String,
        clave: String
    ) = ejecutar {
        SesionUiState(
            usuario = repositorio.iniciarSesion(codigo, clave)
        )
    }

    fun registrar(
        nombre: String,
        codigo: String,
        clave: String,
        confirmacion: String
    ) = ejecutar {
        SesionUiState(
            usuario = repositorio.registrar(
                nombre,
                codigo,
                clave,
                confirmacion
            )
        )
    }

    fun editarPerfil(nombre: String, codigo: String) {
        val usuario = _estado.value.usuario ?: return

        ejecutar {
            SesionUiState(
                usuario = repositorio.editarPerfil(
                    usuario.id,
                    nombre,
                    codigo
                ),
                mensaje = "Datos actualizados correctamente."
            )
        }
    }

    fun limpiarMensajes() {
        _estado.update {
            it.copy(error = null, mensaje = null)
        }
    }

    fun cerrarSesion() {
        tarea?.cancel()
        _estado.value = SesionUiState()
    }

    private fun ejecutar(
        operacion: suspend () -> SesionUiState
    ) {
        if (_estado.value.ocupado) return

        _estado.update {
            it.copy(
                ocupado = true,
                error = null,
                mensaje = null
            )
        }

        tarea = viewModelScope.launch {
            try {
                val resultado = operacion()

                ensureActive()
                _estado.value = resultado
            } catch (e: CancellationException) {
                throw e
            } catch (e: IllegalArgumentException) {
                _estado.update {
                    it.copy(
                        ocupado = false,
                        error = e.message
                    )
                }
            } catch (e: Exception) {
                _estado.update {
                    it.copy(
                        ocupado = false,
                        error = "No se pudo completar la operación. Inténtalo otra vez."
                    )
                }
            }
        }
    }

    class Factory(
        private val repositorio: UsuarioRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {
            require(
                modelClass.isAssignableFrom(
                    SesionViewModel::class.java
                )
            )

            return SesionViewModel(repositorio) as T
        }
    }
}