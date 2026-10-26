package com.equipo7.oftaapp.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

object Validadores {
    private val formatoFecha = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)

    fun fechaValida(valor: String): Boolean = try {
        LocalDate.parse(valor.trim(), formatoFecha)
        true
    } catch (_: Exception) { false }

    fun correoValido(valor: String): Boolean =
        valor.trim().matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))

    fun telefonoValido(valor: String): Boolean =
        valor.filter(Char::isDigit).let { it.length in 9..11 }

    fun rutValido(valor: String): Boolean {
        val limpio = valor.uppercase().replace(".", "").replace("-", "").replace(" ", "")
        if (limpio.length !in 2..9) return false
        val cuerpo = limpio.dropLast(1)
        val verificador = limpio.last()
        if (!cuerpo.all(Char::isDigit)) return false
        var suma = 0
        var multiplicador = 2
        for (digito in cuerpo.reversed()) {
            suma += digito.digitToInt() * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }
        val resto = 11 - (suma % 11)
        val esperado = when (resto) { 11 -> '0'; 10 -> 'K'; else -> ('0'.code + resto).toChar() }
        return verificador == esperado
    }
}
