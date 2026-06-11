package br.com.redesurftank.havalradio

import android.app.Application
import br.com.redesurftank.havalradio.data.FavoritesStore
import br.com.redesurftank.havalradio.data.MediaCenterControl
import br.com.redesurftank.havalradio.data.ThemeStore
import org.lsposed.hiddenapibypass.HiddenApiBypass

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Libera o acesso a APIs ocultas (android.os.ServiceManager#getService) usado no bind do veículo.
        runCatching { HiddenApiBypass.addHiddenApiExemptions("") }
        FavoritesStore.init(this)
        MediaCenterControl.init(this)
        ThemeStore.init(this)
    }
}
