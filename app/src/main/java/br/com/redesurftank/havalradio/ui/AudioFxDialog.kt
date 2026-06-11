package br.com.redesurftank.havalradio.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.redesurftank.havalradio.data.AudioFxProbe

/**
 * Teste do EQ nativo do Android (audiofx) na sessão global — pra ver se afeta o rádio.
 * Temporário/diagnóstico.
 */
@Composable
fun AudioFxDialog(onDismiss: () -> Unit) {
    LaunchedEffect(Unit) { AudioFxProbe.init() }
    var bassOn by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { AudioFxProbe.release(); onDismiss() },
        title = { Text("Teste EQ Android (audiofx)") },
        text = {
            Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState())) {
                Text(
                    "Com o RÁDIO TOCANDO, ligue o EQ e toque nos botões. " +
                        "Se o som mudar → o audiofx pega o rádio (EQ fino viável). Se não → o tuner bypassa o mixer.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    AudioFxProbe.info.value.ifBlank { "(sem info)" },
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    color = UiKit.Muted,
                )
                Spacer(Modifier.height(14.dp))

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("EQ global ligado", modifier = Modifier.weight(1f))
                    Switch(checked = AudioFxProbe.enabled.value, onCheckedChange = { AudioFxProbe.setEnabled(it) })
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { AudioFxProbe.boostBand(true) }) { Text("Graves no talo") }
                    Button(onClick = { AudioFxProbe.boostBand(false) }) { Text("Agudos no talo") }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { AudioFxProbe.flat() }) { Text("Plano") }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Bass Boost (máx)", modifier = Modifier.weight(1f))
                    Switch(checked = bassOn, onCheckedChange = { bassOn = it; AudioFxProbe.bassBoost(it) })
                }

                Spacer(Modifier.height(14.dp))
                Text("Status:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = UiKit.Muted)
                Text(AudioFxProbe.status.value, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            }
        },
        confirmButton = { TextButton(onClick = { AudioFxProbe.init() }) { Text("Recriar") } },
        dismissButton = { TextButton(onClick = { AudioFxProbe.release(); onDismiss() }) { Text("Fechar") } },
    )
}
