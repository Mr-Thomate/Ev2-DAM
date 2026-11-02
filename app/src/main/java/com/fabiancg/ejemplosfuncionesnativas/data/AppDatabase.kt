package com.fabiancg.ejemplosfuncionesnativas.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Usuario::class, Documento::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun documentoDao(): DocumentoDao

    companion object {

        @Volatile
        private var instancia: AppDatabase? = null

        val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE usuarios ADD COLUMN claveProtegida TEXT DEFAULT NULL"
                )
            }
        }

        fun obtener(contexto: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    contexto.applicationContext,
                    AppDatabase::class.java,
                    "esfocar_estudio.db"
                )
                    .addMigrations(MIGRACION_1_2)
                    .addCallback(DatosDePrueba.callback)
                    .build()
                    .also { instancia = it }
            }
    }
}