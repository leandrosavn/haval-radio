package br.com.redesurftank.havalradio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * Coluna de volume VERTICAL encostada na direita (alcance do passageiro).
 * "+" no topo, "−" embaixo (passo de 1), e a barra é arrastável. Mostra o nível 0..volMax.
 */
@Composable
fun VolumeColumn(
    vol: Int,
    volMax: Int,
    onVolume: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val max = volMax.coerceAtLeast(1)
    Column(
        modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.35f))
            .border(1.dp, UiKit.Line, RoundedCornerShape(24.dp))
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        StepButton("+") { onVolume((vol + 1).coerceAtMost(max)) }

        BoxWithConstraints(
            Modifier.weight(1f).width(26.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, UiKit.Line, RoundedCornerShape(16.dp))
                .pointerInput(max) {
                    detectTapGestures { o -> onVolume(((1f - o.y / size.height) * max).roundToInt().coerceIn(0, max)) }
                }
                .pointerInput(max) {
                    detectVerticalDragGestures { ch, _ -> onVolume(((1f - ch.position.y / size.height) * max).roundToInt().coerceIn(0, max)) }
                },
        ) {
            val frac = (vol.toFloat() / max).coerceIn(0f, 1f)
            Box(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(maxHeight * frac)
                    .clip(RoundedCornerShape(15.dp))
                    .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))),
            )
            Box(
                Modifier.align(Alignment.BottomCenter)
                    .offset(y = -(maxHeight * frac) + 17.dp)
                    .size(34.dp).clip(CircleShape).background(Color.White)
                    .border(4.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f), CircleShape),
            )
        }

        StepButton("−") { onVolume((vol - 1).coerceAtLeast(0)) }
        Text("$vol", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text("VOLUME", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = UiKit.Muted2)
    }
}

@Composable
private fun StepButton(symbol: String, onClick: () -> Unit) {
    Box(
        Modifier.size(width = 60.dp, height = 54.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, UiKit.Line2, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, fontSize = 30.sp, fontWeight = FontWeight.Light, color = MaterialTheme.colorScheme.onBackground)
    }
}
