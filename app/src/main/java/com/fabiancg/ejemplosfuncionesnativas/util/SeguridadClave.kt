package com.fabiancg.ejemplosfuncionesnativas.util

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object SeguridadClave {

    private const val ITERACIONES = 600_000

    fun proteger(clave: String): String {
        val sal = ByteArray(16).also {
            SecureRandom().nextBytes(it)
        }

        val hash = calcular(clave, sal)
        val codificador = Base64.getEncoder()

        return listOf(
            "pbkdf2-sha256",
            ITERACIONES.toString(),
            codificador.encodeToString(sal),
            codificador.encodeToString(hash)
        ).joinToString(":")
    }

    fun verificar(clave: String, guardada: String): Boolean {
        val partes = guardada.split(":")

        if (
            partes.size != 4 ||
            partes[0] != "pbkdf2-sha256" ||
            partes[1].toIntOrNull() != ITERACIONES
        ) {
            return false
        }

        return try {
            val sal = Base64.getDecoder().decode(partes[2])
            val hash = Base64.getDecoder().decode(partes[3])

            if (sal.size != 16 || hash.size != 32) {
                return false
            }

            MessageDigest.isEqual(
                hash,
                calcular(clave, sal)
            )
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    private fun calcular(
        clave: String,
        sal: ByteArray
    ): ByteArray {
        val caracteres = clave.toCharArray()

        val especificacion = PBEKeySpec(
            caracteres,
            sal,
            ITERACIONES,
            256
        )

        return try {
            SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(especificacion)
                .encoded
        } finally {
            especificacion.clearPassword()
            caracteres.fill('\u0000')
        }
    }
}