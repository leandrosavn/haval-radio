package br.com.redesurftank.havalradio.ui

import androidx.compose.ui.graphics.Color
import br.com.redesurftank.havalradio.data.Band

/** Cores e helpers compartilhados da UI (paleta do protótipo). */
object UiKit {
    val Line = Color.White.copy(alpha = 0.07f)
    val Line2 = Color.White.copy(alpha = 0.13f)
    val Muted = Color(0xFF9099A8)
    val Muted2 = Color(0xFF5F6776)

    private val LogoColors = listOf(
        Color(0xFFFF6B6B), Color(0xFFFFB13C), Color(0xFF7AA2FF), Color(0xFFFF7AB0),
        Color(0xFFB388FF), Color(0xFF5AD1FF), Color(0xFFFFD166), Color(0xFF5AE0A0),
    )

    /** Cor estável derivada da frequência (sem nome de estação, usamos a freq). */
    fun logoColor(freqKHz: Int, band: Band): Color {
        val unit = if (band == Band.FM) 1000 else 10
        val idx = ((freqKHz / unit) % LogoColors.size + LogoColors.size) % LogoColors.size
        return LogoColors[idx]
    }

    /** Versão mais escura da cor (para gradiente do logo). */
    fun darken(c: Color, amount: Float = 0.45f): Color =
        Color(
            red = (c.red * (1f - amount)).coerceIn(0f, 1f),
            green = (c.green * (1f - amount)).coerceIn(0f, 1f),
            blue = (c.blue * (1f - amount)).coerceIn(0f, 1f),
            alpha = c.alpha,
        )

    /** Rótulo curto da estação para o logo: parte inteira em FM ("94"), kHz em AM. */
    fun logoLabel(freqKHz: Int, band: Band): String =
        if (band == Band.FM) (freqKHz / 1000).toString() else freqKHz.toString()

    /** "94.7" (FM) / "540" (AM). */
    fun freqLabel(freqKHz: Int, band: Band): String =
        if (band == Band.FM) String.format(java.util.Locale.US, "%.1f", freqKHz / 1000.0)
        else freqKHz.toString()
}
