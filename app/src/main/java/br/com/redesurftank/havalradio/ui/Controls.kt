package br.com.redesurftank.havalradio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.redesurftank.havalradio.data.AccentStore
import br.com.redesurftank.havalradio.data.Band
import br.com.redesurftank.havalradio.data.ThemeStore
import br.com.redesurftank.havalradio.ui.theme.DsegFontFamily
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Segmento FM/AM no topo. */
@Composable
fun BandSegment(band: Band, onBand: (Band) -> Unit) {
    Row(
        Modifier.clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, UiKit.Line, RoundedCornerShape(16.dp))
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Band.entries.forEach { b ->
            val on = band == b
            Box(
                Modifier.clip(RoundedCornerShape(12.dp))
                    .background(if (on) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else Color.Transparent)
                    .border(
                        1.dp,
                        if (on) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f) else Color.Transparent,
                        RoundedCornerShape(12.dp),
                    )
                    .clickable { onBand(b) }
                    .padding(horizontal = 28.dp, vertical = 9.dp),
            ) {
                Text(
                    b.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (on) MaterialTheme.colorScheme.onBackground else UiKit.Muted,
                )
            }
        }
    }
}

/** Pílula informativa (sinal/estéreo/conexão). */
@Composable
fun InfoPill(text: String, accent: Boolean = false) {
    Row(
        Modifier.clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, UiKit.Line, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (accent) {
            Icon(Icons.Filled.Circle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(9.dp))
            Spacer(Modifier.width(9.dp))
        }
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
    }
}

/** Botão-pílula com ícone (Salvar atual / Editar). */
@Composable
fun PillButton(icon: ImageVector, text: String, active: Boolean = false, onClick: () -> Unit) {
    Row(
        Modifier.clip(RoundedCornerShape(13.dp))
            .background(if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface)
            .border(1.dp, if (active) MaterialTheme.colorScheme.primary else UiKit.Line2, RoundedCornerShape(13.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(7.dp))
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground)
    }
}

/** Seletor de modo de tema na topbar: Claro (☀) / Escuro (🌙) / Sistema do carro (🚗). */
@Composable
fun ThemeModeSegment() {
    val mode = ThemeStore.mode.value
    val options = listOf(
        ThemeStore.Mode.LIGHT to "☀",
        ThemeStore.Mode.DARK to "🌙",
        ThemeStore.Mode.SYSTEM to "🚗",
    )
    Row(
        Modifier.clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, UiKit.Line, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        options.forEach { (m, icon) ->
            val on = m == mode
            Box(
                Modifier.clip(RoundedCornerShape(10.dp))
                    .background(if (on) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else Color.Transparent)
                    .border(
                        1.dp,
                        if (on) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f) else Color.Transparent,
                        RoundedCornerShape(10.dp),
                    )
                    .clickable { ThemeStore.set(m) }
                    .padding(horizontal = 11.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(icon, fontSize = 18.sp)
            }
        }
    }
}

/** Barra de cor de acento: um círculo por preset; o selecionado ganha anel. */
@Composable
fun AccentSwatches() {
    val sel = AccentStore.selected.value
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
        AccentStore.PRESETS.forEach { a ->
            val on = a.key == sel.key
            Box(
                Modifier.size(if (on) 26.dp else 22.dp)
                    .clip(CircleShape)
                    .background(a.primary)
                    .border(
                        if (on) 2.dp else 1.dp,
                        if (on) MaterialTheme.colorScheme.onBackground else UiKit.Line2,
                        CircleShape,
                    )
                    .clickable { AccentStore.set(a) },
            )
        }
    }
}

/** Relógio LED 7-segmentos (fonte DSEG, brilho na cor do acento) — hora do sistema. */
@Composable
fun TopClock() {
    var hhmm by remember { mutableStateOf(nowHhMm()) }
    LaunchedEffect(Unit) {
        while (true) {
            hhmm = nowHhMm()
            delay(1000)
        }
    }
    val accent = MaterialTheme.colorScheme.primary
    Box(
        Modifier.clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF070B0E))
            .border(1.dp, accent.copy(alpha = 0.30f), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            hhmm,
            style = TextStyle(
                fontFamily = DsegFontFamily,
                fontSize = 22.sp,
                color = accent,
                shadow = Shadow(color = accent.copy(alpha = 0.85f), blurRadius = 18f),
            ),
        )
    }
}

private fun nowHhMm(): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

/** Badge de temperatura (valor no acento + rótulo). Some quando o valor ainda não chegou. */
@Composable
fun TempBadge(value: Float?, label: String) {
    if (value == null) return
    Row(
        Modifier.clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, UiKit.Line, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text("${fmtTemp(value)}°", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = UiKit.Muted)
    }
}

/** Formata a temperatura mantendo a casa decimal (25.5), mas sem o ".0" supérfluo (26). */
private fun fmtTemp(v: Float): String =
    if (v == v.toLong().toFloat()) v.toLong().toString() else String.format(Locale.US, "%.1f", v)

/** Botão de versão (abre Sobre). */
@Composable
fun VersionButton(version: String, onClick: () -> Unit) {
    Row(
        Modifier.clip(RoundedCornerShape(13.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, UiKit.Line2, RoundedCornerShape(13.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
    ) {
        Text("v$version", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = UiKit.Muted)
    }
}

/** Badge pequeno (STEREO / TOCANDO / FAVORITO). */
@Composable
fun Badge(text: String, on: Boolean = false) {
    Box(
        Modifier.clip(RoundedCornerShape(10.dp))
            .background(if (on) MaterialTheme.colorScheme.primary.copy(alpha = 0.09f) else Color.Transparent)
            .border(1.dp, if (on) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f) else UiKit.Line2, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (on) MaterialTheme.colorScheme.primary else UiKit.Muted,
        )
    }
}
