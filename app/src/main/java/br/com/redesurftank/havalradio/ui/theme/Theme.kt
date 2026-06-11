package br.com.redesurftank.havalradio.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import br.com.redesurftank.havalradio.data.AccentStore
import br.com.redesurftank.havalradio.ui.UiKit

@Composable
fun HavalRadioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Mantém o UiKit (cores lidas como propriedades) em sincronia com o tema atual.
    UiKit.dark = darkTheme

    // O acento é dinâmico (escolhido na barra de swatches). Lê o estado observável aqui para
    // recompor quando o usuário troca de cor; primary = accent, secondary = accent2.
    val accent = AccentStore.selected.value
    val colors = if (darkTheme) {
        darkColorScheme(
            primary = accent.primary,
            onPrimary = accent.onAccent,
            secondary = accent.secondary,
            background = Bg,
            surface = Surface,
            onBackground = TextPrimary,
            onSurface = TextPrimary,
        )
    } else {
        lightColorScheme(
            primary = accent.secondary,
            onPrimary = Color.White,
            secondary = accent.primary,
            background = Color(0xFFF2F4F7),
            surface = Color(0xFFFFFFFF),
            onBackground = Color(0xFF131720),
            onSurface = Color(0xFF131720),
        )
    }
    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content,
    )
}
