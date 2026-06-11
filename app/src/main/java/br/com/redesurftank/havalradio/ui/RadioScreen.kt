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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.redesurftank.havalradio.data.RadioRepository
import br.com.redesurftank.havalradio.update.UpdateManager

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
    val found = RadioRepository.found()
    val isFav = st != null && RadioRepository.isFavorite(st.freqKHz)

    var showAbout by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        // O SO desenha a barra de status na esquerda (~96px); o app começa depois dela.
        Column(Modifier.fillMaxSize().padding(start = 28.dp, top = 16.dp, end = 24.dp, bottom = 16.dp)) {

            // topbar: FM/AM + pílulas de estado
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                BandSegment(band) { RadioRepository.setBand(it) }
                InfoPill(if (connected) "Conectado" else "Sem conexão (Shizuku?)", accent = connected)
                if (st?.stereo == true) InfoPill("Estéreo", accent = true)
            }

            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                NowPlaying(
                    st = st,
                    playing = playing,
                    band = band,
                    isFav = isFav,
                    searching = searching,
                    progress = progress,
                    vol = vol,
                    volMax = volMax,
                    onTune = { RadioRepository.tune(it) },
                    onSeek = { RadioRepository.seek(it) },
                    onPlay = { RadioRepository.togglePlay() },
                    onScan = { RadioRepository.startScan() },
                    onMute = { RadioRepository.toggleMute() },
                    onVolume = { RadioRepository.setVolume(it) },
                    modifier = Modifier.weight(1f),
                )
                FavoritesPanel(
                    favs = favs,
                    found = found,
                    band = band,
                    currentFreq = st?.freqKHz,
                    version = UpdateManager.currentVersion,
                    editing = editing,
                    onToggleEdit = { editing = !editing },
                    onSaveCurrent = { RadioRepository.favoriteCurrent() },
                    onAbout = { showAbout = true },
                    onTune = { RadioRepository.tune(it) },
                    onRemove = { RadioRepository.removeFavorite(it) },
                    modifier = Modifier.width(580.dp).fillMaxHeight(),
                )
            }
        }

        if (showAbout) AboutDialog(onDismiss = { showAbout = false })
    }
}
