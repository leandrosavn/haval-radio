package br.com.redesurftank.havalradio.data

import android.content.Context

/**
 * Favoritos do app, persistidos localmente.
 *
 * Por quê local: os favoritos do rádio são privados do com.beantechs.mediacenter
 * (shared_prefs/local_radio.xml, storage device-encrypted, dono `system`) e NUNCA são
 * publicados pelo IntelligentVehicleControlService nem expostos pelo MediaBrowser do
 * com.android.car.radio (nó "Favoritos" vem vazio). Logo, um app de terceiros não consegue
 * lê-los em runtime — então o Haval Radio mantém a própria lista, gerenciada pela estrela.
 *
 * Formato: uma SharedPreferences "favorites" com duas strings CSV de kHz (band "fm"/"am").
 */
object FavoritesStore {
    private const val PREFS = "favorites"
    private const val KEY_FM = "fm"
    private const val KEY_AM = "am"

    private lateinit var appCtx: Context

    fun init(context: Context) {
        appCtx = context.applicationContext
    }

    private fun prefs() = appCtx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun key(band: Band) = if (band == Band.AM) KEY_AM else KEY_FM

    fun load(band: Band): List<Int> =
        prefs().getString(key(band), "")
            ?.split(',')
            ?.mapNotNull { it.trim().toIntOrNull() }
            ?.filter { it > 0 }
            ?: emptyList()

    private fun save(band: Band, freqs: List<Int>) {
        prefs().edit().putString(key(band), freqs.joinToString(",")).apply()
        FavoritesBridge.publish(appCtx)   // avisa o dock (botões de favorita na barra)
    }

    /** Alterna o favorito; retorna a nova lista (ordem preservada, novos no fim). */
    fun toggle(band: Band, freqKHz: Int): List<Int> {
        val list = load(band).toMutableList()
        if (!list.remove(freqKHz)) list.add(freqKHz)
        save(band, list)
        return list
    }

    /** Persiste a lista reordenada (drag-and-drop). */
    fun reorder(band: Band, freqs: List<Int>) = save(band, freqs)

    /** Remove um favorito; retorna a nova lista. */
    fun remove(band: Band, freqKHz: Int): List<Int> {
        val list = load(band).toMutableList()
        list.remove(freqKHz)
        save(band, list)
        return list
    }
}
