package com.fabiancg.ejemplosfuncionesnativas.data

import android.database.sqlite.SQLiteConstraintException
import com.fabiancg.ejemplosfuncionesnativas.util.SeguridadClave
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class UsuarioRepository(
    private val dao: UsuarioDao
) {

    suspend fun registrar(
        nombre: String,
        codigo: String,
        clave: String,
        confirmacion: String
    ): Usuario {
        val nombreLimpio = validarNombre(nombre)
        val codigoLimpio = validarCodigo(codigo)

        require(clave.length in 8..128 && clave.isNotBlank()) {
            "La contraseña debe tener entre 8 y 128 caracteres."
        }

        require(clave == confirmacion) {
            "Las contraseñas no coinciden."
        }

        require(dao.buscarPorCodigo(codigoLimpio) == null) {
            "Ese código ya está registrado."
        }

        // Este cálculo se ejecuta fuera del hilo de la pantalla.
        val protegida = withContext(Dispatchers.Default) {
            SeguridadClave.proteger(clave)
        }

        val usuario = Usuario(
            nombre = nombreLimpio,
            codigo = codigoLimpio,
            claveProtegida = protegida
        )

        val id = try {
            dao.insertar(usuario)
        } catch (e: SQLiteConstraintException) {
            throw IllegalArgumentException(
                "Ese código ya está registrado."
            )
        }

        // La pantalla recibe los datos del perfil sin el hash.
        return usuario.copy(
            id = id,
            claveProtegida = null
        )
    }

    suspend fun iniciarSesion(
        codigo: String,
        clave: String
    ): Usuario {
        val codigoLimpio = codigo.trim().uppercase(Locale.ROOT)

        require(codigoLimpio.isNotBlank() && clave.isNotBlank()) {
            "Completa el código y la contraseña."
        }

        require(clave.length <= 128) {
            "Código o contraseña incorrectos."
        }

        val usuario = dao.buscarPorCodigo(codigoLimpio)
        val protegida = usuario?.claveProtegida

        val correcta = if (protegida == null) {
            false
        } else {
            withContext(Dispatchers.Default) {
                SeguridadClave.verificar(clave, protegida)
            }
        }

        if (usuario == null || !correcta) {
            throw IllegalArgumentException(
                "Código o contraseña incorrectos."
            )
        }

        return usuario.copy(claveProtegida = null)
    }

    suspend fun editarPerfil(
        id: Long,
        nombre: String,
        codigo: String
    ): Usuario {
        val nombreLimpio = validarNombre(nombre)
        val codigoLimpio = validarCodigo(codigo)

        val filas = try {
            dao.actualizarPerfil(
                id = id,
                nombre = nombreLimpio,
                codigo = codigoLimpio
            )
        } catch (e: SQLiteConstraintException) {
            throw IllegalArgumentException(
                "Ese código ya pertenece a otro usuario."
            )
        }

        require(filas == 1) {
            "No se encontró el usuario."
        }

        val usuario = requireNotNull(dao.buscarPorId(id)) {
            "No se encontró el usuario."
        }

        return usuario.copy(claveProtegida = null)
    }

    private fun validarNombre(nombre: String): String {
        val limpio = nombre.trim()

        require(limpio.length in 2..60) {
            "El nombre debe tener entre 2 y 60 caracteres."
        }

        return limpio
    }

    private fun validarCodigo(codigo: String): String {
        val limpio = codigo.trim().uppercase(Locale.ROOT)

        require(Regex("[A-Z0-9-]{3,30}").matches(limpio)) {
            "El código debe tener de 3 a 30 caracteres: letras, números o guion."
        }

        return limpio
    }
}