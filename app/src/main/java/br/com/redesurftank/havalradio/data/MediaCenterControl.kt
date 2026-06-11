package br.com.redesurftank.havalradio.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Aciona ações do com.beantechs.mediacenter via o broadcast global de teclas (do volante).
 *
 * Recon ao vivo 2026-06-11 (central HAVAL_6984): o botão "próxima favorita" do volante
 * (keyCode 517 = `key.media.custom_short`) faz o BeanInputService disparar o broadcast
 * `android.intent.action.BEAN_GLOBAL_KEY_EVENT` (extra int `keyCode`) para o receiver
 * EXPORTED `com.beantechs.mediacenter/.mediacentermodel.CustomKeyEventReceiver`. O mediacenter
 * então chama `requestAudioFocus()` + sintoniza a próxima favorita — e o rádio TOCA COM SOM.
 *
 * Por que isto e não escrever sys.radio.play_state: escrever play_state/search_state é
 * REJEITADO para apps de terceiros ("is not support for dataId"). E só tunar (cur_channel_info)
 * não retoma o áudio, porque o foco de áudio é do mediacenter (uid system). Este broadcast é a
 * única forma de um app de terceiro fazer o rádio voltar a tocar de fato.
 *
 * Componente explícito (setComponent) é obrigatório: o Android 9 bloqueia o broadcast IMPLÍCITO
 * em background, mas o explícito para um receiver exported passa sem privilégio (sem Shizuku).
 */
object MediaCenterControl {
    private const val TAG = "HavalRadio"
    private const val ACTION = "android.intent.action.BEAN_GLOBAL_KEY_EVENT"
    private const val RECEIVER_PKG = "com.beantechs.mediacenter"
    private const val RECEIVER_CLS = "com.beantechs.mediacenter.mediacentermodel.CustomKeyEventReceiver"

    /** keyCode da tecla "próxima favorita" do volante (toca + reconquista o foco de áudio). */
    private const val KEY_MEDIA_CUSTOM_SHORT = 517

    private lateinit var appCtx: Context

    fun init(context: Context) {
        appCtx = context.applicationContext
    }

    /**
     * Toca/avança para a próxima favorita do mediacenter, com foco de áudio (= som real).
     * É o que o botão "play" do app usa, pois é o único caminho que produz áudio.
     */
    fun playNextFavorite() = send(KEY_MEDIA_CUSTOM_SHORT)

    private fun send(keyCode: Int) {
        runCatching {
            val intent = Intent(ACTION)
                .setComponent(ComponentName(RECEIVER_PKG, RECEIVER_CLS))
                .putExtra("keyCode", keyCode)
            appCtx.sendBroadcast(intent)
        }.onFailure { Log.e(TAG, "sendBroadcast keyCode=$keyCode", it) }
    }
}
