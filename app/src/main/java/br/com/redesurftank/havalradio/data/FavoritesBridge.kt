package br.com.redesurftank.havalradio.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Publica os favoritos do app para o Haval Dock (espelho do [DockBridge], no sentido contrário).
 *
 * O dock mantém botões de favorita anterior/próxima na barra e precisa da lista para sintonizar
 * direto (escrever cur_channel_info só funciona com som enquanto o rádio já está tocando — que é
 * exatamente quando os botões aparecem). Como SharedPreferences é privada por package, a lista
 * viaja por broadcast: [publish] manda [ACTION_FAVORITES] com os CSVs de kHz por banda sempre que
 * os favoritos mudam, e o [FavoritesRequestReceiver] (manifest, exported) responde ao pedido
 * explícito do dock mesmo com o processo do rádio morto — broadcast explícito acorda o app
 * (mesmo mecanismo do MediaCenterControl com o mediacenter).
 */
object FavoritesBridge {
    const val ACTION_FAVORITES = "br.com.redesurftank.havalradio.FAVORITES"
    const val EXTRA_FM = "fm"
    const val EXTRA_AM = "am"

    /** Manda a lista atual (CSVs de kHz) para quem escuta (dock registra receiver em runtime). */
    fun publish(context: Context) {
        runCatching {
            context.sendBroadcast(
                Intent(ACTION_FAVORITES)
                    .putExtra(EXTRA_FM, FavoritesStore.load(Band.FM).joinToString(","))
                    .putExtra(EXTRA_AM, FavoritesStore.load(Band.AM).joinToString(","))
            )
        }
    }
}

/** Alvo do pedido explícito do dock (REQUEST_FAVORITES); responde com ACTION_FAVORITES. */
class FavoritesRequestReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        FavoritesStore.init(context)   // processo pode ter acabado de nascer pelo broadcast
        FavoritesBridge.publish(context)
    }
}
