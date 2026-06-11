package br.com.redesurftank.havalradio.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import br.com.redesurftank.havalradio.ui.UiKit

private val DarkColors = darkColorScheme(
    primary = Accent,
    onPrimary = OnAccent,
    secondary = Accent2,
    background = Bg,
    surface = Surface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
)

private val LightColors = lightColorScheme(
    primary = Accent2,
    onPrimary = Color.White,
    secondary = Accent,
    background = Color(0xFFF2F4F7),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF131720),
    onSurface = Color(0xFF131720),
)

@Composable
fun HavalRadioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Mantém o UiKit (cores lidas como propriedades) em sincronia com o tema atual.
    UiKit.dark = darkTheme
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
