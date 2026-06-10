package br.com.redesurftank.havalradio.data

import java.util.Locale

/**
 * Modelo de domínio do rádio, decodificado por recon na central HAVAL_6984 (ver doc FM-Radio).
 *
 * Frequência em kHz. cur_channel_info = {freqKHz, banda, tocando, estéreo}.
 * banda: 0 = FM (passo 100 kHz), 1 = AM (passo 10 kHz).
 */
enum class Band(val code: Int, val min: Int, val max: Int, val step: Int) {
    FM(0, 87_500, 108_000, 100),
    AM(1, 530, 1_710, 10);

    companion object {
        fun from(code: Int): Band = if (code == AM.code) AM else FM
    }
}

data class Station(
    val freqKHz: Int,
    val band: Band,
    val playing: Boolean = false,
    val stereo: Boolean = false,
) {
    /** Rótulo da frequência: FM "100.9", AM "540". */
    val label: String
        get() = if (band == Band.FM) String.format(Locale.US, "%.1f", freqKHz / 1000.0)
        else freqKHz.toString()

    val unit: String get() = if (band == Band.FM) "MHz" else "kHz"
}

object RadioCodec {
    /** Parse de "{100900,0,1,0}" → Station. */
    fun parseChannel(raw: String?): Station? {
        val n = numbers(raw)
        if (n.size < 4) return null
        return Station(n[0], Band.from(n[1]), n[2] == 1, n[3] == 1)
    }

    /** Parse de "{94700,101700,98500}" → lista de kHz. */
    fun parseStationList(raw: String?): List<Int> = numbers(raw).filter { it > 0 }

    /** Valor para sintonizar: "{freqKHz,banda,0,0}". */
    fun tuneValue(freqKHz: Int, band: Band): String = "{$freqKHz,${band.code},0,0}"

    private fun numbers(raw: String?): List<Int> =
        raw?.trim()?.trim('{', '}', ' ')
            ?.split(',')
            ?.mapNotNull { it.trim().toIntOrNull() }
            ?: emptyList()
}
