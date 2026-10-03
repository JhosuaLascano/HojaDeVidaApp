package com.hojadevida.app

// ============================================================
//  EDITA AQUÍ TU HOJA DE VIDA
//  Deja un campo en "" (vacío) o una lista vacía para ocultarlo.
// ============================================================

data class Experiencia(
    val cargo: String,
    val empresa: String,
    val periodo: String,
    val descripcion: String,
)

data class Estudio(
    val titulo: String,
    val institucion: String,
    val periodo: String,
    val detalle: String = "",
    // Nombre del PDF dentro de app/src/main/assets (vacío = sin certificado)
    val certificadoPdf: String = "",
)

// dominio: 0.0 a 1.0 (para la barra animada)
data class Idioma(val nombre: String, val nivel: String, val dominio: Float)

data class HojaDeVida(
    val nombre: String,
    val profesion: String,
    val perfil: String,
    val cedula: String,
    val fechaNacimiento: String, // formato dd/MM/yyyy
    val lugarNacimiento: String,
    val residencia: String,
    val email: String,
    val telefono: String,
    val experiencia: List<Experiencia>,
    val educacion: List<Estudio>,
    val documentos: List<String>,
    val habilidades: List<String>,
    val idiomas: List<Idioma>,
    val referencias: List<String>,
)

val miHojaDeVida = HojaDeVida(
    nombre = "Jhosua Lascano Villarreal",
    profesion = "Recepción Hotelera · Conductor Profesional",
    perfil = "Persona responsable, puntual y con vocación de servicio, con experiencia en " +
        "recepción hotelera en Lago Agrio (Sucumbíos). Habilidad para la atención al cliente, " +
        "manejo de reservas y registro de huéspedes. Bachiller en Ciencias, con licencia de " +
        "conducir tipo C profesional y tarjeta militar.",
    cedula = "1726654419",
    fechaNacimiento = "17/01/2003",
    lugarNacimiento = "Quito, Ecuador",
    residencia = "Tulcán, Carchi, Ecuador",
    email = "rogerlasxvilla@gmail.com",
    telefono = "0960260813",
    experiencia = listOf(
        Experiencia(
            cargo = "Recepcionista",
            empresa = "Hoteles de Lago Agrio, Sucumbíos",
            periodo = "",
            descripcion = "Atención y registro de huéspedes (check-in / check-out), gestión de " +
                "reservas, atención telefónica, cobros y coordinación con el personal del hotel.",
        ),
    ),
    educacion = listOf(
        Estudio(
            titulo = "Bachiller en Ciencias",
            institucion = "Unidad Educativa Particular Emanuel",
            periodo = "Graduado: 26/07/2018",
            detalle = "Refrendación Nro. ME-REF-05298096 · Ministerio de Educación del Ecuador",
            certificadoPdf = "titulo_bachiller.pdf",
        ),
    ),
    documentos = listOf(
        "Licencia de conducir tipo C (profesional)",
        "Tarjeta militar",
    ),
    habilidades = listOf(
        "Atención al cliente", "Recepción hotelera", "Manejo de reservas",
        "Caja y cobros", "Conducción profesional", "Trabajo en equipo",
        "Puntualidad", "Responsabilidad",
    ),
    idiomas = listOf(
        Idioma("Español", "Nativo", 1f),
    ),
    referencias = listOf(
        // "Nombre Apellido – Cargo – Teléfono",
    ),
)
