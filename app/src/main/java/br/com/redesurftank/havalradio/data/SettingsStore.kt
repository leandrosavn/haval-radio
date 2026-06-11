package br.com.redesurftank.havalradio.data

import android.content.Context
import androidx.compose.runtime.mutableStateOf

/**
 * Preferências gerais do app, persistidas localmente.
 *
 * - launchOnBoot: abrir o app automaticamente quando o carro liga (via [BootReceiver]).
 */
object SettingsStore {
    private const val PREFS = "settings"
    private const val KEY_BOOT = "launch_on_boot"
    private const val DEFAULT_BOOT = true

    private lateinit var appCtx: Context

    /** Observável pelo Compose (o Switch no Sobre lê/escreve). */
    val launchOnBoot = mutableStateOf(DEFAULT_BOOT)

    fun init(context: Context) {
        appCtx = context.applicationContext
        launchOnBoot.value = prefs(appCtx).getBoolean(KEY_BOOT, DEFAULT_BOOT)
    }

    fun setLaunchOnBoot(value: Boolean) {
        launchOnBoot.value = value
        prefs(appCtx).edit().putBoolean(KEY_BOOT, value).apply()
    }

    /** Leitura direta a partir de um Context — usada pelo [BootReceiver], sem depender de [init]. */
    fun isLaunchOnBoot(context: Context): Boolean =
        prefs(context).getBoolean(KEY_BOOT, DEFAULT_BOOT)

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
