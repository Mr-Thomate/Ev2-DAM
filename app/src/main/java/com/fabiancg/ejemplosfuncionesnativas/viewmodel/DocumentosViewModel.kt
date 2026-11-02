package com.fabiancg.ejemplosfuncionesnativas.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fabiancg.ejemplosfuncionesnativas.data.ArchivoInvalidoException
import com.fabiancg.ejemplosfuncionesnativas.data.Documento
import com.fabiancg.ejemplosfuncionesnativas.data.DocumentoAbrible
import com.fabiancg.ejemplosfuncionesnativas.data.DocumentoRepository
import com.fabiancg.ejemplosfuncionesnativas.data.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/** Todo lo que la pantalla necesita dibujar, en un solo objeto inmutable. */
data class DocumentosUiState(
    val usuario: Usuario? = null,
    val documentos: List<Documento> = emptyList(),
    val cargando: Boolean = true,   // primera carga de la lista
    val ocupado: Boolean = false,   // copiando un archivo
    val mensaje: String? = null,    // aviso pendiente de mostrar (Snackbar)
    val paraAbrir: DocumentoAbrible? = null // documento listo para que la pantalla lo abra
)

/**
 * Este ViewModel pertenece a UN usuario: recibe su id al crearse y TODAS las operaciones
 * pasan ese id al repositorio. La pantalla nunca decide de quién son los documentos.
 */
class DocumentosViewModel(
    private val repositorio: DocumentoRepository,
    private val usuarioId: Long
) : ViewModel() {

    private val _estado = MutableStateFlow(DocumentosUiState())
    val estado: StateFlow<DocumentosUiState> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            val usuario = repositorio.buscarUsuario(usuarioId)
            _estado.update { it.copy(usuario = usuario) }
        }
        viewModelScope.launch {
            // collect se queda escuchando: si cambia la tabla, llega una lista nueva.
            repositorio.observarDocumentos(usuarioId).collect { lista ->
                _estado.update { it.copy(documentos = lista, cargando = false) }
            }
        }
    }

    fun cargarDocumento(uri: Uri) {
        viewModelScope.launch {
            _estado.update { it.copy(ocupado = true) }
            val mensaje = try {
                repositorio.cargarDocumento(usuarioId, uri)
                "Documento cargado correctamente"
            } catch (e: CancellationException) {
                throw e // la corrutina fue cancelada (ej. se cerró la pantalla): no es un error
            } catch (e: ArchivoInvalidoException) {
                e.message ?: "El archivo no es válido"
            } catch (e: Exception) {
                "No se pudo cargar el documento"
            }
            _estado.update { it.copy(ocupado = false, mensaje = mensaje) }
        }
    }

    fun eliminarDocumento(documentoId: Long) {
        viewModelScope.launch {
            val mensaje = try {
                if (repositorio.eliminarDocumento(usuarioId, documentoId)) {
                    "Documento eliminado"
                } else {
                    "El documento ya no existe"
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                "No se pudo eliminar el documento"
            }
            _estado.update { it.copy(mensaje = mensaje) }
        }
    }

    /**
     * El ViewModel NO abre el documento él mismo: abrir otra app requiere un Context de Android,
     * que es cosa de la pantalla. Aquí solo se prepara y se deja en el estado; la pantalla lo consume.
     */
    fun abrirDocumento(documentoId: Long) {
        viewModelScope.launch {
            try {
                val abrible = repositorio.prepararParaAbrir(usuarioId, documentoId)
                _estado.update { it.copy(paraAbrir = abrible) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ArchivoInvalidoException) {
                _estado.update { it.copy(mensaje = e.message ?: "No se pudo abrir el documento") }
            } catch (e: Exception) {
                _estado.update { it.copy(mensaje = "No se pudo abrir el documento") }
            }
        }
    }

    /** La pantalla avisa que ya intentó abrir el documento, para no abrirlo de nuevo al rotar el teléfono. */
    fun aperturaProcesada() {
        _estado.update { it.copy(paraAbrir = null) }
    }

    /** Permite que la pantalla muestre un aviso (por ejemplo, si no hay ninguna app para abrir el archivo). */
    fun mostrarMensaje(texto: String) {
        _estado.update { it.copy(mensaje = texto) }
    }

    /** La pantalla avisa que ya mostró el mensaje, para no repetirlo al rotar el teléfono. */
    fun mensajeMostrado() {
        _estado.update { it.copy(mensaje = null) }
    }

    class Factory(
        private val repositorio: DocumentoRepository,
        private val usuarioId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DocumentosViewModel(repositorio, usuarioId) as T
    }
}
