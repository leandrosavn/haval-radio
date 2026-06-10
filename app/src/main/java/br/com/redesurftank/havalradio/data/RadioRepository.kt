package br.com.redesurftank.havalradio.data

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import com.beantechs.intelligentvehiclecontrol.sdk.IListener

/**
 * Camada de dados do rádio. Conecta no veículo via [VehicleClient], faz a leitura inicial e
 * mantém um mapa observável (Compose) das chaves sys.radio.*.
 *
 * Nesta v0 ele também funciona como MONITOR DE RECON: mostra os valores ao vivo de todas as
 * chaves de rádio, para mapearmos os formatos reais no carro antes de montar a UI final.
 */
object RadioRepository {
    private val main = Handler(Looper.getMainLooper())

    /** Valores ao vivo das chaves sys.radio.* (observável pelo Compose). */
    val values = mutableStateMapOf<String, String>()
    val connected = mutableStateOf(false)

    private val listener = object : IListener.Stub() {
        override fun onDataChanged(key: String?, value: String?) {
            if (key == null) return
            main.post { values[key] = value ?: "" }
        }
    }

    /** Conecta, lê o estado inicial e registra o listener. Chamar fora da main thread (faz IPC). */
    fun start() {
        val ok = VehicleClient.ensureConnected()
        main.post { connected.value = ok }
        if (!ok) return
        RadioKeys.ALL.forEach { k ->
            VehicleClient.getData(k)?.let { v -> main.post { values[k] = v } }
        }
        VehicleClient.registerListener(RadioKeys.ALL, listener)
    }

    fun stop() = VehicleClient.unregisterListener(listener)

    // Ações de controle — valores ainda a confirmar no recon (ver projeto FM-Radio no Obsidian).
    fun sendPlayControl(value: String) = VehicleClient.set(RadioKeys.PLAY_CONTROL_ACTION, value)
    fun toggleFavoriteCurrent(value: String) = VehicleClient.set(RadioKeys.FAVORITE_CUR_STATION_ACTION, value)
}
