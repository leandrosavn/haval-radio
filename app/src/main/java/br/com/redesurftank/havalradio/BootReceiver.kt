package br.com.redesurftank.havalradio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import br.com.redesurftank.havalradio.data.SettingsStore

/**
 * Abre o app quando o carro liga (BOOT_COMPLETED), se o toggle "Abrir ao ligar o carro" estiver
 * habilitado (ver [SettingsStore]).
 *
 * Observação: em algumas centrais "ligar o carro" é um resume de suspensão, não um boot completo —
 * nesse caso o BOOT_COMPLETED pode não disparar. O com.beantechs.mediacenter usa um receiver de
 * BOOT_COMPLETED, então a central emite esse broadcast ao menos no boot real. Validar no carro.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        if (!SettingsStore.isLaunchOnBoot(context)) return
        runCatching {
            val launch = Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launch)
        }.onFailure { Log.e("HavalRadio", "auto-launch no boot falhou", it) }
    }
}
