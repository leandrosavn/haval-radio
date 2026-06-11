package br.com.redesurftank.havalradio.ui

import androidx.compose.ui.graphics.Color
import br.com.redesurftank.havalradio.data.Band

/** Cores e helpers compartilhados da UI (paleta do protótipo). */
object UiKit {
    /**
     * Tema atual, definido por HavalRadioTheme ANTES de compor o conteúdo. É um var simples (não
     * snapshot) de propósito: a troca de tema já recompõe toda a árvore (o MaterialTheme muda de
     * colorScheme), então as cores abaixo são relidas com o valor novo, sem o risco de "writing to
     * state during composition".
     */
    var dark = true

    // Bordas/textos secundários: alpha sobre branco no escuro, sobre preto no claro.
    val Line get() = if (dark) Color.White.copy(alpha = 0.07f) else Color.Black.copy(alpha = 0.07f)
    val Line2 get() = if (dark) Color.White.copy(alpha = 0.13f) else Color.Black.copy(alpha = 0.12f)
    val Muted get() = if (dark) Color(0xFF9099A8) else Color(0xFF5B6573)
    val Muted2 get() = if (dark) Color(0xFF5F6776) else Color(0xFF99A1AE)

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
