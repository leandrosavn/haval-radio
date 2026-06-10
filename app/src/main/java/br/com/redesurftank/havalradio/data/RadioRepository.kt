package br.com.redesurftank.havalradio.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import com.beantechs.intelligentvehiclecontrol.sdk.IListener
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Camada de dados do rádio. Conecta no veículo via [VehicleClient], faz a leitura inicial e
 * mantém um mapa observável (Compose) das chaves sys.radio.*.
 *
 * Nesta v0 ele também funciona como MONITOR DE RECON: mostra os valores ao vivo e os registra
 * em logcat (tag [RECON_TAG]) + exporta um snapshot em arquivo, para mapearmos os formatos reais
 * no carro antes de montar a UI final.
 *
 * Como capturar (do PC, via ADB):
 *   adb logcat -s HavalRadioRecon          → acompanha as mudanças ao vivo
 *   adb pull <caminho mostrado no Exportar> → baixa o snapshot .txt
 */
object RadioRepository {
    private const val RECON_TAG = "HavalRadioRecon"
    private val main = Handler(Looper.getMainLooper())

    /** Valores ao vivo das chaves sys.radio.* (observável pelo Compose). */
    val values = mutableStateMapOf<String, String>()
    val connected = mutableStateOf(false)

    private val listener = object : IListener.Stub() {
        override fun onDataChanged(key: String?, value: String?) {
            if (key == null) return
            Log.i(RECON_TAG, "CHANGE $key = $value")
            main.post { values[key] = value ?: "" }
        }
    }

    /** Conecta, lê o estado inicial e registra o listener. Chamar fora da main thread (faz IPC). */
    fun start() {
        val ok = VehicleClient.ensureConnected()
        Log.i(RECON_TAG, "conectado=$ok")
        main.post { connected.value = ok }
        if (!ok) return
        RadioKeys.ALL.forEach { k ->
            val v = VehicleClient.getData(k)
            Log.i(RECON_TAG, "INIT $k = $v")
            if (v != null) main.post { values[k] = v }
        }
        VehicleClient.registerListener(RadioKeys.ALL, listener)
    }

    fun stop() = VehicleClient.unregisterListener(listener)

    /** Grava um snapshot dos valores atuais num .txt na pasta externa do app. Retorna o caminho. */
    fun exportSnapshot(context: Context): String? = runCatching {
        val dir = context.getExternalFilesDir(null) ?: context.filesDir
        val ts = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val f = File(dir, "recon-$ts.txt")
        f.writeText(buildString {
            appendLine("# Haval Radio — recon sys.radio.* — $ts")
            appendLine("# conectado=${connected.value}")
            appendLine()
            RadioKeys.ALL.forEach { k -> appendLine("$k = ${values[k] ?: "—"}") }
        })
        Log.i(RECON_TAG, "Snapshot exportado: ${f.absolutePath}")
        f.absolutePath
    }.onFailure { Log.e(RECON_TAG, "Falha ao exportar snapshot", it) }.getOrNull()

    // Ações de controle — valores ainda a confirmar no recon (ver projeto FM-Radio no Obsidian).
    fun sendPlayControl(value: String) = VehicleClient.set(RadioKeys.PLAY_CONTROL_ACTION, value)
    fun toggleFavoriteCurrent(value: String) = VehicleClient.set(RadioKeys.FAVORITE_CUR_STATION_ACTION, value)
}
