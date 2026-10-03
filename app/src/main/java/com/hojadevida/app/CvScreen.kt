package com.hojadevida.app

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun PantallaHojaDeVida(
    cv: HojaDeVida,
    lista: LazyListState = rememberLazyListState(),
    // Índices ya animados: al volver a hacer scroll no se repite la entrada
    animados: MutableSet<Int> = remember { mutableSetOf() },
    onVerCertificado: (Estudio) -> Unit,
) {
    val edad = calcularEdad(cv.fechaNacimiento)

    val secciones = buildList<@Composable () -> Unit> {
        add { Seccion("Perfil profesional", Icons.Default.Person) { Text(cv.perfil, color = Ink, lineHeight = 22.sp, fontSize = 15.sp) } }
        add {
            Seccion("Datos personales", Icons.Default.AccountBox) {
                Dato("Cédula", cv.cedula)
                Dato("Fecha de nacimiento", cv.fechaNacimiento + (edad?.let { "  ($it años)" } ?: ""))
                Dato("Lugar de nacimiento", cv.lugarNacimiento)
                Dato("Residencia", cv.residencia)
            }
        }
        add { Seccion("Contacto", Icons.Default.Phone) { Contacto(cv) } }
        if (cv.experiencia.isNotEmpty()) add {
            Seccion("Experiencia laboral", Icons.Default.Star) {
                cv.experiencia.forEachIndexed { i, exp ->
                    ItemLineaTiempo(ultimo = i == cv.experiencia.lastIndex) {
                        Text(exp.cargo, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Ink)
                        Text(
                            listOf(exp.empresa, exp.periodo).filter { it.isNotBlank() }.joinToString(" · "),
                            color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(exp.descripcion, fontSize = 14.sp, color = Muted, lineHeight = 20.sp)
                    }
                }
            }
        }
        if (cv.educacion.isNotEmpty()) add {
            Seccion("Formación académica", Icons.Default.Info) {
                cv.educacion.forEachIndexed { i, est ->
                    ItemLineaTiempo(ultimo = i == cv.educacion.lastIndex) {
                        Text(est.titulo, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Ink)
                        Text(est.institucion, color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        if (est.periodo.isNotBlank()) Text(est.periodo, fontSize = 13.sp, color = Muted)
                        if (est.detalle.isNotBlank()) Text(est.detalle, fontSize = 12.sp, color = Muted)
                        if (est.certificadoPdf.isNotBlank()) {
                            Spacer(Modifier.height(10.dp))
                            BotonDorado("Ver título", Icons.Default.CheckCircle) { onVerCertificado(est) }
                        }
                    }
                }
            }
        }
        if (cv.documentos.isNotEmpty()) add {
            Seccion("Licencias y documentos", Icons.Default.CheckCircle) {
                cv.documentos.forEach {
                    Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(22.dp).clip(CircleShape).background(Gold.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center,
                        ) { Icon(Icons.Default.Check, null, tint = Gold, modifier = Modifier.size(14.dp)) }
                        Spacer(Modifier.width(12.dp))
                        Text(it, color = Ink, fontSize = 15.sp)
                    }
                }
            }
        }
        if (cv.habilidades.isNotEmpty()) add { Seccion("Habilidades", Icons.Default.Build) { Habilidades(cv.habilidades) } }
        if (cv.idiomas.isNotEmpty()) add {
            Seccion("Idiomas", Icons.Default.Face) { cv.idiomas.forEach { BarraIdioma(it) } }
        }
        if (cv.referencias.isNotEmpty()) add {
            Seccion("Referencias", Icons.Default.AccountCircle) {
                cv.referencias.forEach { Text("• $it", color = Ink, modifier = Modifier.padding(vertical = 3.dp)) }
            }
        }
    }

    Box(Modifier.fillMaxSize().background(Paper)) {
        LazyColumn(
            state = lista,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            item { Encabezado(cv, lista) }
            item { Resumen(cv, edad) }
            secciones.forEachIndexed { i, seccion ->
                item { EntradaAnimada(i, animados) { seccion() } }
            }
            item {
                Text(
                    "${cv.nombre} · Hoja de vida",
                    color = Muted.copy(alpha = 0.6f), fontSize = 12.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                )
            }
        }
        BarraSuperior(cv, lista)
    }
}

/* ---------------- Encabezado con parallax ---------------- */

@Composable
private fun Encabezado(cv: HojaDeVida, lista: LazyListState) {
    val aparicion = remember { Animatable(0f) }
    LaunchedEffect(Unit) { aparicion.animateTo(1f, tween(900, easing = FastOutSlowInEasing)) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .background(NavyGradient),
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(Gold.copy(alpha = 0.07f), radius = size.width * 0.55f, center = Offset(size.width * 1.05f, 0f))
            drawCircle(Gold.copy(alpha = 0.05f), radius = size.width * 0.35f, center = Offset(0f, size.height))
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    // Se lee dentro de graphicsLayer para no recomponer en cada scroll
                    val scroll = if (lista.firstVisibleItemIndex == 0) lista.firstVisibleItemScrollOffset.toFloat() else 1000f
                    translationY = scroll * 0.45f
                    alpha = (1f - scroll / 700f).coerceIn(0f, 1f) * aparicion.value
                }
                .padding(top = 48.dp, bottom = 64.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier
                    .size(140.dp)
                    .graphicsLayer { val s = 0.85f + 0.15f * aparicion.value; scaleX = s; scaleY = s }
                    .border(3.dp, GoldGradient, CircleShape)
                    .padding(7.dp)
            ) {
                Image(
                    painterResource(R.drawable.foto), cv.nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                cv.nombre, color = Color.White, fontSize = 26.sp, fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Box(Modifier.width(56.dp).height(2.dp).background(GoldGradient))
            Spacer(Modifier.height(10.dp))
            Text(cv.profesion, color = GoldLight, fontSize = 14.sp, textAlign = TextAlign.Center, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(cv.residencia, color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
            }
        }
    }
}

/** Barra compacta que aparece al hacer scroll. */
@Composable
private fun BarraSuperior(cv: HojaDeVida, lista: LazyListState) {
    val umbral = with(LocalDensity.current) { 260.dp.toPx() }
    val visible by remember {
        derivedStateOf { lista.firstVisibleItemIndex > 0 || lista.firstVisibleItemScrollOffset > umbral }
    }
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp)
                .background(NavyGradient)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painterResource(R.drawable.foto), null, contentScale = ContentScale.Crop,
                modifier = Modifier.size(36.dp).clip(CircleShape).border(1.5.dp, Gold, CircleShape),
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(cv.nombre, color = Color.White, fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text("Hoja de vida", color = GoldLight, fontSize = 11.sp, letterSpacing = 1.sp)
            }
        }
    }
}

/* ---------------- Resumen rápido ---------------- */

@Composable
private fun Resumen(cv: HojaDeVida, edad: Int?) {
    val datos = listOfNotNull(
        edad?.let { "$it" to "años" },
        "Tipo C" to "licencia",
        cv.residencia.substringBefore(",") to "residencia",
    )
    val entrada = remember { Animatable(0f) }
    LaunchedEffect(Unit) { delay(300); entrada.animateTo(1f, spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-32).dp)
            .padding(horizontal = 20.dp)
            .graphicsLayer { alpha = entrada.value; translationY = (1f - entrada.value) * 60f }
            .shadow(10.dp, RoundedCornerShape(20.dp), ambientColor = Navy, spotColor = Navy)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        datos.forEachIndexed { i, (valor, etiqueta) ->
            if (i > 0) Box(Modifier.width(1.dp).height(40.dp).background(Muted.copy(alpha = 0.2f)))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text(valor, color = Navy, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(etiqueta.uppercase(), color = Gold, fontSize = 10.sp, letterSpacing = 1.5.sp)
            }
        }
    }
}

/* ---------------- Bloques reutilizables ---------------- */

@Composable
private fun EntradaAnimada(indice: Int, animados: MutableSet<Int>, contenido: @Composable () -> Unit) {
    val yaAnimado = indice in animados
    val progreso = remember { Animatable(if (yaAnimado) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!yaAnimado) {
            delay(150L + 90L * indice.coerceAtMost(4))
            progreso.animateTo(1f, tween(650, easing = FastOutSlowInEasing))
            animados += indice
        }
    }
    Box(
        Modifier.graphicsLayer {
            alpha = progreso.value
            translationY = (1f - progreso.value) * 90f
            val s = 0.96f + 0.04f * progreso.value
            scaleX = s; scaleY = s
        }
    ) { contenido() }
}

@Composable
private fun Seccion(titulo: String, icono: ImageVector, contenido: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .shadow(3.dp, RoundedCornerShape(20.dp), ambientColor = Navy.copy(alpha = 0.3f), spotColor = Navy.copy(alpha = 0.3f))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .animateContentSize()
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(NavyGradient),
                contentAlignment = Alignment.Center,
            ) { Icon(icono, null, tint = GoldLight, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.width(14.dp))
            Text(titulo, fontSize = 18.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold, color = Navy)
        }
        Spacer(Modifier.height(16.dp))
        contenido()
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String) {
    if (valor.isBlank()) return
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(etiqueta.uppercase(), color = Gold, fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Medium)
        Text(valor, color = Ink, fontSize = 15.sp)
    }
}

@Composable
private fun ItemLineaTiempo(ultimo: Boolean, contenido: @Composable ColumnScope.() -> Unit) {
    Row(Modifier.height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(18.dp)) {
            Box(Modifier.padding(top = 5.dp).size(12.dp).clip(CircleShape).background(GoldGradient))
            if (!ultimo) Box(Modifier.width(2.dp).weight(1f).background(Gold.copy(alpha = 0.25f)))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.padding(bottom = if (ultimo) 0.dp else 18.dp), content = contenido)
    }
}

@Composable
private fun Contacto(cv: HojaDeVida) {
    val context = LocalContext.current
    fun abrir(uri: String) {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri))) }
    }
    val telLimpio = cv.telefono.replace(" ", "")
    // Número ecuatoriano 09XXXXXXXX -> 5939XXXXXXXX para WhatsApp
    val whatsapp = if (telLimpio.startsWith("0")) "593" + telLimpio.drop(1) else telLimpio.removePrefix("+")
    FilaContacto(Icons.Default.Phone, "Llamar", cv.telefono) { abrir("tel:$telLimpio") }
    FilaContacto(Icons.AutoMirrored.Filled.Send, "WhatsApp", cv.telefono) { abrir("https://wa.me/$whatsapp") }
    FilaContacto(Icons.Default.Email, "Correo", cv.email) { abrir("mailto:${cv.email}") }
}

@Composable
private fun FilaContacto(icono: ImageVector, etiqueta: String, valor: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .background(Paper)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icono, null, tint = Gold, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(etiqueta.uppercase(), color = Muted, fontSize = 10.sp, letterSpacing = 1.5.sp)
            Text(valor, color = Ink, fontSize = 15.sp)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Muted.copy(alpha = 0.5f))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Habilidades(habilidades: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        habilidades.forEach {
            Text(
                it,
                color = Navy,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .border(1.dp, Gold.copy(alpha = 0.5f), RoundedCornerShape(50))
                    .background(Gold.copy(alpha = 0.08f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun BarraIdioma(idioma: Idioma) {
    val progreso = remember { Animatable(0f) }
    LaunchedEffect(Unit) { delay(400); progreso.animateTo(idioma.dominio, tween(1200, easing = FastOutSlowInEasing)) }
    Column(Modifier.padding(vertical = 6.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Text(idioma.nombre, fontWeight = FontWeight.SemiBold, color = Ink, modifier = Modifier.weight(1f))
            Text(idioma.nivel, color = Muted, fontSize = 13.sp)
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(Paper)) {
            Box(Modifier.fillMaxWidth(progreso.value).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(GoldGradient))
        }
    }
}

@Composable
private fun BotonDorado(texto: String, icono: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(NavyGradient)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icono, null, tint = GoldLight, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(texto, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
    }
}
