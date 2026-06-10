package br.com.redesurftank.havalradio.data

/**
 * Chaves de propriedade do rádio expostas pelo IntelligentVehicleControlService (SDK Beantechs),
 * extraídas de CarConstants.java do projeto Impulse (haval-app-tool-multimidia).
 *
 * Os formatos dos valores ainda serão mapeados via recon no carro
 * (ver projeto FM-Radio no Obsidian / script Watch-HavalRadio.ps1).
 */
object RadioKeys {
    // Leitura
    const val CUR_CHANNEL_INFO = "sys.radio.cur_channel_info"
    const val RDS_CUR_CHANNEL_INFO = "sys.radio.rds_cur_channel_info"
    const val PLAY_STATE = "sys.radio.play_state"
    const val SEARCH_STATE = "sys.radio.search_state"
    const val SEARCH_PROGRESS = "sys.radio.search_progress"
    const val FM_VALID_STATION_LIST = "sys.radio.fm_valid_station_list"
    const val FM_FAVORITES_STATION_LIST = "sys.radio.fm_favorites_station_list"
    const val AM_VALID_STATION_LIST = "sys.radio.am_valid_station_list"
    const val AM_FAVORITES_STATION_LIST = "sys.radio.am_favorites_station_list"
    const val RDS_FM_VALID_STATION_LIST = "sys.radio.rds_fm_valid_station_list"
    const val RDS_FM_FAVORITE_STATION_LIST = "sys.radio.rds_fm_favorite_station_list"
    const val RDS_REGIONAL_INFO = "sys.radio.rds_regional_info"
    const val RDS_TRAFFIC_ANNOUNCEMENT_STATE = "sys.radio.rds_traffic_announcement_state"
    const val RDS_TRAFFIC_PROGRAM_STATE = "sys.radio.rds_traffic_program_state"

    // Escrita (ações) — valores a confirmar no recon
    const val PLAY_CONTROL_ACTION = "sys.radio.play_control_action"
    const val FAVORITE_CUR_STATION_ACTION = "sys.radio.favorite_cur_station_action"
    const val RDS_FAVORITE_CUR_STATION_ACTION = "sys.radio.rds_favorite_cur_station_action"

    /** Todas as chaves de leitura — usadas para registrar o listener e o monitor de recon. */
    val ALL: List<String> = listOf(
        CUR_CHANNEL_INFO, RDS_CUR_CHANNEL_INFO, PLAY_STATE, SEARCH_STATE, SEARCH_PROGRESS,
        FM_VALID_STATION_LIST, FM_FAVORITES_STATION_LIST, AM_VALID_STATION_LIST, AM_FAVORITES_STATION_LIST,
        RDS_FM_VALID_STATION_LIST, RDS_FM_FAVORITE_STATION_LIST, RDS_REGIONAL_INFO,
        RDS_TRAFFIC_ANNOUNCEMENT_STATE, RDS_TRAFFIC_PROGRAM_STATE,
    )
}
