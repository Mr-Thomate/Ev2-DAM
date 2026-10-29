package com.fabiancg.ejemplosfuncionesnativas.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Query("SELECT * FROM usuarios ORDER BY nombre")
    fun observarTodos(): Flow<List<Usuario>>

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun buscarPorId(id: Long): Usuario?

    @Insert
    suspend fun insertar(usuario: Usuario): Long

    @Query("SELECT * FROM usuarios WHERE codigo = :codigo")
    suspend fun buscarPorCodigo(codigo: String): Usuario?

    @Query("UPDATE usuarios SET nombre = :nombre WHERE id = :id")
    suspend fun actualizarNombre(id: Long, nombre: String): Int
}
