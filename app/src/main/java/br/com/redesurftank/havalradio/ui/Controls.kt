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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.redesurftank.havalradio.data.Band

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
