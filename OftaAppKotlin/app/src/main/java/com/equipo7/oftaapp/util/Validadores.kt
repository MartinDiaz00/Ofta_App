package com.equipo7.oftaapp.util

import java.time.LocalDate

object Validadores {
    fun nombreValido(valor: String): Boolean =
        valor.trim().matches(Regex("^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ' -]{3,80}$"))

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
        val esperado = when (resto) {
            11 -> '0'
            10 -> 'K'
            else -> ('0'.code + resto).toChar()
        }
        return verificador == esperado
    }

    fun rutFormateado(valor: String): String {
        val limpio = valor.uppercase().replace(".", "").replace("-", "").replace(" ", "")
        if (limpio.length < 2) return valor.trim()
        val cuerpo = limpio.dropLast(1)
        val verificador = limpio.last()
        val conPuntos = cuerpo.reversed().chunked(3).joinToString(".").reversed()
        return "$conPuntos-$verificador"
    }

    fun correoValido(valor: String): Boolean =
        valor.trim().matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))

    fun telefonoNormalizado(valor: String): String {
        val digitos = valor.filter(Char::isDigit)
        return when {
            digitos.length == 9 && digitos.startsWith("9") -> "+56$digitos"
            digitos.length == 11 && digitos.startsWith("569") -> "+$digitos"
            digitos.length == 8 -> "+569$digitos"
            else -> valor.trim()
        }
    }

    fun telefonoValido(valor: String): Boolean {
        val normalizado = telefonoNormalizado(valor)
        return normalizado.matches(Regex("^\\+569\\d{8}$"))
    }

    fun fechaValida(valor: String): Boolean = Fechas.parseVisible(valor) != null

    fun fechaNacimientoValida(valor: String): Boolean {
        val fecha = Fechas.parseVisible(valor) ?: return false
        val hoy = LocalDate.now()
        if (fecha.isAfter(hoy)) return false
        return fecha.isAfter(hoy.minusYears(121))
    }

    fun fechaExamenValida(valor: String): Boolean {
        val fecha = Fechas.parseVisible(valor) ?: return false
        return !fecha.isAfter(LocalDate.now())
    }

    fun horaValida(valor: String): Boolean = Fechas.horaValida(valor)

    fun textoValido(valor: String, minimo: Int = 1): Boolean =
        valor.trim().length >= minimo

    fun observacionValida(valor: String): Boolean = valor.trim().length <= 500

    fun claveValida(valor: String): Boolean = valor.length >= 4

    fun formatearRutMientrasEscribe(valor: String): String {
        val limpio = valor.uppercase().filter { it.isDigit() || it == 'K' }.take(9)
        if (limpio.length <= 1) return limpio
        val cuerpo = limpio.dropLast(1)
        val verificador = limpio.last()
        val conPuntos = cuerpo.reversed().chunked(3).joinToString(".").reversed()
        return "$conPuntos-$verificador"
    }
}
