package br.com.redesurftank.havalradio.data

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import java.util.concurrent.Executors

/**
 * Diagnóstico temporário: lê um conjunto de chaves candidatas direto do
 * IntelligentVehicleControlService (via [VehicleClient.getData]) e expõe os valores pra UI,
 * sem depender de logcat (que o build de release stripa).
 *
 * Objetivo desta rodada: confirmar se a central expõe, por essa via:
 * - favoritos oficiais do rádio (pra um futuro "Sincronizar do rádio oficial");
 * - parâmetros de EQ/som (pra um futuro painel de equalizador);
 * - RDS.
 */
object ReconManager {
    /** Chaves candidatas a investigar. */
    val KEYS: List<String> = listOf(
        // favoritos oficiais (mediacenter) — ver se fetchData devolve
        "sys.radio.fm_favorites_station_list",
        "sys.radio.am_favorites_station_list",
        "sys.radio.fm_valid_station_list",
        "sys.radio.rds_cur_channel_info",
        // EQ / som (chaves do CarConstants do Impulse)
        "sys.settings.audio.eq_bass_value",
        "sys.settings.audio.eq_alto_value",
        "sys.settings.audio.eq_treble_value",
        "sys.settings.audio.eq_value_range",
        "sys.settings.audio.sound_field_value",
        "sys.settings.audio.sound_field_value_range",
        "sys.settings.audio.sound_surround_enable",
        "sys.settings.audio.sound_effect_dts_state",
        "sys.settings.audio.sound_quality_restore_enable",
        "sys.settings.audio.system_acoustics_enable",
        "sys.settings.audio.anc_enable",
    )

    private val io = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())

    /** Pares chave→valor lidos (observável pela UI). */
    val results = mutableStateListOf<Pair<String, String?>>()
    val running = mutableStateOf(false)

    /** Lê todas as chaves (IPC off-main) e publica os resultados na main thread. */
    fun run() {
        if (running.value) return
        running.value = true
        io.execute {
            val out = KEYS.map { it to VehicleClient.getData(it) }
            main.post {
                results.clear()
                results.addAll(out)
                running.value = false
            }
        }
    }
}
