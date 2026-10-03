package com.hojadevida.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

private sealed interface Pantalla {
    data object Intro : Pantalla
    data object HojaDeVida : Pantalla
    data class Certificado(val estudio: Estudio) : Pantalla
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaHojaDeVida { App(miHojaDeVida) }
        }
    }
}

@Composable
private fun App(cv: HojaDeVida) {
    var introVista by rememberSaveable { mutableStateOf(false) }
    var certificado by remember { mutableStateOf<Estudio?>(null) }
    // Se conservan al abrir y cerrar el certificado
    val lista = rememberLazyListState()
    val animados = remember { mutableSetOf<Int>() }
    val pantalla: Pantalla = when {
        !introVista -> Pantalla.Intro
        certificado != null -> Pantalla.Certificado(certificado!!)
        else -> Pantalla.HojaDeVida
    }

    BackHandler(enabled = certificado != null) { certificado = null }

    AnimatedContent(
        targetState = pantalla,
        transitionSpec = {
            val duracion = 550
            when {
                initialState is Pantalla.Intro ->
                    (fadeIn(tween(duracion)) + scaleIn(tween(duracion, easing = FastOutSlowInEasing), initialScale = 1.08f))
                        .togetherWith(fadeOut(tween(duracion)) + scaleOut(tween(duracion), targetScale = 0.92f))
                targetState is Pantalla.Certificado ->
                    (slideInHorizontally(tween(duracion, easing = FastOutSlowInEasing)) { it } + fadeIn())
                        .togetherWith(slideOutHorizontally(tween(duracion)) { -it / 3 } + fadeOut())
                else ->
                    (slideInHorizontally(tween(duracion, easing = FastOutSlowInEasing)) { -it / 3 } + fadeIn())
                        .togetherWith(slideOutHorizontally(tween(duracion)) { it } + fadeOut())
            }
        },
        contentKey = { it::class },
        label = "navegacion",
    ) { destino ->
        when (destino) {
            Pantalla.Intro -> PantallaIntro(cv) { introVista = true }
            Pantalla.HojaDeVida -> PantallaHojaDeVida(cv, lista, animados) { certificado = it }
            is Pantalla.Certificado -> PantallaCertificado(
                titulo = destino.estudio.titulo,
                archivo = destino.estudio.certificadoPdf,
                onVolver = { certificado = null },
            )
        }
    }
}
