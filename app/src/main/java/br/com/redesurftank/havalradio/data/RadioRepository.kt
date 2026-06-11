package br.com.redesurftank.havalradio.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.beantechs.intelligentvehiclecontrol.sdk.IListener
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

/**
 * Camada de dados do rádio: conecta no veículo via [VehicleClient], mantém o estado reativo
 * (observável pelo Compose) e expõe as ações de controle.
 *
 * Modelo confirmado por recon (ver doc FM-Radio / Recon 2026-06-10).
 */
object RadioRepository {
    private const val TAG = "HavalRadio"
    private const val RECON_TAG = "HavalRadioRecon"
    private val main = Handler(Looper.getMainLooper())
    private val io = Executors.newSingleThreadExecutor()

    // ---- estado observável ----
    val connected = mutableStateOf(false)
    val station = mutableStateOf<Station?>(null)
    val playing = mutableStateOf(false)
    val searching = mutableStateOf(false)
    val searchProgress = mutableStateOf(0)
    val volume = mutableStateOf(0)
    val volumeMax = mutableStateOf(30)
    val outsideTemp = mutableStateOf<Float?>(null)
    val insideTemp = mutableStateOf<Float?>(null)
    val favoritesFm = mutableStateListOf<Int>()
    val favoritesAm = mutableStateListOf<Int>()
    val foundFm = mutableStateListOf<Int>()
    val foundAm = mutableStateListOf<Int>()

    private val lastFreq = mutableMapOf(Band.FM to 87_900, Band.AM to 530)

    val band: Band get() = station.value?.band ?: Band.FM
    fun favorites(): List<Int> = if (band == Band.AM) favoritesAm else favoritesFm
    fun found(): List<Int> = if (band == Band.AM) foundAm else foundFm

    private val listener = object : IListener.Stub() {
        override fun onDataChanged(key: String?, value: String?) {
            if (key == null) return
            Log.i(RECON_TAG, "CHANGE $key = $value")
            main.post { apply(key, value) }
        }
    }

    /** Conecta, lê o estado inicial e registra o listener. Chamar fora da main thread (faz IPC). */
    fun start() {
        // Favoritos são locais (o veículo nunca os publica — ver [FavoritesStore]).
        main.post { loadFavorites() }
        val ok = VehicleClient.ensureConnected()
        Log.i(RECON_TAG, "conectado=$ok")
        main.post { connected.value = ok }
        if (!ok) return
        RadioKeys.ALL.forEach { k ->
            val v = VehicleClient.getData(k)
            Log.i(RECON_TAG, "INIT $k = $v")
            if (v != null) main.post { apply(k, v) }
        }
        VehicleClient.registerListener(RadioKeys.ALL, listener)

        // Auto-play: ao abrir o app, se o rádio NÃO estiver tocando, dispara a próxima favorita
        // (único caminho que reconquista o foco de áudio — ver [MediaCenterControl]). Roda uma vez
        // por sessão; o post entra DEPOIS dos apply() acima (mesmo handler, FIFO), então
        // playing.value já reflete o estado inicial lido do veículo.
        main.post {
            if (!autoPlayChecked) {
                autoPlayChecked = true
                if (!playing.value) MediaCenterControl.playNextFavorite()
            }
        }
    }

    @Volatile private var autoPlayChecked = false

    private fun loadFavorites() {
        replace(favoritesFm, FavoritesStore.load(Band.FM))
        replace(favoritesAm, FavoritesStore.load(Band.AM))
    }

    fun stop() = VehicleClient.unregisterListener(listener)

    /** Atualiza o estado a partir de uma chave/valor (sempre na main thread). */
    private fun apply(key: String, value: String?) {
        when (key) {
            RadioKeys.CUR_CHANNEL_INFO -> RadioCodec.parseChannel(value)?.let { s ->
                station.value = s
                playing.value = s.playing
                lastFreq[s.band] = s.freqKHz
            }
            RadioKeys.PLAY_STATE -> playing.value = value?.trim() == "1"
            RadioKeys.SEARCH_STATE -> searching.value = value?.trim() == "1"
            RadioKeys.SEARCH_PROGRESS -> searchProgress.value = value?.trim()?.toIntOrNull() ?: 0
            // FM_FAVORITES/AM_FAVORITES: o veículo nunca publica essas chaves — favoritos são locais.
            RadioKeys.FM_VALID -> replace(foundFm, RadioCodec.parseStationList(value))
            RadioKeys.AM_VALID -> replace(foundAm, RadioCodec.parseStationList(value))
            RadioKeys.MEDIA_VOLUME -> value?.trim()?.toIntOrNull()?.let { volume.value = it }
            RadioKeys.MEDIA_VOLUME_RANGE -> parseMax(value)?.let { volumeMax.value = it }
            RadioKeys.OUTSIDE_TEMP -> outsideTemp.value = parseTemp(value)
            RadioKeys.INSIDE_TEMP -> insideTemp.value = parseTemp(value)
        }
    }

    /** Temperatura do veículo: float °C em string (ex.: "25.5"); mantém a casa decimal. */
    private fun parseTemp(raw: String?): Float? = raw?.trim()?.toFloatOrNull()

    private fun replace(list: MutableList<Int>, values: List<Int>) {
        list.clear(); list.addAll(values)
    }

    /** O range pode vir como "30" ou "0,30"/"{0,30}". Pega o maior. */
    private fun parseMax(raw: String?): Int? =
        RadioCodec.parseStationList(raw).maxOrNull() ?: raw?.trim()?.toIntOrNull()

    // ---- ações de controle (escrevem via Beantechs) ----
    fun tune(freqKHz: Int, b: Band = band) {
        // atualização otimista da UI (o listener confirma/corrige depois)
        main.post {
            val cur = station.value
            station.value = Station(freqKHz, b, playing = cur?.playing ?: true, stereo = cur?.stereo ?: false)
        }
        io.execute { VehicleClient.set(RadioKeys.CUR_CHANNEL_INFO, RadioCodec.tuneValue(freqKHz, b)) }
    }

    fun setBand(target: Band) {
        if (target == band) return
        tune(lastFreq[target] ?: target.min, target)
    }

    fun seek(dir: Int) {
        val s = station.value ?: return
        var f = s.freqKHz + dir * s.band.step
        if (f > s.band.max) f = s.band.min
        if (f < s.band.min) f = s.band.max
        tune(f, s.band)
    }

    fun startScan() = io.execute { VehicleClient.set(RadioKeys.SEARCH_STATE, "1") }

    fun setVolume(v: Int) {
        val clamped = v.coerceIn(0, volumeMax.value)
        volume.value = clamped
        io.execute { VehicleClient.set(RadioKeys.MEDIA_VOLUME, clamped.toString()) }
    }

    private var volumeBeforeMute = 0
    fun toggleMute() {
        if (volume.value > 0) {
            volumeBeforeMute = volume.value
            setVolume(0)
        } else {
            setVolume(if (volumeBeforeMute > 0) volumeBeforeMute else (volumeMax.value / 3).coerceAtLeast(1))
        }
    }

    /** Alterna a estação atual nos favoritos locais (o veículo não persiste favoritos de 3os). */
    fun favoriteCurrent() {
        val s = station.value ?: return
        val updated = FavoritesStore.toggle(s.band, s.freqKHz)
        replace(if (s.band == Band.AM) favoritesAm else favoritesFm, updated)
    }

    /** Remove um favorito (modo editar). */
    fun removeFavorite(freqKHz: Int, b: Band = band) {
        val updated = FavoritesStore.remove(b, freqKHz)
        replace(if (b == Band.AM) favoritesAm else favoritesFm, updated)
    }

    fun isFavorite(freqKHz: Int): Boolean = favorites().contains(freqKHz)

    // ---- recon: exportar snapshot ----
    fun exportSnapshot(context: Context): String? = runCatching {
        val dir = context.getExternalFilesDir(null) ?: context.filesDir
        val ts = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val f = File(dir, "recon-$ts.txt")
        f.writeText(buildString {
            appendLine("# Haval Radio — snapshot $ts (conectado=${connected.value})")
            appendLine("station=${station.value}  playing=${playing.value}  vol=${volume.value}/${volumeMax.value}")
            appendLine("fav_fm=$favoritesFm")
            appendLine("fav_am=$favoritesAm")
        })
        Log.i(RECON_TAG, "Snapshot: ${f.absolutePath}")
        f.absolutePath
    }.onFailure { Log.e(RECON_TAG, "export falhou", it) }.getOrNull()
}
