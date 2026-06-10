package br.com.redesurftank.havalradio.data

/**
 * Chaves de propriedade expostas pelo IntelligentVehicleControlService (SDK Beantechs),
 * confirmadas por recon na central HAVAL_6984. Formatos decodificados em [RadioCodec].
 */
object RadioKeys {
    // Rádio — leitura/escrita
    const val CUR_CHANNEL_INFO = "sys.radio.cur_channel_info"            // {freqKHz,banda,play,stereo}
    const val PLAY_STATE = "sys.radio.play_state"                        // 0/1
    const val SEARCH_STATE = "sys.radio.search_state"                    // 0/1
    const val SEARCH_PROGRESS = "sys.radio.search_progress"              // 0..100
    const val FM_FAVORITES = "sys.radio.fm_favorites_station_list"       // {kHz,...}
    const val AM_FAVORITES = "sys.radio.am_favorites_station_list"
    const val FM_VALID = "sys.radio.fm_valid_station_list"               // encontradas no scan
    const val AM_VALID = "sys.radio.am_valid_station_list"
    const val FAVORITE_ACTION = "sys.radio.favorite_cur_station_action"  // favoritar atual (a confirmar)

    // Volume de mídia (mesmo serviço Beantechs; já usado pelo Impulse)
    const val MEDIA_VOLUME = "sys.settings.audio.media_volume"
    const val MEDIA_VOLUME_RANGE = "sys.settings.audio.media_volume_range"

    /** Chaves monitoradas (listener + leitura inicial). */
    val ALL: List<String> = listOf(
        CUR_CHANNEL_INFO, PLAY_STATE, SEARCH_STATE, SEARCH_PROGRESS,
        FM_FAVORITES, AM_FAVORITES, FM_VALID, AM_VALID,
        MEDIA_VOLUME, MEDIA_VOLUME_RANGE,
    )
}
