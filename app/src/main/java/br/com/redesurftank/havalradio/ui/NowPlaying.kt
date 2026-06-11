package br.com.redesurftank.havalradio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.redesurftank.havalradio.data.Band
import br.com.redesurftank.havalradio.data.Station
import kotlin.math.roundToInt

@Composable
fun StationLogo(freqKHz: Int, band: Band, size: Int) {
    val c = UiKit.logoColor(freqKHz, band)
    Box(
        Modifier.size(size.dp).clip(RoundedCornerShape((size / 3.7f).dp))
            .background(Brush.verticalGradient(listOf(c, UiKit.darken(c)))),
        contentAlignment = Alignment.Center,
    ) {
        Text(UiKit.logoLabel(freqKHz, band), fontSize = (size / 3).sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun NowPlaying(
    st: Station?,
    playing: Boolean,
    band: Band,
    isFav: Boolean,
    searching: Boolean,
    progress: Int,
    vol: Int,
    volMax: Int,
    onTune: (Int) -> Unit,
    onSeek: (Int) -> Unit,
    onPlay: () -> Unit,
    onScan: () -> Unit,
    onMute: () -> Unit,
    onVolume: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxHeight()) {
        // cabeçalho: logo + frequência
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (st != null) StationLogo(st.freqKHz, band, 96)
            Spacer(Modifier.width(22.dp))
            Column {
                Text(
                    "${band.name} · ao vivo",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        st?.label ?: "—",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.width(11.dp))
                    Text(st?.unit ?: "", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = UiKit.Muted, modifier = Modifier.padding(bottom = 12.dp))
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    if (st?.stereo == true) Badge("STEREO", on = true)
                    Badge(if (playing) "TOCANDO" else "PAUSADO", on = playing)
                    if (isFav) Badge("★ FAVORITO", on = true)
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // card de sintonia
        Column(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.background.copy(alpha = 0.6f))))
                .border(1.dp, UiKit.Line, RoundedCornerShape(26.dp))
                .padding(horizontal = 22.dp, vertical = 14.dp),
        ) {
            Text("SINTONIA · arraste para sintonizar", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = UiKit.Muted2)
            Spacer(Modifier.height(6.dp))
            TuningRuler(station = st, accent = MaterialTheme.colorScheme.primary, onTune = onTune)

            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RoundButton(Icons.Filled.SkipPrevious, "Anterior", 66) { onSeek(-1) }
                PlayButton(playing, onPlay)
                RoundButton(Icons.Filled.SkipNext, "Próxima", 66) { onSeek(1) }
                RoundButton(Icons.Filled.Search, "Buscar", 56) { onScan() }
            }
            if (searching) {
                Spacer(Modifier.height(6.dp))
                Text("Buscando… $progress%", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            Spacer(Modifier.height(12.dp))
            VolumeBar(vol, volMax, onMute, onVolume)
        }
    }
}

@Composable
private fun RoundButton(icon: ImageVector, desc: String, size: Int, onClick: () -> Unit) {
    Box(
        Modifier.size(size.dp).clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, UiKit.Line, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, desc, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size((size / 2.6f).dp))
    }
}

@Composable
private fun PlayButton(playing: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(88.dp).clip(CircleShape)
            .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            "Play/Pause",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(40.dp),
        )
    }
}

@Composable
private fun VolumeBar(vol: Int, volMax: Int, onMute: () -> Unit, onVolume: (Int) -> Unit) {
    val max = volMax.coerceAtLeast(1)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(
            Modifier.size(52.dp).clip(RoundedCornerShape(15.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, UiKit.Line, RoundedCornerShape(15.dp))
                .clickable(onClick = onMute),
            contentAlignment = Alignment.Center,
        ) {
            Icon(if (vol == 0) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp, "Mudo", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(23.dp))
        }
        BoxWithConstraints(
            Modifier.weight(1f).height(16.dp).clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, UiKit.Line, RoundedCornerShape(12.dp))
                .pointerInput(max) {
                    detectTapGestures { o -> onVolume((o.x / size.width * max).roundToInt()) }
                }
                .pointerInput(max) {
                    detectHorizontalDragGestures { ch, _ -> onVolume((ch.position.x / size.width * max).roundToInt().coerceIn(0, max)) }
                },
        ) {
            val frac = (vol.toFloat() / max).coerceIn(0f, 1f)
            Box(
                Modifier.fillMaxHeight().fillMaxWidth(frac).clip(RoundedCornerShape(12.dp))
                    .background(Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary))),
            )
            Box(
                Modifier.align(Alignment.CenterStart)
                    .offset(x = (maxWidth * frac - 16.dp).coerceAtLeast(0.dp))
                    .size(32.dp).clip(CircleShape).background(Color.White)
                    .border(4.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f), CircleShape),
            )
        }
        Text("$vol", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.width(44.dp))
    }
}
