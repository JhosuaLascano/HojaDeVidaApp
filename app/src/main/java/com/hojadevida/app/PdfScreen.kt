package com.hojadevida.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Visor del certificado PDF incluido en la app (con zoom por pellizco). */
@Composable
fun PantallaCertificado(titulo: String, archivo: String, onVolver: () -> Unit) {
    val context = LocalContext.current
    val paginas by produceState<List<Bitmap>?>(null, archivo) {
        value = withContext(Dispatchers.IO) { runCatching { renderizarPdf(context, archivo) }.getOrDefault(emptyList()) }
    }
    var escala by remember { mutableFloatStateOf(1f) }
    var desplazamiento by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

    Column(Modifier.fillMaxSize().background(Paper)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyGradient)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
            Column {
                Text("CERTIFICADO", color = GoldLight, fontSize = 11.sp, letterSpacing = 3.sp)
                Text(titulo, color = Color.White, fontSize = 18.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold)
            }
        }

        when {
            paginas == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Gold)
            }
            paginas!!.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No se pudo abrir el certificado", color = Muted)
            }
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            escala = (escala * zoom).coerceIn(1f, 4f)
                            desplazamiento = if (escala == 1f) androidx.compose.ui.geometry.Offset.Zero else desplazamiento + pan
                        }
                    }
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                paginas!!.forEach { bmp ->
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = titulo,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = escala; scaleY = escala
                                translationX = desplazamiento.x; translationY = desplazamiento.y
                            }
                            .shadow(8.dp, RoundedCornerShape(6.dp))
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White),
                    )
                }
                Text(
                    "Pellizca para hacer zoom",
                    color = Muted, fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
    }
}

private fun renderizarPdf(context: Context, archivo: String): List<Bitmap> {
    val copia = File(context.cacheDir, archivo)
    context.assets.open(archivo).use { entrada -> copia.outputStream().use { entrada.copyTo(it) } }
    return ParcelFileDescriptor.open(copia, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
        PdfRenderer(pfd).use { renderer ->
            (0 until renderer.pageCount).map { i ->
                renderer.openPage(i).use { pagina ->
                    val factor = 2
                    val bmp = Bitmap.createBitmap(pagina.width * factor, pagina.height * factor, Bitmap.Config.ARGB_8888)
                    bmp.eraseColor(android.graphics.Color.WHITE)
                    pagina.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bmp
                }
            }
        }
    }
}
