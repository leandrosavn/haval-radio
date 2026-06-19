package br.com.redesurftank.havalradio

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import br.com.redesurftank.havalradio.data.DockBridge
import br.com.redesurftank.havalradio.data.RadioRepository
import br.com.redesurftank.havalradio.data.ThemeStore
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
        setContent {
            val dark = when (ThemeStore.mode.value) {
                ThemeStore.Mode.DARK -> true
                ThemeStore.Mode.LIGHT -> false
                ThemeStore.Mode.SYSTEM -> isSystemInDarkTheme()
            }
            HavalRadioTheme(darkTheme = dark) { RadioScreen() }
        }
        requestShizukuThenConnect()
        DockBridge.ensureRegistered(this)
    }

    override fun onResume() {
        super.onResume()
        // ressincroniza com o dock ao voltar pro app (ele pode ter mudado de estado enquanto fora)
        DockBridge.requestState(this)
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
