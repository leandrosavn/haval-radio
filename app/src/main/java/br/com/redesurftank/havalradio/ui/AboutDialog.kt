package br.com.redesurftank.havalradio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.redesurftank.havalradio.data.ThemeStore
import br.com.redesurftank.havalradio.update.UpdateManager

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val checking = UpdateManager.checking.value
    val downloading = UpdateManager.downloading.value
    val progress = UpdateManager.progress.value
    val available = UpdateManager.available.value
    val message = UpdateManager.message.value

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Haval Radio") },
        text = {
            Column {
                Text("Versão atual: ${UpdateManager.currentVersion}")

                Spacer(Modifier.height(14.dp))
                Text("Tema", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                ThemeSelector()

                if (message != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(message, color = MaterialTheme.colorScheme.secondary)
                }
                if (downloading) {
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text("$progress%")
                }
            }
        },
        confirmButton = {
            if (available != null && !downloading) {
                TextButton(onClick = { UpdateManager.downloadAndInstall(context) }) {
                    Text("Baixar e instalar")
                }
            } else {
                TextButton(onClick = { UpdateManager.checkForUpdate() }, enabled = !checking && !downloading) {
                    Text(if (checking) "Verificando…" else "Verificar atualização")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Fechar") }
        },
    )
}

/** Segmento Claro / Escuro / Sistema. "Sistema" segue o modo dia/noite da central. */
@Composable
private fun ThemeSelector() {
    val current = ThemeStore.mode.value
    val options = listOf(
        ThemeStore.Mode.LIGHT to "Claro",
        ThemeStore.Mode.DARK to "Escuro",
        ThemeStore.Mode.SYSTEM to "Sistema",
    )
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, UiKit.Line2, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { (mode, label) ->
            val on = mode == current
            Box(
                Modifier.weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (on) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else Color.Transparent)
                    .border(
                        1.dp,
                        if (on) MaterialTheme.colorScheme.primary.copy(alpha = 0.55f) else Color.Transparent,
                        RoundedCornerShape(11.dp),
                    )
                    .clickable { ThemeStore.set(mode) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (on) MaterialTheme.colorScheme.primary else UiKit.Muted,
                )
            }
        }
    }
}
