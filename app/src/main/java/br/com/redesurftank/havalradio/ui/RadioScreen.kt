package br.com.redesurftank.havalradio.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.redesurftank.havalradio.data.RadioKeys
import br.com.redesurftank.havalradio.data.RadioRepository

/**
 * Tela inicial (v0). Mostra o estado da conexão, a estação atual e um monitor ao vivo das
 * chaves sys.radio.* (recon). A UI final (favoritos, dial-régua, volume, modos/cores) será
 * portada do protótipo HTML — ver projeto FM-Radio no Obsidian.
 */
@Composable
fun RadioScreen() {
    val context = LocalContext.current
    val values = RadioRepository.values
    val connected = RadioRepository.connected.value
    val cur = values[RadioKeys.CUR_CHANNEL_INFO] ?: "—"

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(24.dp)) {
            Text("Haval Radio", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(6.dp))
            AssistChip(
                onClick = {},
                label = { Text(if (connected) "Conectado ao veículo" else "Sem conexão — Shizuku ativo?") },
            )

            Spacer(Modifier.height(20.dp))
            Text("Estação atual", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
            Text(cur, fontSize = 40.sp)

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // TODO: trocar os valores pelos reais após o recon (sys.radio.play_control_action)
                Button(onClick = { RadioRepository.sendPlayControl("seek_down") }) { Text("⏮ Anterior") }
                Button(onClick = { RadioRepository.sendPlayControl("seek_up") }) { Text("Próxima ⏭") }
                Button(onClick = {
                    val path = RadioRepository.exportSnapshot(context)
                    Toast.makeText(
                        context,
                        path?.let { "Salvo em: $it" } ?: "Falha ao exportar",
                        Toast.LENGTH_LONG,
                    ).show()
                }) { Text("Exportar recon") }
            }

            Spacer(Modifier.height(24.dp))
            Text("Monitor sys.radio.* (recon)", fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            LazyColumn(Modifier.fillMaxSize()) {
                items(RadioKeys.ALL) { key ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Text(
                            key.removePrefix("sys.radio."),
                            Modifier.weight(0.45f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                        )
                        Text(values[key] ?: "—", Modifier.weight(0.55f), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
