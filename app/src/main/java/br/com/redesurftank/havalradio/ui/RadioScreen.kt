package br.com.redesurftank.havalradio.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.redesurftank.havalradio.data.Band
import br.com.redesurftank.havalradio.data.RadioRepository
import br.com.redesurftank.havalradio.update.UpdateManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioScreen() {
    val st = RadioRepository.station.value
    val playing = RadioRepository.playing.value
    val connected = RadioRepository.connected.value
    val band = RadioRepository.band
    val searching = RadioRepository.searching.value
    val progress = RadioRepository.searchProgress.value
    val vol = RadioRepository.volume.value
    val volMax = RadioRepository.volumeMax.value
    val favs = RadioRepository.favorites()
    var showAbout by remember { mutableStateOf(false) }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Row(Modifier.fillMaxSize().padding(20.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {

            // ---------- ESQUERDA: tocando agora + controles ----------
            Column(Modifier.weight(1.5f).fillMaxHeight()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Band.entries.forEach { b ->
                        FilterChip(
                            selected = band == b,
                            onClick = { RadioRepository.setBand(b) },
                            label = { Text(b.name) },
                            modifier = Modifier.padding(end = 8.dp),
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    AssistChip(
                        onClick = {},
                        label = { Text(if (connected) "Conectado" else "Sem conexão (Shizuku?)") },
                    )
                }

                Spacer(Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        st?.label ?: "—",
                        fontSize = 84.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        st?.unit ?: "",
                        fontSize = 26.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 14.dp),
                    )
                }
                Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Badge(band.name)
                    if (st?.stereo == true) Badge("STEREO")
                    Badge(if (playing) "TOCANDO" else "PAUSADO")
                    if (st != null && RadioRepository.isFavorite(st.freqKHz)) Badge("★ FAVORITO")
                }

                Spacer(Modifier.height(18.dp))
                TuningRuler(
                    station = st,
                    accent = MaterialTheme.colorScheme.primary,
                    onTune = { RadioRepository.tune(it) },
                )

                Spacer(Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    IconButton(onClick = { RadioRepository.seek(-1) }) {
                        Icon(Icons.Filled.SkipPrevious, "Anterior", Modifier.size(40.dp))
                    }
                    FilledIconButton(onClick = { RadioRepository.togglePlay() }, modifier = Modifier.size(72.dp)) {
                        Icon(
                            if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            "Play/Pause",
                            Modifier.size(40.dp),
                        )
                    }
                    IconButton(onClick = { RadioRepository.seek(1) }) {
                        Icon(Icons.Filled.SkipNext, "Próxima", Modifier.size(40.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { RadioRepository.startScan() }) {
                        Icon(Icons.Filled.Search, "Buscar", Modifier.size(32.dp))
                    }
                    if (searching) Text("$progress%", color = MaterialTheme.colorScheme.secondary)
                }
                if (searching) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(Modifier.height(28.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { RadioRepository.toggleMute() }) {
                        Icon(if (vol == 0) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp, "Mudo")
                    }
                    Spacer(Modifier.width(12.dp))
                    Slider(
                        value = vol.toFloat(),
                        onValueChange = { RadioRepository.setVolume(it.toInt()) },
                        valueRange = 0f..volMax.coerceAtLeast(1).toFloat(),
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("$vol", fontWeight = FontWeight.Bold)
                }
            }

            // ---------- DIREITA: favoritos ----------
            Column(Modifier.weight(1f).fillMaxHeight()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Favoritos", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { showAbout = true }) {
                        Text("v${UpdateManager.currentVersion}")
                    }
                    IconButton(onClick = { RadioRepository.favoriteCurrent() }) {
                        val isFav = st != null && RadioRepository.isFavorite(st.freqKHz)
                        Icon(if (isFav) Icons.Filled.Star else Icons.Filled.StarBorder, "Favoritar atual")
                    }
                }
                if (showAbout) AboutDialog(onDismiss = { showAbout = false })
                Spacer(Modifier.height(8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(favs) { freq ->
                        val isCurrent = st?.freqKHz == freq
                        Card(
                            onClick = { RadioRepository.tune(freq) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surface,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                freqLabel(freq, band),
                                Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Badge(text: String) {
    Text(
        text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.secondary,
    )
}

private fun freqLabel(freqKHz: Int, band: Band): String =
    if (band == Band.FM) String.format(java.util.Locale.US, "%.1f MHz", freqKHz / 1000.0)
    else "$freqKHz kHz"
