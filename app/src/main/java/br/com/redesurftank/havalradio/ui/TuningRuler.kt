package br.com.redesurftank.havalradio.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.redesurftank.havalradio.data.Band
import br.com.redesurftank.havalradio.data.Station
import kotlin.math.roundToInt

/**
 * Régua de sintonia fina (estilo protótipo): escala horizontal com a estação atual no centro,
 * arrastável para sintonizar. Tune é disparado ao soltar o arrasto.
 */
@Composable
fun TuningRuler(
    station: Station?,
    accent: Color,
    onTune: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val band = station?.band ?: Band.FM
    val baseFreq = station?.freqKHz ?: band.min
    var dragFreq by remember(baseFreq, band) { mutableStateOf<Int?>(null) }
    val freq = dragFreq ?: baseFreq
    val pxPerStep = with(LocalDensity.current) { 16.dp.toPx() }
    val measurer = rememberTextMeasurer()
    val majorMod = if (band == Band.FM) 1000 else 100

    Canvas(
        modifier
            .fillMaxWidth()
            .height(84.dp)
            .pointerInput(band, baseFreq) {
                var acc = 0f
                var f = baseFreq
                detectHorizontalDragGestures(
                    onDragStart = { acc = 0f; f = baseFreq },
                    onDragEnd = { onTune(f); dragFreq = null },
                    onDragCancel = { dragFreq = null },
                ) { change, dragAmount ->
                    change.consume()
                    acc += dragAmount
                    val steps = (-acc / pxPerStep).roundToInt()
                    f = (baseFreq + steps * band.step).coerceIn(band.min, band.max)
                    dragFreq = f
                }
            }
    ) {
        val centerX = size.width / 2f
        val bottom = size.height
        var fv = band.min
        while (fv <= band.max) {
            val x = centerX + ((fv - freq).toFloat() / band.step) * pxPerStep
            if (x in 0f..size.width) {
                val major = fv % majorMod == 0
                val h = if (major) 34f else 16f
                drawLine(
                    color = Color.White.copy(alpha = if (major) 0.40f else 0.16f),
                    start = Offset(x, bottom - 16f - h),
                    end = Offset(x, bottom - 16f),
                    strokeWidth = if (major) 2.5f else 1.5f,
                )
                if (major) {
                    val label = if (band == Band.FM) (fv / 1000).toString() else fv.toString()
                    val tl = measurer.measure(
                        label,
                        style = TextStyle(fontSize = 11.sp, color = Color.White.copy(alpha = 0.55f)),
                    )
                    drawText(tl, topLeft = Offset(x - tl.size.width / 2f, bottom - 16f - h - tl.size.height - 2f))
                }
            }
            fv += band.step
        }
        // agulha central
        drawLine(accent, Offset(centerX, 0f), Offset(centerX, bottom - 12f), strokeWidth = 3f)
        drawCircle(accent, radius = 7f, center = Offset(centerX, 10f))
    }
}
