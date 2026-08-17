package br.com.redesurftank.havalradio

import android.app.Application
import br.com.redesurftank.havalradio.data.AccentStore
import br.com.redesurftank.havalradio.data.FavoritesBridge
import br.com.redesurftank.havalradio.data.FavoritesStore
import br.com.redesurftank.havalradio.data.MediaCenterControl
import br.com.redesurftank.havalradio.data.SettingsStore
import br.com.redesurftank.havalradio.data.ThemeStore
import br.com.redesurftank.havalradio.data.VehicleClient
import org.lsposed.hiddenapibypass.HiddenApiBypass

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Libera o acesso a APIs ocultas (android.os.ServiceManager#getService) usado no bind do veículo.
        runCatching { HiddenApiBypass.addHiddenApiExemptions("") }
        FavoritesStore.init(this)
        FavoritesBridge.publish(this)   // sincroniza o dock sempre que o app sobe
        MediaCenterControl.init(this)
        ThemeStore.init(this)
        AccentStore.init(this)
        SettingsStore.init(this)
        // Instala os hooks do Shizuku p/ (re)conectar ao veículo assim que ele subir no boot.
        VehicleClient.init()
    }
}
