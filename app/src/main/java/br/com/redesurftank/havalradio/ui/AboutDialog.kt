package br.com.redesurftank.havalradio.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import br.com.redesurftank.havalradio.data.SettingsStore
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

                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Auto start", modifier = Modifier.weight(1f))
                    Switch(
                        checked = SettingsStore.launchOnBoot.value,
                        onCheckedChange = { SettingsStore.setLaunchOnBoot(it) },
                    )
                }

                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Reservar rodapé (barra do Dock)", modifier = Modifier.weight(1f))
                    Switch(
                        checked = SettingsStore.reserveDockBar.value,
                        onCheckedChange = { SettingsStore.setReserveDockBar(it) },
                    )
                }

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
