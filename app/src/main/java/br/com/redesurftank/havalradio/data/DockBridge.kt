package br.com.redesurftank.havalradio.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.compose.runtime.mutableStateOf

/**
 * Ponte com o app Haval Dock (barra inferior em overlay).
 *
 * O dock transmite [ACTION_BAR_STATE] com a altura que ocupa no rodapé sempre que mostra/esconde
 * a barra. Aqui escutamos e expomos [barHeightDp] p/ o Compose reservar o espaço dinamicamente —
 * só quando a barra está de fato ocupando o rodapé.
 *
 * Como o rádio pode abrir depois do dock, ao registrar mandamos [ACTION_REQUEST_STATE]; o dock
 * responde com o estado atual.
 */
object DockBridge {
    private const val ACTION_BAR_STATE = "br.com.redesurftank.havaldock.BAR_STATE"
    private const val ACTION_REQUEST_STATE = "br.com.redesurftank.havaldock.REQUEST_BAR_STATE"
    private const val EXTRA_HEIGHT_DP = "height_dp"

    /** Altura (dp) ocupada pela barra do dock agora: 0 = sem barra, 22 = só a alça, 84 = barra cheia. */
    val barHeightDp = mutableStateOf(0)

    private var registered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_BAR_STATE) {
                barHeightDp.value = intent.getIntExtra(EXTRA_HEIGHT_DP, 0)
            }
        }
    }

    /** Registra o listener (idempotente) e já pede o estado atual ao dock. */
    fun ensureRegistered(context: Context) {
        val app = context.applicationContext
        if (!registered) {
            val filter = IntentFilter(ACTION_BAR_STATE)
            if (Build.VERSION.SDK_INT >= 33)
                app.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
            else
                @Suppress("UnspecifiedRegisterReceiverFlag") app.registerReceiver(receiver, filter)
            registered = true
        }
        requestState(app)
    }

    /** Pede ao dock o estado atual da barra (caso ainda não tenhamos recebido nenhum broadcast). */
    fun requestState(context: Context) {
        runCatching { context.sendBroadcast(Intent(ACTION_REQUEST_STATE)) }
    }
}
