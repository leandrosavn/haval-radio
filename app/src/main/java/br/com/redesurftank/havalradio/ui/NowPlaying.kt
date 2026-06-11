package br.com.redesurftank.havalradio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.redesurftank.havalradio.data.Band
import br.com.redesurftank.havalradio.data.Station

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
    band: Band,
    isFav: Boolean,
    muted: Boolean,
    searching: Boolean,
    progress: Int,
    onTune: (Int) -> Unit,
    onSeek: (Int) -> Unit,
    onScan: () -> Unit,
    onMute: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // cabeçalho centralizado: logo + frequência
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(30.dp)) {
            if (st != null) StationLogo(st.freqKHz, band, 128)
            Column {
                Text(
                    "${band.name} · ao vivo",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(st?.label ?: "—", fontSize = 88.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.width(11.dp))
                    Text(st?.unit ?: "", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = UiKit.Muted, modifier = Modifier.padding(bottom = 14.dp))
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    if (st?.stereo == true) Badge("STEREO", on = true)
                    if (muted) Badge("🔇 MUDO", on = true)
                    if (isFav) Badge("★ FAVORITO", on = true)
                }
            }
        }

        Spacer(Modifier.height(34.dp))

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

            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RoundButton(Icons.Filled.SkipPrevious, "Anterior", 66) { onSeek(-1) }
                MuteButton(muted, onMute)
                RoundButton(Icons.Filled.SkipNext, "Próxima", 66) { onSeek(1) }
                // Lupa/busca OCULTA até decidirmos o que fazer com ela (o scan real é inacessível
                // por app de terceiro — ver memória). A infra (onScan/startScan) segue intacta p/
                // reativar fácil quando definirmos a abordagem (soft-scan / lista do stock).
                // RoundButton(Icons.Filled.Search, "Buscar", 56) { onScan() }
            }
            if (searching) {
                Spacer(Modifier.height(6.dp))
                Text("Buscando… $progress%", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
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
private fun MuteButton(muted: Boolean, onClick: () -> Unit) {
    val bg = if (muted) {
        Brush.verticalGradient(listOf(Color(0xFFE0556A), Color(0xFFB8324A)))
    } else {
        Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
    }
    Box(
        Modifier.size(88.dp).clip(CircleShape).background(bg).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (muted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
            "Mudo",
            tint = if (muted) Color.White else MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(40.dp),
        )
    }
}
