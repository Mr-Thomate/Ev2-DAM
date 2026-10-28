package com.fabiancg.ejemplosfuncionesnativas.data

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Datos ficticios.
 *
 * Se insertan UNA sola vez
 *
 * El Estudiante C queda SIN documentos a propósito, para probar la pantalla "lista vacía".
 */
object DatosDePrueba {

    private class DocumentoDemo(
        val codigoUsuario: String,
        val nombre: String,
        val asignatura: String,
        val tipoMime: String,
        val tamanoBytes: Long
    )

    private val usuarios = listOf(
        "Estudiante Demo A" to "DEMO-001",
        "Estudiante Demo B" to "DEMO-002",
        "Estudiante Demo C" to "DEMO-003"
    )

    private const val PDF = "application/pdf"
    private const val DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"

    private val documentos = listOf(
        DocumentoDemo("DEMO-001", "Apuntes Unidad 1 - Ética policial (ficticio).pdf", "Ética y Deontología", PDF, 482_000),
        DocumentoDemo("DEMO-001", "Manual de procedimientos básicos (ficticio).pdf", "Procedimientos Operativos", PDF, 1_250_000),
        DocumentoDemo("DEMO-001", "Guía de repaso - Primeros auxilios (ficticio).pdf", "Primeros Auxilios", PDF, 730_000),
        DocumentoDemo("DEMO-002", "Resumen Derecho Penal (ficticio).pdf", "Normativa Institucional", PDF, 910_000),
        DocumentoDemo("DEMO-002", "Trivia - Normativa de tránsito (ficticio).docx", "Normativa Institucional", DOCX, 64_000)
    )

    // Nota: onCreate(SupportSQLiteDatabase) solo se invoca cuando Room trabaja sin "driver"
    // (nuestro caso: no configuramos ninguno). Si algún día se migra a Room 3, este método
    // pasa a recibir un SQLiteConnection.
    val callback = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            insertar(db)
        }
    }

    private fun insertar(db: SupportSQLiteDatabase) {
        usuarios.forEach { (nombre, codigo) ->
            db.execSQL(
                "INSERT INTO usuarios (nombre, codigo) VALUES (?, ?)",
                arrayOf<Any?>(nombre, codigo)
            )
        }

        val ahora = System.currentTimeMillis()
        documentos.forEachIndexed { indice, d ->
            // El usuarioId se obtiene con una subconsulta por "codigo": no suponemos qué id quedó.
            db.execSQL(
                "INSERT INTO documentos " +
                    "(usuarioId, nombre, asignatura, tipoMime, tamanoBytes, fechaCarga, rutaRelativa, esDePrueba) " +
                    "VALUES ((SELECT id FROM usuarios WHERE codigo = ?), ?, ?, ?, ?, ?, NULL, 1)",
                arrayOf<Any?>(
                    d.codigoUsuario,
                    d.nombre,
                    d.asignatura,
                    d.tipoMime,
                    d.tamanoBytes,
                    ahora - indice * 86_400_000L // cada documento "cargado" un día antes que el anterior
                )
            )
        }
    }
}
