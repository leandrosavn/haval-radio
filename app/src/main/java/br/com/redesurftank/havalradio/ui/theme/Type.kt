package br.com.redesurftank.havalradio.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import br.com.redesurftank.havalradio.R

// Tipografia padrão do Material 3 por enquanto; refinar junto da UI final.
val AppTypography = Typography()

/** Fonte LED 7-segmentos (DSEG) usada no relógio da topbar. */
val DsegFontFamily = FontFamily(Font(R.font.dseg7_classic_bold, FontWeight.Bold))
