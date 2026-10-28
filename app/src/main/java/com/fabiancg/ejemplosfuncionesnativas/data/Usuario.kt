package com.fabiancg.ejemplosfuncionesnativas.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Perfil de prueba (ficticio). El caso exige NO usar nombres, RUT ni correos reales.
 *
 * "codigo" es único: así, al sembrar los datos de prueba, podemos buscar a un usuario
 * por su código sin depender de qué número de id le tocó.
 */
@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["codigo"], unique = true)]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val codigo: String
)
