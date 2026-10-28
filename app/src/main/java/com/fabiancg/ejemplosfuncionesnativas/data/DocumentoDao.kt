package com.fabiancg.ejemplosfuncionesnativas.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * REGLA DE AISLAMIENTO: ninguna consulta de este DAO puede leer o borrar documentos
 * sin recibir el usuarioId. No existe un "SELECT * FROM documentos" a propósito.
 */
@Dao
interface DocumentoDao {

    /** Flow: la lista de la pantalla se actualiza sola cuando se agrega o elimina algo. */
    @Query("SELECT * FROM documentos WHERE usuarioId = :usuarioId ORDER BY fechaCarga DESC")
    fun observarDeUsuario(usuarioId: Long): Flow<List<Documento>>

    /** Solo encuentra el documento si pertenece a ese usuario. */
    @Query("SELECT * FROM documentos WHERE id = :id AND usuarioId = :usuarioId")
    suspend fun buscar(id: Long, usuarioId: Long): Documento?

    @Insert
    suspend fun insertar(documento: Documento): Long

    /**
     * Devuelve cuántas filas borró. Si el documento es de OTRO usuario devuelve 0
     * y no toca nada, aunque alguien conozca su id.
     */
    @Query("DELETE FROM documentos WHERE id = :id AND usuarioId = :usuarioId")
    suspend fun eliminar(id: Long, usuarioId: Long): Int
}
