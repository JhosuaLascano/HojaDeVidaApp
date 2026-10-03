package com.hojadevida.app

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Pantalla de bienvenida animada que se muestra al abrir la app. */
@Composable
fun PantallaIntro(cv: HojaDeVida, onTerminar: () -> Unit) {
    val fotoEscala = remember { Animatable(0.4f) }
    val fotoAlpha = remember { Animatable(0f) }
    val anillo = remember { Animatable(0f) }
    val textoAlpha = remember { Animatable(0f) }
    val textoY = remember { Animatable(40f) }
    val linea = remember { Animatable(0f) }
    val subAlpha = remember { Animatable(0f) }

    // Brillo suave que recorre el fondo
    val brillo = rememberInfiniteTransition(label = "brillo")
    val brilloX by brillo.animateFloat(
        initialValue = -0.3f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(3200, easing = LinearEasing)),
        label = "brilloX",
    )

    LaunchedEffect(Unit) {
        launch { fotoAlpha.animateTo(1f, tween(600)) }
        launch { anillo.animateTo(1f, tween(1300, delayMillis = 200, easing = FastOutSlowInEasing)) }
        fotoEscala.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow))
        launch { textoAlpha.animateTo(1f, tween(700)) }
        textoY.animateTo(0f, tween(700, easing = FastOutSlowInEasing))
        launch { linea.animateTo(1f, tween(700, easing = FastOutSlowInEasing)) }
        subAlpha.animateTo(1f, tween(600))
        delay(1100)
        onTerminar()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyGradient)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTerminar,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val x = size.width * brilloX
            drawRect(
                Brush.radialGradient(
                    listOf(Gold.copy(alpha = 0.10f), Color.Transparent),
                    center = Offset(x, size.height * 0.35f),
                    radius = size.width * 0.9f,
                )
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(184.dp)
                    .graphicsLayer {
                        scaleX = fotoEscala.value; scaleY = fotoEscala.value; alpha = fotoAlpha.value
                    },
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val grosor = 3.dp.toPx()
                    drawArc(
                        brush = GoldGradient,
                        startAngle = -90f,
                        sweepAngle = 360f * anillo.value,
                        useCenter = false,
                        topLeft = Offset(grosor / 2, grosor / 2),
                        size = Size(size.width - grosor, size.height - grosor),
                        style = Stroke(width = grosor, cap = StrokeCap.Round),
                    )
                }
                Image(
                    painter = painterResource(R.drawable.foto),
                    contentDescription = cv.nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(164.dp).clip(CircleShape),
                )
            }

            Spacer(Modifier.height(32.dp))

            Text(
                cv.nombre,
                color = Color.White,
                fontSize = 28.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .graphicsLayer { alpha = textoAlpha.value; translationY = textoY.value },
            )
            Spacer(Modifier.height(14.dp))
            Box(
                Modifier
                    .width(90.dp * linea.value)
                    .height(2.dp)
                    .background(GoldGradient)
            )
            Spacer(Modifier.height(14.dp))
            Text(
                "HOJA DE VIDA",
                color = GoldLight,
                fontSize = 13.sp,
                letterSpacing = 6.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.graphicsLayer { alpha = subAlpha.value },
            )
        }

        Text(
            "Toca para continuar",
            color = Color.White.copy(alpha = 0.35f * subAlpha.value),
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp),
        )
    }
}
