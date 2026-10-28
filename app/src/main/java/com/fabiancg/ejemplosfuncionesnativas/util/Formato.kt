package com.fabiancg.ejemplosfuncionesnativas.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Convierte bytes a un texto legible ("1,5 KB").
 * Usa aritmética entera a propósito: así el resultado no depende del idioma del teléfono
 * y la función se puede probar con una prueba unitaria simple.
 */
fun formatearTamano(bytes: Long): String = when {
    bytes < 1024L -> "$bytes B"
    bytes < 1024L * 1024L -> "${conUnDecimal(bytes, 1024L)} KB"
    else -> "${conUnDecimal(bytes, 1024L * 1024L)} MB"
}

private fun conUnDecimal(bytes: Long, divisor: Long): String {
    val decimas = bytes * 10 / divisor // 1,5 KB se representa como 15 décimas
    return "${decimas / 10},${decimas % 10}"
}

fun formatearFecha(milisegundos: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-CL")).format(Date(milisegundos))
