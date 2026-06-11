package br.com.redesurftank.havalradio

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import br.com.redesurftank.havalradio.data.RadioBrowser
import br.com.redesurftank.havalradio.data.RadioRepository
import br.com.redesurftank.havalradio.ui.RadioScreen
import br.com.redesurftank.havalradio.ui.theme.HavalRadioTheme
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {

    private val shizukuReq = 1001
    private val permListener =
        Shizuku.OnRequestPermissionResultListener { _, result ->
            if (result == PackageManager.PERMISSION_GRANTED) connect()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Shizuku.addRequestPermissionResultListener(permListener)
        setContent { HavalRadioTheme { RadioScreen() } }
        requestShizukuThenConnect()
        // SONDA temporária: mapeia a árvore do MediaBrowser do rádio AOSP (favoritos/transport).
        RadioBrowser.probe(this)
    }

    private fun requestShizukuThenConnect() = runCatching {
        if (!Shizuku.pingBinder()) return@runCatching
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) connect()
        else Shizuku.requestPermission(shizukuReq)
    }

    private fun connect() = Thread { RadioRepository.start() }.start()

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(permListener)
        RadioRepository.stop()
    }
}
