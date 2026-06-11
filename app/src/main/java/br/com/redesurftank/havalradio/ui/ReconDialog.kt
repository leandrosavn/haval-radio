package br.com.redesurftank.havalradio.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.redesurftank.havalradio.data.ReconManager

/**
 * Diálogo de diagnóstico: lê as chaves candidatas via fetchData e mostra chave = valor.
 * Temporário — serve pra confirmar o que a central expõe (favoritos oficiais, EQ, RDS).
 */
@Composable
fun ReconDialog(onDismiss: () -> Unit) {
    LaunchedEffect(Unit) { ReconManager.run() }
    val running = ReconManager.running.value
    val results = ReconManager.results

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Diagnóstico (recon)") },
        text = {
            Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState())) {
                Text(
                    if (running) "Lendo do veículo…" else "Toque em Reler pra atualizar. Tire um print.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(Modifier.height(10.dp))
                results.forEach { (key, value) ->
                    Text(key, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = UiKit.Muted)
                    Text(
                        if (value.isNullOrBlank()) "— (vazio/null)" else value,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (value.isNullOrBlank()) UiKit.Muted2 else MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(9.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { ReconManager.run() }, enabled = !running) {
                Text(if (running) "Lendo…" else "Reler")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Fechar") } },
    )
}
