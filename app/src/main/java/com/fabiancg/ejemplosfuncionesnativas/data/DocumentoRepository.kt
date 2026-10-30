package com.fabiancg.ejemplosfuncionesnativas.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID

/** Error "esperable" (el archivo no cumple una regla). Su mensaje sí se puede mostrar al usuario. */
class ArchivoInvalidoException(mensaje: String) : Exception(mensaje)

/** Lo necesario para pedirle a otra app que abra el documento: un enlace seguro y el tipo de archivo. */
class DocumentoAbrible(val uri: Uri, val tipoMime: String)

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

    /**
     * Prepara un documento para abrirlo con otra app (visor de PDF, Word, etc.).
     *
     * 1) Comprueba que el documento sea de ESTE usuario (misma regla de aislamiento que al listar y borrar).
     * 2) Ubica el archivo: la copia privada si el usuario lo cargó, o el archivo de ejemplo si es de prueba.
     * 3) Entrega un Uri "content://" mediante FileProvider. No se puede compartir una ruta de archivo
     *    directa ("file://"): Android la prohíbe, porque el visor no tiene permiso sobre nuestra carpeta privada.
     *    FileProvider da un permiso temporal solo para ese archivo.
     */
    suspend fun prepararParaAbrir(usuarioId: Long, documentoId: Long): DocumentoAbrible =
        withContext(Dispatchers.IO) {
            val documento = documentoDao.buscar(documentoId, usuarioId)
                ?: throw ArchivoInvalidoException("El documento ya no existe")

            val ruta = documento.rutaRelativa
            val archivo = if (ruta != null) File(contexto.filesDir, ruta) else copiarEjemploACache(documento)

            if (!archivo.exists()) {
                throw ArchivoInvalidoException("No se encontró el archivo de este documento")
            }

            val uri = FileProvider.getUriForFile(contexto, "${contexto.packageName}.fileprovider", archivo)
            DocumentoAbrible(uri, tipoMimeDe(documento))
        }

    /**
     * Los documentos de prueba vienen dentro del APK (assets), que no es un archivo real que otra app pueda leer.
     * Se copia a la carpeta de caché una sola vez y se reutiliza. En caché (y no en almacenamiento permanente)
     * porque, si el sistema la limpia, la copia se vuelve a generar sola desde los assets.
     */
    private fun copiarEjemploACache(documento: Documento): File {
        val nombreAsset = DatosDePrueba.archivoDeEjemplo(documento.nombre)
            ?: throw ArchivoInvalidoException("Este documento de ejemplo no tiene un archivo asociado")

        val destino = File(contexto.cacheDir, "ejemplos/$nombreAsset")
        if (!destino.exists()) {
            destino.parentFile?.mkdirs()
            // Se copia a un archivo temporal y luego se renombra: si la app se cierra a mitad de la copia,
            // nunca queda un archivo incompleto que parezca válido.
            val temporal = File(destino.parentFile, "$nombreAsset.tmp")
            contexto.assets.open("ejemplos/$nombreAsset").use { origen ->
                temporal.outputStream().use { salida -> origen.copyTo(salida) }
            }
            temporal.renameTo(destino)
        }
        return destino
    }

    /** Tipo de archivo (MIME) para que Android elija el visor correcto. Si no se guardó, se deduce de la extensión. */
    private fun tipoMimeDe(documento: Documento): String {
        val guardado = documento.tipoMime
        if (!guardado.isNullOrBlank() && guardado != "application/octet-stream") return guardado
        val extension = documento.nombre.substringAfterLast('.', "").lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
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
