package br.com.redesurftank.havalradio.data

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.mutableStateOf
import java.util.concurrent.Executors

/**
 * EQ / som do carro, via o IntelligentVehicleControlService (mesmo serviço do volume).
 *
 * Recon confirmou (HAVAL_6984): EQ de 3 bandas + campo sonoro + toggles, no namespace
 * `sys.settings.audio.*`. Leitura via fetchData; escrita via request set (igual media_volume).
 * Formatos confirmados no recon: eq_* = inteiro ("10"); sound_field = "{x,y}"; toggles = "0"/"1".
 */
object SoundController {
    private const val EQ_BASS = "sys.settings.audio.eq_bass_value"
    private const val EQ_MID = "sys.settings.audio.eq_alto_value"        // médios
    private const val EQ_TREBLE = "sys.settings.audio.eq_treble_value"
    private const val EQ_RANGE = "sys.settings.audio.eq_value_range"
    private const val FIELD = "sys.settings.audio.sound_field_value"
    private const val FIELD_RANGE = "sys.settings.audio.sound_field_value_range"
    private const val SURROUND = "sys.settings.audio.sound_surround_enable"
    private const val DTS = "sys.settings.audio.sound_effect_dts_state"
    private const val ACOUSTICS = "sys.settings.audio.system_acoustics_enable"
    private const val ANC = "sys.settings.audio.anc_enable"

    private val io = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())

    // estado observável
    val bass = mutableStateOf(0)
    val mid = mutableStateOf(0)
    val treble = mutableStateOf(0)
    val eqRange = mutableStateOf(10)
    val fieldX = mutableStateOf(0)   // balanço (esq–dir)
    val fieldY = mutableStateOf(0)   // fade (trás–frente)
    val fieldRange = mutableStateOf(10)
    val surround = mutableStateOf(false)
    val dts = mutableStateOf(false)
    val acoustics = mutableStateOf(false)
    val anc = mutableStateOf(false)
    val loaded = mutableStateOf(false)

    /** Lê todos os valores do veículo (IPC off-main). Chamar ao abrir o painel. */
    fun load() = io.execute {
        val b = readInt(EQ_BASS); val m = readInt(EQ_MID); val t = readInt(EQ_TREBLE)
        val r = readInt(EQ_RANGE)
        val f = readPair(FIELD); val fr = readPair(FIELD_RANGE)
        val sur = readBool(SURROUND); val d = readBool(DTS); val ac = readBool(ACOUSTICS); val an = readBool(ANC)
        main.post {
            b?.let { bass.value = it }; m?.let { mid.value = it }; t?.let { treble.value = it }
            r?.let { eqRange.value = it.coerceAtLeast(1) }
            f?.let { fieldX.value = it.first; fieldY.value = it.second }
            fr?.let { fieldRange.value = maxOf(it.first, it.second).coerceAtLeast(1) }
            sur?.let { surround.value = it }; d?.let { dts.value = it }
            ac?.let { acoustics.value = it }; an?.let { anc.value = it }
            loaded.value = true
        }
    }

    fun setBass(v: Int) = setEq(EQ_BASS, bass, v)
    fun setMid(v: Int) = setEq(EQ_MID, mid, v)
    fun setTreble(v: Int) = setEq(EQ_TREBLE, treble, v)

    private fun setEq(key: String, state: androidx.compose.runtime.MutableState<Int>, v: Int) {
        val c = v.coerceIn(-eqRange.value, eqRange.value)
        if (c == state.value) return
        state.value = c
        write(key, c.toString())
    }

    fun setField(x: Int, y: Int) {
        val cx = x.coerceIn(-fieldRange.value, fieldRange.value)
        val cy = y.coerceIn(-fieldRange.value, fieldRange.value)
        if (cx == fieldX.value && cy == fieldY.value) return
        fieldX.value = cx; fieldY.value = cy
        write(FIELD, "{$cx,$cy}")
    }

    fun setSurround(on: Boolean) = setBoolKey(SURROUND, surround, on)
    fun setDts(on: Boolean) = setBoolKey(DTS, dts, on)
    fun setAcoustics(on: Boolean) = setBoolKey(ACOUSTICS, acoustics, on)
    fun setAnc(on: Boolean) = setBoolKey(ANC, anc, on)

    private fun setBoolKey(key: String, state: androidx.compose.runtime.MutableState<Boolean>, on: Boolean) {
        state.value = on
        write(key, if (on) "1" else "0")
    }

    private fun write(key: String, value: String) = io.execute { VehicleClient.set(key, value) }

    private fun readInt(k: String): Int? = VehicleClient.getData(k)?.trim()?.toIntOrNull()
    private fun readBool(k: String): Boolean? = VehicleClient.getData(k)?.trim()?.let { it == "1" }
    /** "{x,y}" → par (mantém negativos). */
    private fun readPair(k: String): Pair<Int, Int>? {
        val n = VehicleClient.getData(k)?.trim()?.trim('{', '}', ' ')
            ?.split(',')?.mapNotNull { it.trim().toIntOrNull() }
        return if (n != null && n.size >= 2) n[0] to n[1] else null
    }
}
