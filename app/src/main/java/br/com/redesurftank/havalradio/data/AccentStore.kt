package br.com.redesurftank.havalradio.data

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import android.content.Context

/**
 * Cor de acento do app (escolhida pelo usuário na barra de swatches), persistida localmente.
 *
 * Presets portados 1:1 do protótipo V2 (prototype/index-v2.html, objeto THEMES):
 * primary = accent, secondary = accent2, soft = accentSoft, onAccent = texto sobre o acento.
 */
object AccentStore {
    data class Accent(
        val key: String,
        val primary: Color,
        val secondary: Color,
        val soft: Color,
        val onAccent: Color,
    )

    val PRESETS = listOf(
        Accent("teal", Color(0xFF19E3B1), Color(0xFF0BB69A), Color(0xFF5FF0CF), Color(0xFF04140F)),
        Accent("green", Color(0xFF36D399), Color(0xFF19A877), Color(0xFF74E9BD), Color(0xFF04210F)),
        Accent("cyan", Color(0xFF22C3FF), Color(0xFF0A93CC), Color(0xFF7ADCFF), Color(0xFF04222E)),
        Accent("blue", Color(0xFF3B82F6), Color(0xFF1D5FD1), Color(0xFF8FB4FF), Color(0xFFFFFFFF)),
        Accent("violet", Color(0xFFA06BFF), Color(0xFF7B3FE4), Color(0xFFC5A3FF), Color(0xFFFFFFFF)),
        Accent("pink", Color(0xFFFF5FA2), Color(0xFFD6356F), Color(0xFFFF9CC4), Color(0xFF33091C)),
        Accent("red", Color(0xFFFF3B46), Color(0xFFC41020), Color(0xFFFF7A82), Color(0xFFFFFFFF)),
        Accent("orange", Color(0xFFFF8A3D), Color(0xFFDB6312), Color(0xFFFFB285), Color(0xFF2E1500)),
        Accent("amber", Color(0xFFFFB13C), Color(0xFFE08A1E), Color(0xFFFFD089), Color(0xFF2A1A00)),
    )

    private const val PREFS = "theme"
    private const val KEY = "accent"
    private const val DEFAULT = "teal"

    private lateinit var appCtx: Context

    /** Observável pelo Compose: HavalRadioTheme monta o colorScheme a partir disto. */
    val selected = mutableStateOf(PRESETS.first())

    fun init(context: Context) {
        appCtx = context.applicationContext
        val k = appCtx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, DEFAULT)
        selected.value = PRESETS.firstOrNull { it.key == k } ?: PRESETS.first()
    }

    fun set(accent: Accent) {
        selected.value = accent
        appCtx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, accent.key).apply()
    }
}
