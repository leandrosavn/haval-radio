package br.com.redesurftank.havalradio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.redesurftank.havalradio.data.SoundController
import kotlin.math.roundToInt

/** Painel de Equalizador / Som do carro (3 bandas + campo sonoro + efeitos). */
@Composable
fun EqDialog(onDismiss: () -> Unit) {
    LaunchedEffect(Unit) { SoundController.load() }
    val eqR = SoundController.eqRange.value
    val fR = SoundController.fieldRange.value

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Equalizador") },
        text = {
            Column(Modifier.heightIn(max = 470.dp).verticalScroll(rememberScrollState())) {
                EqRow("Graves", SoundController.bass.value, eqR) { SoundController.setBass(it) }
                EqRow("Médios", SoundController.mid.value, eqR) { SoundController.setMid(it) }
                EqRow("Agudos", SoundController.treble.value, eqR) { SoundController.setTreble(it) }

                Sep()
                Text("CAMPO SONORO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = UiKit.Muted2)
                EqRow("Balanço (Esq–Dir)", SoundController.fieldX.value, fR) {
                    SoundController.setField(it, SoundController.fieldY.value)
                }
                EqRow("Fade (Trás–Frente)", SoundController.fieldY.value, fR) {
                    SoundController.setField(SoundController.fieldX.value, it)
                }

                Sep()
                Text("EFEITOS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = UiKit.Muted2)
                ToggleRow("DTS", SoundController.dts.value) { SoundController.setDts(it) }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } },
    )
}

@Composable
private fun EqRow(label: String, value: Int, range: Int, onChange: (Int) -> Unit) {
    Column(Modifier.padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = 15.sp, modifier = Modifier.weight(1f))
            Text(
                (if (value > 0) "+$value" else "$value"),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.roundToInt()) },
            valueRange = -range.toFloat()..range.toFloat(),
            steps = (range * 2 - 1).coerceAtLeast(0),
        )
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun Sep() {
    Spacer(Modifier.height(14.dp))
    Box(Modifier.fillMaxWidth().height(1.dp).background(UiKit.Line2))
    Spacer(Modifier.height(8.dp))
}
