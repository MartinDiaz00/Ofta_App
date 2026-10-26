package com.equipo7.oftaapp.model

enum class SeccionApp(val titulo: String) {
    INICIO("Inicio"),
    EXAMENES("Exámenes"),
    PACIENTES("Pacientes"),
    HISTORIAL("Historial"),
    REGISTRAR("Registrar Examen"),
    PERFIL("Perfil");

    fun visiblePara(rol: Rol): Boolean = when (rol) {
        Rol.PACIENTE -> this in setOf(INICIO, EXAMENES, HISTORIAL, PERFIL)
        Rol.TENS -> true
        Rol.OFTALMOLOGO -> this != REGISTRAR
        Rol.ADMINISTRADOR -> true
    }
}
