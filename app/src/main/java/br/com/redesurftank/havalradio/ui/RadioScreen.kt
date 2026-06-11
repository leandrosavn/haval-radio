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
    val connected = RadioRepository.connected.value
    val band = RadioRepository.band
    val searching = RadioRepository.searching.value
    val progress = RadioRepository.searchProgress.value
    val vol = RadioRepository.volume.value
    val volMax = RadioRepository.volumeMax.value
    val favs = RadioRepository.favorites()
    val found = RadioRepository.found()
    val isFav = st != null && RadioRepository.isFavorite(st.freqKHz)
    val muted = vol == 0

    var showAbout by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        // O SO desenha a barra de status na esquerda (~96px); o app começa depois dela.
        Column(Modifier.fillMaxSize().padding(start = 28.dp, top = 16.dp, end = 20.dp, bottom = 16.dp)) {

            // topbar: FM/AM + pílulas (Sinal não existe na central; só Estéreo é real) + versão à direita
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                BandSegment(band) { RadioRepository.setBand(it) }
                InfoPill(if (connected) "Conectado" else "Sem conexão (Shizuku?)", accent = connected)
                if (st?.stereo == true) InfoPill("Estéreo", accent = true)
                Spacer(Modifier.weight(1f))
                AccentSwatches()
                ThemeModeSegment()
                VersionButton(UpdateManager.currentVersion) { showAbout = true }
            }

            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                NowPlaying(
                    st = st,
                    band = band,
                    isFav = isFav,
                    muted = muted,
                    searching = searching,
                    progress = progress,
                    onTune = { RadioRepository.tune(it) },
                    onSeek = { RadioRepository.seek(it) },
                    onScan = { RadioRepository.startScan() },
                    onMute = { RadioRepository.toggleMute() },
                    modifier = Modifier.weight(1f),
                )
                FavoritesPanel(
                    favs = favs,
                    found = found,
                    band = band,
                    currentFreq = st?.freqKHz,
                    editing = editing,
                    onToggleEdit = { editing = !editing },
                    onSaveCurrent = { RadioRepository.favoriteCurrent() },
                    onTune = { RadioRepository.tune(it) },
                    onRemove = { RadioRepository.removeFavorite(it) },
                    modifier = Modifier.width(520.dp).fillMaxHeight(),
                )
                VolumeColumn(
                    vol = vol,
                    volMax = volMax,
                    onVolume = { RadioRepository.setVolume(it) },
                    modifier = Modifier.width(116.dp).fillMaxHeight(),
                )
            }
        }

        if (showAbout) AboutDialog(onDismiss = { showAbout = false })
    }
}
