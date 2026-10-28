package com.fabiancg.ejemplosfuncionesnativas.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Usuario::class, Documento::class],
    version = 1,
    exportSchema = false // MVP: no guardamos el historial de esquemas en el repositorio
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun documentoDao(): DocumentoDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        /** Singleton: abrir varias instancias de la misma base de datos es caro y propenso a errores. */
        fun obtener(contexto: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    contexto.applicationContext,
                    AppDatabase::class.java,
                    "esfocar_estudio.db"
                )
                    .addCallback(DatosDePrueba.callback)
                    .build()
                    .also { instancia = it }
            }
    }
}
