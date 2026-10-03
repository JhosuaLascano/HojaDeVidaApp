package com.hojadevida.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import java.util.Calendar

val Navy = Color(0xFF0F1C2E)
val Navy2 = Color(0xFF1C3150)
val Gold = Color(0xFFC9A45C)
val GoldLight = Color(0xFFE6CE96)
val Paper = Color(0xFFF5F3EE)
val Ink = Color(0xFF1B1F27)
val Muted = Color(0xFF6B7280)

val NavyGradient = Brush.verticalGradient(listOf(Navy, Navy2))
val GoldGradient = Brush.linearGradient(listOf(GoldLight, Gold, Color(0xFFA8843F)))

@Composable
fun TemaHojaDeVida(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Navy,
            secondary = Gold,
            background = Paper,
            surface = Color.White,
            onSurface = Ink,
        ),
        content = content,
    )
}

/** Edad a partir de "dd/MM/yyyy". */
fun calcularEdad(fecha: String): Int? = runCatching {
    val (d, m, a) = fecha.split("/").map { it.trim().toInt() }
    val hoy = Calendar.getInstance()
    var edad = hoy.get(Calendar.YEAR) - a
    val mesHoy = hoy.get(Calendar.MONTH) + 1
    if (mesHoy < m || (mesHoy == m && hoy.get(Calendar.DAY_OF_MONTH) < d)) edad--
    edad
}.getOrNull()
