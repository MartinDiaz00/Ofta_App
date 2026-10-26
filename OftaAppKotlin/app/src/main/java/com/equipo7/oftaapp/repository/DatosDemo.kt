package com.equipo7.oftaapp.repository

import android.content.Context
import androidx.room.withTransaction
import com.equipo7.oftaapp.data.local.AppDatabase
import com.equipo7.oftaapp.data.local.entity.DocumentoExamenEntity
import com.equipo7.oftaapp.data.local.entity.ExamenEntity
import com.equipo7.oftaapp.data.local.entity.PacienteEntity
import com.equipo7.oftaapp.data.local.entity.UsuarioEntity
import com.equipo7.oftaapp.model.EstadoExamen
import com.equipo7.oftaapp.model.Rol
import com.equipo7.oftaapp.util.Fechas
import com.equipo7.oftaapp.util.PdfReport
import com.equipo7.oftaapp.util.Seguridad
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDate

class DatosDemo(
    private val database: AppDatabase,
    private val context: Context
) {
    private val mutex = Mutex()

    suspend fun inicializar() {
        mutex.withLock {
            if (database.usuarioDao().contar() > 0) return
            withContext(Dispatchers.IO) { crearDatos() }
        }
    }

    private suspend fun crearDatos() {
        val pacientes = pacientesDemo()
        val usuarios = usuariosDemo()
        val fechaBase = LocalDate.now()
        val tipos = listOf(
            "Agudeza visual",
            "Campimetría computarizada",
            "Topografía corneal",
            "Tonometría de aplanación",
            "OCT",
            "Fondo de ojo"
        )
        val profesionales = listOf("Dra. Valentina Fernández", "Dr. Roberto Silva")
        val sucursales = listOf("Puerto Montt", "Santiago", "Valdivia")
        val ojos = listOf("Derecho", "Izquierdo", "Ambos")
        val estados = listOf(
            EstadoExamen.ENTREGADO,
            EstadoExamen.VALIDADO,
            EstadoExamen.PENDIENTE,
            EstadoExamen.OBSERVADO,
            EstadoExamen.ENTREGADO,
            EstadoExamen.VALIDADO,
            EstadoExamen.PENDIENTE,
            EstadoExamen.VALIDADO,
            EstadoExamen.ENTREGADO,
            EstadoExamen.OBSERVADO,
            EstadoExamen.VALIDADO,
            EstadoExamen.PENDIENTE,
            EstadoExamen.ENTREGADO,
            EstadoExamen.VALIDADO,
            EstadoExamen.OBSERVADO,
            EstadoExamen.ENTREGADO,
            EstadoExamen.PENDIENTE,
            EstadoExamen.VALIDADO
        )

        val examenes = estados.mapIndexed { indice, estado ->
            val numero = indice + 1
            val paciente = pacientes[indice % pacientes.size]
            val fecha = fechaBase.minusDays((indice * 3L) + 2L)
            val observacion = when (estado) {
                EstadoExamen.PENDIENTE ->
                    "Examen registrado y pendiente de revisión clínica."
                EstadoExamen.VALIDADO ->
                    "Informe revisado. Se recomienda control según indicación."
                EstadoExamen.OBSERVADO ->
                    "Se solicita repetir el examen por calidad de imagen insuficiente."
                EstadoExamen.ENTREGADO ->
                    "Informe revisado y entregado al paciente."
            }
            ExamenDemo(
                entity = ExamenEntity(
                    id = "exa-%03d".format(numero),
                    pacienteId = paciente.id,
                    tipoExamen = tipos[indice % tipos.size],
                    fecha = fecha.toString(),
                    hora = "%02d:%02d".format(9 + indice % 7, (indice % 2) * 30),
                    sucursal = sucursales[indice % sucursales.size],
                    profesional = profesionales[indice % profesionales.size],
                    ojoEvaluado = ojos[indice % ojos.size],
                    estado = estado.name,
                    observaciones = observacion,
                    creadoPorId = "usr-tens-1",
                    createdAt = System.currentTimeMillis() - ((indice * 3L) + 2L) * 86_400_000L,
                    updatedAt = System.currentTimeMillis() - (indice * 3L + 1L) * 86_400_000L
                ),
                paciente = paciente,
                estado = estado
            )
        }

        val documentos = examenes.mapNotNull { examen ->
            if (examen.estado == EstadoExamen.PENDIENTE) return@mapNotNull null
            val nombreArchivo = "informe_${examen.entity.id}.pdf"
            val archivo = PdfReport.generarDemo(
                context = context,
                nombreArchivo = nombreArchivo,
                titulo = "Informe de examen oftalmológico",
                lineas = listOf(
                    "Paciente: ${examen.paciente.nombreCompleto}",
                    "RUT: ${examen.paciente.rut}",
                    "Tipo de examen: ${examen.entity.tipoExamen}",
                    "Fecha: ${Fechas.aVisible(examen.entity.fecha)}",
                    "Hora: ${examen.entity.hora}",
                    "Sucursal: ${examen.entity.sucursal}",
                    "Profesional: ${examen.entity.profesional}",
                    "Estado: ${examen.estado.etiqueta}",
                    "",
                    "Observaciones:",
                    examen.entity.observaciones,
                    "",
                    "Este documento es ficticio y fue generado para la demostración académica."
                )
            )
            DocumentoExamenEntity(
                id = "doc-${examen.entity.id}",
                examenId = examen.entity.id,
                nombreOriginal = nombreArchivo,
                rutaInterna = "documentos/$nombreArchivo",
                mimeType = "application/pdf",
                tamanoBytes = archivo.length(),
                createdAt = examen.entity.createdAt + 3_600_000L
            )
        }

        database.withTransaction {
            pacientes.forEach { database.pacienteDao().insertar(it) }
            usuarios.forEach { database.usuarioDao().insertar(it) }
            examenes.forEach { database.examenDao().insertar(it.entity) }
            documentos.forEach { database.documentoDao().insertar(it) }
        }
    }

    private fun pacientesDemo(): List<PacienteEntity> = listOf(
        paciente(
            "pac-001", "11.111.111-1", "Camila Rojas", "1995-03-12",
            "camila.rojas@ofta-demo.test", "+56911111111", "Fonasa"
        ),
        paciente(
            "pac-002", "22.222.222-2", "Matías González", "1988-07-21",
            "matias.gonzalez@ofta-demo.test", "+56922222222", "Isapre"
        ),
        paciente(
            "pac-003", "33.333.333-3", "Valentina Soto", "2001-11-04",
            "valentina.soto@ofta-demo.test", "+56933333333", "Fonasa"
        ),
        paciente(
            "pac-004", "44.444.444-4", "Diego Herrera", "1979-01-18",
            "diego.herrera@ofta-demo.test", "+56944444444", "Particular"
        ),
        paciente(
            "pac-005", "55.555.555-5", "Antonia Muñoz", "1965-09-30",
            "antonia.munoz@ofta-demo.test", "+56955555555", "Fonasa"
        ),
        paciente(
            "pac-006", "66.666.666-6", "Sebastián Pérez", "1992-12-02",
            "sebastian.perez@ofta-demo.test", "+56966666666", "Isapre"
        ),
        paciente(
            "pac-007", "77.777.777-7", "Javiera Contreras", "2004-06-15",
            "javiera.contreras@ofta-demo.test", "+56977777777", "Fonasa"
        ),
        paciente(
            "pac-008", "88.888.888-8", "Tomás Vidal", "1958-04-09",
            "tomas.vidal@ofta-demo.test", "+56988888888", "Fonasa"
        ),
        paciente(
            "pac-009", "12.345.678-5", "Fernanda Lagos", "1983-08-27",
            "fernanda.lagos@ofta-demo.test", "+56999999999", "Isapre"
        ),
        paciente(
            "pac-010", "20.123.456-5", "Martín Araya", "2010-02-14",
            "martin.araya@ofta-demo.test", "+56900000000", "Fonasa"
        )
    )

    private fun usuariosDemo(): List<UsuarioEntity> {
        fun usuario(
            id: String,
            username: String,
            nombre: String,
            correo: String,
            rol: Rol,
            pacienteId: String? = null
        ): UsuarioEntity {
            val salt = Seguridad.nuevoSalt()
            return UsuarioEntity(
                id = id,
                username = username,
                passwordHash = Seguridad.hash("1234", salt),
                passwordSalt = salt,
                nombreCompleto = nombre,
                correo = correo,
                rol = rol.name,
                pacienteId = pacienteId
            )
        }
        return listOf(
            usuario(
                "usr-admin", "admin.ofta", "Administrador Ofta",
                "admin@ofta-demo.test", Rol.ADMINISTRADOR
            ),
            usuario(
                "usr-oft-1", "dra.fernandez", "Dra. Valentina Fernández",
                "fernandez@ofta-demo.test", Rol.OFTALMOLOGO
            ),
            usuario(
                "usr-oft-2", "dr.silva", "Dr. Roberto Silva",
                "silva@ofta-demo.test", Rol.OFTALMOLOGO
            ),
            usuario(
                "usr-tens-1", "tens.martinez", "TENS Camila Martínez",
                "martinez@ofta-demo.test", Rol.TENS
            ),
            usuario(
                "usr-tens-2", "tens.rojas", "TENS Diego Rojas",
                "rojas@ofta-demo.test", Rol.TENS
            ),
            usuario(
                "usr-pac-1", "paciente.demo", "Camila Rojas",
                "paciente.demo@ofta-demo.test", Rol.PACIENTE, "pac-001"
            )
        )
    }

    private fun paciente(
        id: String,
        rut: String,
        nombre: String,
        fechaNacimiento: String,
        correo: String,
        telefono: String,
        prevision: String
    ): PacienteEntity = PacienteEntity(
        id = id,
        rut = rut,
        nombreCompleto = nombre,
        fechaNacimiento = fechaNacimiento,
        correo = correo,
        telefonoPrincipal = telefono,
        prevision = prevision
    )

    private data class ExamenDemo(
        val entity: ExamenEntity,
        val paciente: PacienteEntity,
        val estado: EstadoExamen
    )
}
