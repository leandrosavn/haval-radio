package br.com.redesurftank.havalradio.data

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import androidx.compose.runtime.mutableStateOf

/**
 * Teste do EQ nativo do Android (android.media.audiofx) na SESSÃO GLOBAL (0).
 *
 * Objetivo: descobrir se o áudio do rádio passa pelo mixer do Android (AudioFlinger).
 * Se passar, um Equalizer global afeta o som do rádio → teríamos um EQ de 5+ bandas
 * (mais fino que o 3-band nativo do carro). Se não passar (tuner vai direto pro amp),
 * o efeito não muda nada no rádio.
 *
 * É só diagnóstico — afeta TODO o áudio do Android enquanto ligado; soltar ao fechar.
 */
object AudioFxProbe {
    private var eq: Equalizer? = null
    private var bass: BassBoost? = null

    val status = mutableStateOf("não iniciado")
    val info = mutableStateOf("")
    val enabled = mutableStateOf(false)

    /** Cria os efeitos na sessão global (0). Pode falhar se a central não suportar. */
    fun init() {
        release()
        runCatching {
            val e = Equalizer(0, 0).apply { enabled = false }
            eq = e
            bass = runCatching { BassBoost(0, 0).apply { enabled = false } }.getOrNull()

            val n = e.numberOfBands.toInt()
            val r = e.bandLevelRange // short[]{min,max} em milibéis (mB); dB = mB/100
            val freqs = (0 until n).joinToString(", ") { i ->
                "${e.getCenterFreq(i.toShort()) / 1000} Hz"
            }
            info.value = "Bandas: $n · faixa: ${r[0] / 100}..${r[1] / 100} dB\n" +
                "Freqs: $freqs\n" +
                "BassBoost: ${if (bass != null) "ok" else "indisponível"}"
            enabled.value = false
            status.value = "criado na sessão global (0)"
        }.onFailure {
            status.value = "ERRO ao criar: ${it.javaClass.simpleName}: ${it.message}"
            info.value = ""
        }
    }

    fun setEnabled(on: Boolean) {
        runCatching {
            eq?.enabled = on
            enabled.value = on
            status.value = if (on) "EQ global LIGADO — ouça o rádio" else "EQ global desligado"
        }.onFailure { status.value = "ERRO enable: ${it.message}" }
    }

    /** Joga a banda extrema (grave ou aguda) no máximo, pra ficar bem audível. */
    fun boostBand(low: Boolean) {
        runCatching {
            val e = eq ?: return
            if (!e.enabled) { e.enabled = true; enabled.value = true }
            val r = e.bandLevelRange
            val n = e.numberOfBands.toInt()
            val target = if (low) 0 else n - 1
            for (i in 0 until n) {
                e.setBandLevel(i.toShort(), if (i == target) r[1] else 0.toShort())
            }
            status.value = "${if (low) "GRAVE" else "AGUDO"} no máx (${r[1] / 100} dB) — mudou no rádio?"
        }.onFailure { status.value = "ERRO setBand: ${it.message}" }
    }

    fun flat() {
        runCatching {
            val e = eq ?: return
            for (i in 0 until e.numberOfBands.toInt()) e.setBandLevel(i.toShort(), 0.toShort())
            status.value = "plano (todas as bandas em 0)"
        }.onFailure { status.value = "ERRO flat: ${it.message}" }
    }

    fun bassBoost(on: Boolean) {
        runCatching {
            val b = bass ?: run { status.value = "BassBoost indisponível"; return }
            b.setStrength(if (on) 1000.toShort() else 0.toShort())
            b.enabled = on
            status.value = "BassBoost ${if (on) "ON (máx)" else "off"} — mudou no rádio?"
        }.onFailure { status.value = "ERRO bass: ${it.message}" }
    }

    fun release() {
        runCatching { eq?.release() }
        runCatching { bass?.release() }
        eq = null; bass = null; enabled.value = false
    }
}
