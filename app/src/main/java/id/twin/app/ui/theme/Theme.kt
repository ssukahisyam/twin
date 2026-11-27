package id.twin.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val TwinBlue = Color(0xFF3D6AF2)
val TwinBlueDark = Color(0xFF6B90FF)

private val LightColors = lightColorScheme(
    primary = TwinBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8EEFF),
    onPrimaryContainer = Color(0xFF102A83),
    secondary = Color(0xFF4F5B76),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEAEFF8),
    onSecondaryContainer = Color(0xFF1D2536),
    background = Color(0xFFFAFAFC),
    onBackground = Color(0xFF111318),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111318),
    surfaceVariant = Color(0xFFF1F3F9),
    onSurfaceVariant = Color(0xFF5A6070),
    outline = Color(0xFFE3E6EF),
    outlineVariant = Color(0xFFECEEF5),
    error = Color(0xFFD9383A),
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = TwinBlueDark,
    onPrimary = Color(0xFF071A52),
    primaryContainer = Color(0xFF1C3177),
    onPrimaryContainer = Color(0xFFDCE5FF),
    secondary = Color(0xFFAEC0E8),
    onSecondary = Color(0xFF19243B),
    secondaryContainer = Color(0xFF222A3D),
    onSecondaryContainer = Color(0xFFD9E2F8),
    background = Color(0xFF0B0D12),
    onBackground = Color(0xFFF1F3F9),
    surface = Color(0xFF131620),
    onSurface = Color(0xFFF1F3F9),
    surfaceVariant = Color(0xFF1B2030),
    onSurfaceVariant = Color(0xFF969EB0),
    outline = Color(0xFF282E42),
    outlineVariant = Color(0xFF1F2435),
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF420002)
)

@Composable
fun TwinTheme(
    themeMode: Int = 0,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
