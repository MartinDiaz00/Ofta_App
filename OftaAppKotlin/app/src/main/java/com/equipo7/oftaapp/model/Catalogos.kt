package com.equipo7.oftaapp.model

object Catalogos {
    val tiposExamen = listOf(
        "Agudeza visual",
        "Campimetría computarizada",
        "Topografía corneal",
        "Tonometría de aplanación",
        "OCT",
        "Fondo de ojo"
    )

    val sucursales = listOf("Puerto Montt", "Santiago", "Valdivia")

    val ojos = listOf("Derecho", "Izquierdo", "Ambos")

    val previsiones = listOf("Fonasa", "Isapre", "Particular", "Sin especificar")

    val estados = EstadoExamen.entries
}
