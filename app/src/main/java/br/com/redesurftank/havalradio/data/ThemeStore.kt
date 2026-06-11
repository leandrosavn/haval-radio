package br.com.redesurftank.havalradio.data

import android.content.Context
import androidx.compose.runtime.mutableStateOf

/**
 * Preferência de tema do app, persistida localmente.
 *
 * - SYSTEM: segue o modo dia/noite da central (lido via isSystemInDarkTheme(), que reflete o
 *   `ui_night_mode` do carro — recon 2026-06-11: noturno = 2).
 * - DARK / LIGHT: força o tema independente da central.
 */
object ThemeStore {
    enum class Mode { SYSTEM, DARK, LIGHT }

    private const val PREFS = "theme"
    private const val KEY = "mode"

    private lateinit var appCtx: Context

    /** Observável pelo Compose (MainActivity reage; o seletor escreve). */
    val mode = mutableStateOf(Mode.SYSTEM)

    fun init(context: Context) {
        appCtx = context.applicationContext
        mode.value = load()
    }

    private fun load(): Mode = runCatching {
        Mode.valueOf(appCtx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, Mode.SYSTEM.name)!!)
    }.getOrDefault(Mode.SYSTEM)

    fun set(value: Mode) {
        mode.value = value
        appCtx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, value.name).apply()
    }
}
