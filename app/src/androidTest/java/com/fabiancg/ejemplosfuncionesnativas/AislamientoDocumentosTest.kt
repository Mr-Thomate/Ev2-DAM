package com.fabiancg.ejemplosfuncionesnativas

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fabiancg.ejemplosfuncionesnativas.data.AppDatabase
import com.fabiancg.ejemplosfuncionesnativas.data.Documento
import com.fabiancg.ejemplosfuncionesnativas.data.DocumentoDao
import com.fabiancg.ejemplosfuncionesnativas.data.Usuario
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Prueba la regla "cada usuario ve únicamente sus documentos" contra Room de verdad
 * (base en memoria: no toca los datos reales de la app). Requiere emulador o teléfono conectado.
 */
@RunWith(AndroidJUnit4::class)
class AislamientoDocumentosTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: DocumentoDao
    private var idA = 0L
    private var idB = 0L

    private fun documento(usuarioId: Long, nombre: String) = Documento(
        usuarioId = usuarioId,
        nombre = nombre,
        asignatura = "Prueba",
        tipoMime = "application/pdf",
        tamanoBytes = 100,
        fechaCarga = System.currentTimeMillis(),
        rutaRelativa = null,
        esDePrueba = true
    )

    @Before
    fun crearBaseEnMemoria() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).build()
        dao = db.documentoDao()
        runBlocking {
            idA = db.usuarioDao().insertar(Usuario(nombre = "Usuario A", codigo = "T-A"))
            idB = db.usuarioDao().insertar(Usuario(nombre = "Usuario B", codigo = "T-B"))
            dao.insertar(documento(idA, "a1.pdf"))
            dao.insertar(documento(idA, "a2.pdf"))
            dao.insertar(documento(idB, "b1.pdf"))
        }
    }

    @After
    fun cerrarBase() {
        db.close()
    }

    @Test
    fun cadaUsuarioVeSoloSusDocumentos() {
        runBlocking {
            val deA = dao.observarDeUsuario(idA).first().map { it.nombre }.toSet()
            val deB = dao.observarDeUsuario(idB).first().map { it.nombre }.toSet()

            assertEquals(setOf("a1.pdf", "a2.pdf"), deA)
            assertEquals(setOf("b1.pdf"), deB)
        }
    }

    @Test
    fun unUsuarioNoPuedeEliminarDocumentosAjenos() {
        runBlocking {
            val docDeA = dao.observarDeUsuario(idA).first().first()

            // B intenta borrar un documento de A conociendo su id: no debe borrar nada.
            val filasBorradas = dao.eliminar(id = docDeA.id, usuarioId = idB)

            assertEquals(0, filasBorradas)
            assertEquals(2, dao.observarDeUsuario(idA).first().size)
        }
    }

    @Test
    fun elPropietarioSiPuedeEliminarSuDocumento() {
        runBlocking {
            val docDeA = dao.observarDeUsuario(idA).first().first()

            val filasBorradas = dao.eliminar(id = docDeA.id, usuarioId = idA)

            assertEquals(1, filasBorradas)
            assertEquals(1, dao.observarDeUsuario(idA).first().size)
            assertNull(dao.buscar(docDeA.id, idA))
        }
    }
}
