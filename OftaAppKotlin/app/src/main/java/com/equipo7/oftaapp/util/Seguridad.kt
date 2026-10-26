package com.equipo7.oftaapp.util

import java.security.MessageDigest
import java.security.SecureRandom

object Seguridad {
    private val aleatorio = SecureRandom()

    fun nuevoSalt(): String {
        val bytes = ByteArray(16)
        aleatorio.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hash(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest("$salt:$password".toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verificar(password: String, salt: String, hashEsperado: String): Boolean {
        val calculado = hash(password, salt)
        return MessageDigest.isEqual(
            calculado.toByteArray(Charsets.UTF_8),
            hashEsperado.toByteArray(Charsets.UTF_8)
        )
    }
}
