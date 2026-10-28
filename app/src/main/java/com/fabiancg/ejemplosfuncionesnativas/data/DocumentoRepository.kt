package com.fabiancg.ejemplosfuncionesnativas.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID

/** Error "esperable" (el archivo no cumple una regla). Su mensaje sí se puede mostrar al usuario. */
class ArchivoInvalidoException(mensaje: String) : Exception(mensaje)

class DocumentoRepository(
    private val contexto: Context,
    private val usuarioDao: UsuarioDao,
    private val documentoDao: DocumentoDao
) {

    fun observarUsuarios(): Flow<List<Usuario>> = usuarioDao.observarTodos()

    suspend fun buscarUsuario(id: Long): Usuario? = usuarioDao.buscarPorId(id)

    fun observarDocumentos(usuarioId: Long): Flow<List<Documento>> =
        documentoDao.observarDeUsuario(usuarioId)

    /**
     * Copia el archivo elegido al almacenamiento privado del usuario y lo registra en la base de datos.
     * Para asi poder funcionar aun estando offline
     */
    suspend fun cargarDocumento(usuarioId: Long, uri: Uri): Long = withContext(Dispatchers.IO) {
        val resolver = contexto.contentResolver

        // 1) Metadatos: nombre y tamaño tal como los informa el selector de archivos.
        var nombre = "documento"
        var tamanoInformado = -1L
        resolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val iNombre = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val iTamano = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (iNombre >= 0) cursor.getString(iNombre)?.let { nombre = it }
                if (iTamano >= 0 && !cursor.isNull(iTamano)) tamanoInformado = cursor.getLong(iTamano)
            }
        }

        // 2) Validación temprana: evita copiar un archivo enorme solo para rechazarlo después.
        if (tamanoInformado > TAMANO_MAXIMO_BYTES) {
            throw ArchivoInvalidoException("El archivo supera el máximo de $TAMANO_MAXIMO_MB MB")
        }

        // 3) Nombre INTERNO propio (UUID). Nunca usamos el nombre original para crear la ruta:
        //    un nombre como "../../algo" podría escribir fuera de la carpeta del usuario.
        val extension = nombre.substringAfterLast('.', "").filter { it.isLetterOrDigit() }.take(10)
        val nombreInterno = if (extension.isEmpty()) "${UUID.randomUUID()}" else "${UUID.randomUUID()}.$extension"

        // Una carpeta por usuario: el aislamiento también existe a nivel de archivos.
        val rutaRelativa = "documentos/$usuarioId/$nombreInterno"
        val destino = File(contexto.filesDir, rutaRelativa)
        destino.parentFile?.mkdirs()

        try {
            val entrada = resolver.openInputStream(uri)
                ?: throw IOException("No se pudo abrir el archivo seleccionado")
            entrada.use { origen ->
                destino.outputStream().use { salida -> origen.copyTo(salida) }
            }

            // Algunos proveedores no informan el tamaño: se comprueba con la copia ya hecha.
            if (destino.length() > TAMANO_MAXIMO_BYTES) {
                throw ArchivoInvalidoException("El archivo supera el máximo de $TAMANO_MAXIMO_MB MB")
            }

            documentoDao.insertar(
                Documento(
                    usuarioId = usuarioId,
                    nombre = nombre,
                    asignatura = ASIGNATURA_POR_DEFECTO,
                    tipoMime = resolver.getType(uri),
                    tamanoBytes = destino.length(),
                    fechaCarga = System.currentTimeMillis(),
                    rutaRelativa = rutaRelativa,
                    esDePrueba = false
                )
            )
        } catch (e: Exception) {
            // Si algo falló a mitad de camino, no dejamos un archivo "huérfano" ocupando espacio.
            destino.delete()
            throw e
        }
    }

    /**
     * Elimina el registro Y el archivo. Recibe el usuarioId para que un usuario
     * no pueda borrar documentos ajenos. Devuelve true si realmente se eliminó algo.
     */
    suspend fun eliminarDocumento(usuarioId: Long, documentoId: Long): Boolean =
        withContext(Dispatchers.IO) {
            val documento = documentoDao.buscar(documentoId, usuarioId) ?: return@withContext false

            val filasBorradas = documentoDao.eliminar(documentoId, usuarioId)
            if (filasBorradas > 0) {
                // Se borra el archivo DESPUÉS del registro: si fallara el archivo, solo quedaría
                // basura invisible; al revés, quedaría un registro que apunta a nada.
                documento.rutaRelativa?.let { File(contexto.filesDir, it).delete() }
            }
            filasBorradas > 0
        }

    companion object {
        const val TAMANO_MAXIMO_MB = 300
        private const val TAMANO_MAXIMO_BYTES = TAMANO_MAXIMO_MB * 1024L * 1024L
        private const val ASIGNATURA_POR_DEFECTO = "Material propio"

        fun crear(contexto: Context): DocumentoRepository {
            val app = contexto.applicationContext
            val db = AppDatabase.obtener(app)
            return DocumentoRepository(app, db.usuarioDao(), db.documentoDao())
        }
    }
}
