package com.fabiancg.ejemplosfuncionesnativas.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Un documento de estudio que pertenece a UN solo usuario.
 *
 * - [usuarioId] es la clave del aislamiento: toda consulta filtra por este campo.
 * - [rutaRelativa] apunta a la copia del archivo dentro del almacenamiento privado de la app
 *   (filesDir). Es null en los documentos de prueba, que solo existen como registro.
 *   Se guarda RELATIVA para que siga siendo válida aunque cambie la ruta base del sistema.
 */
@Entity(
    tableName = "documentos",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE // si se borra un usuario, se borran sus documentos
        )
    ],
    indices = [Index(value = ["usuarioId"])] // acelera el filtro por usuario
)
data class Documento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val nombre: String,
    val asignatura: String,
    val tipoMime: String?,
    val tamanoBytes: Long,
    val fechaCarga: Long, // milisegundos desde 1970 (epoch)
    val rutaRelativa: String?,
    val esDePrueba: Boolean
)
