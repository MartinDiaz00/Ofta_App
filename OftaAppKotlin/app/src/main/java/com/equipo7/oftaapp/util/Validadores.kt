package com.equipo7.oftaapp.util

import java.time.LocalDate

object Validadores {
    fun nombreValido(valor: String): Boolean {
        val limpio = valor.trim().replace(Regex("\\s+"), " ")
        if (limpio.length !in 3..80) return false
        if (!limpio.matches(Regex("^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ' -]+$"))) return false
        // Debe contener al menos una letra y al menos dos partes razonables (nombre y apellido) o un nombre compuesto válido
        return limpio.any { it.isLetter() }
    }

    fun rutValido(valor: String): Boolean {
        val limpio = rutLimpio(valor)
        // Valida formato RUT chileno (xx.xxx.xxx-x): cuerpo numérico + dígito verificador 0-9/K.
        // No exige dígito verificador "real" (módulo 11) para permitir datos de prueba/demo.
        if (limpio.length !in 7..10) return false
        val cuerpo = limpio.dropLast(1)
        val verificador = limpio.last()
        if (cuerpo.length !in 6..9 || !cuerpo.all(Char::isDigit)) return false
        if (verificador != 'K' && !verificador.isDigit()) return false
        // Rechaza cuerpos inválidos obvios (ej. todos ceros)
        if (cuerpo.all { it == '0' }) return false
        return true
    }

    fun rutLimpio(valor: String): String =
        valor.trim().uppercase().replace(".", "").replace("-", "").replace(" ", "")

    fun rutFormateado(valor: String): String {
        val limpio = rutLimpio(valor)
        if (limpio.length < 2) return valor.trim()
        val cuerpo = limpio.dropLast(1)
        val verificador = limpio.last()
        if (cuerpo.isBlank() || !cuerpo.all(Char::isDigit) || (verificador != 'K' && !verificador.isDigit())) {
            return valor.trim()
        }
        val conPuntos = cuerpo.reversed().chunked(3).joinToString(".").reversed()
        return "$conPuntos-$verificador"
    }

    fun correoValido(valor: String): Boolean {
        val limpio = valor.trim()
        if (limpio.isBlank() || limpio.length > 254) return false
        return limpio.matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
    }

    fun telefonoNormalizado(valor: String): String {
        val limpio = valor.trim()
        val digitos = limpio.filter(Char::isDigit)
        return when {
            digitos.length == 9 && digitos.startsWith("9") -> "+56$digitos"
            digitos.length == 11 && digitos.startsWith("569") -> "+$digitos"
            digitos.length == 8 -> "+569$digitos"
            limpio.startsWith("+56") && digitos.length == 11 -> "+$digitos"
            else -> limpio
        }
    }

    fun telefonoValido(valor: String): Boolean {
        val normalizado = telefonoNormalizado(valor)
        return normalizado.matches(Regex("^\\+569\\d{8}$"))
    }

    fun telefonoOpcionalValido(valor: String): Boolean {
        val limpio = valor.trim()
        return limpio.isBlank() || telefonoValido(limpio)
    }

    fun previsionValida(valor: String): Boolean = valor.trim().isNotBlank()

    fun usuarioValido(valor: String): Boolean =
        valor.trim().matches(Regex("^[A-Za-z0-9._-]{3,40}$"))

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
        val limpio = valor.trim().uppercase().filter { it.isDigit() || it == 'K' }.take(10)
        if (limpio.length <= 1) return limpio
        val cuerpo = limpio.dropLast(1)
        val verificador = limpio.last()
        val conPuntos = cuerpo.reversed().chunked(3).joinToString(".").reversed()
        return "$conPuntos-$verificador"
    }
}
