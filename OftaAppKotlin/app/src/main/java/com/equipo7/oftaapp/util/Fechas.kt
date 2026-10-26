package com.equipo7.oftaapp.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.util.Locale

object Fechas {
    val formatoIso: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    val formatoVisible: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)
    private val formatoHora: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val formatoLargo: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d 'de' MMMM 'de' uuuu", Locale("es", "CL"))

    fun hoyIso(): String = LocalDate.now().format(formatoIso)

    fun ahoraHora(): String = LocalTime.now().format(formatoHora)

    fun parseIso(valor: String?): LocalDate? = runCatching {
        LocalDate.parse(valor?.trim(), formatoIso)
    }.getOrNull()

    fun parseVisible(valor: String?): LocalDate? = runCatching {
        LocalDate.parse(valor?.trim(), formatoVisible)
    }.getOrNull()

    fun aIso(valorVisible: String?): String? =
        parseVisible(valorVisible)?.format(formatoIso)

    fun aVisible(valorIso: String?): String =
        parseIso(valorIso)?.format(formatoVisible) ?: valorIso.orEmpty()

    fun larga(valorIso: String?): String =
        parseIso(valorIso)?.format(formatoLargo) ?: valorIso.orEmpty()

    fun edad(fechaNacimientoIso: String?): Int {
        val nacimiento = parseIso(fechaNacimientoIso) ?: return 0
        val hoy = LocalDate.now()
        if (nacimiento.isAfter(hoy)) return 0
        return Period.between(nacimiento, hoy).years
    }

    fun fechaFutura(valorIso: String?): Boolean {
        val fecha = parseIso(valorIso) ?: return false
        return fecha.isAfter(LocalDate.now())
    }

    fun horaValida(valor: String): Boolean = runCatching {
        LocalTime.parse(valor.trim(), formatoHora)
    }.isSuccess

    fun dentroDeRango(fecha: String, desde: String?, hasta: String?): Boolean {
        val valor = parseIso(fecha) ?: return false
        val inicio = parseIso(desde)
        val termino = parseIso(hasta)
        if (inicio != null && valor.isBefore(inicio)) return false
        if (termino != null && valor.isAfter(termino)) return false
        return true
    }
}
