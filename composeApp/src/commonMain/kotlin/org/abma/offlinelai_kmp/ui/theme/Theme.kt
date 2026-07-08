package org.abma.offlinelai_kmp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val BrandIndigo = Color(0xFF6366F1)
val BrandSlate = Color(0xFF475569)

val EmeraldGreen = Color(0xFF059669)
val PrimaryColor = Color(0xFF0F172A) // Slate 900
val SecondaryColor = Color(0xFF64748B) // Slate 500
val AccentColor = BrandIndigo

val GradientStart = Color(0xFF1E293B)
val GradientEnd = Color(0xFF334155)

@Immutable
data class ExtendedColors(
    val chatBackground: Color,
    val inputBackground: Color,
    val bubbleUser: Color,
    val bubbleAi: Color,
    val bubbleAiBorder: Color,
    val avatarAi: Color,
    val statusReady: Color,
    val statusLoading: Color,
    val headerBackground: Color,
    val headerBorder: Color,
    val onSurfaceVariant: Color,
    val surfaceContainerLow: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
)

private val LightExtendedColors = ExtendedColors(
    chatBackground = Color(0xFFF9F9FF),
    inputBackground = Color(0xFFF9F9FF),
    bubbleUser = Color(0xFF0058BE),
    bubbleAi = Color(0xFFE1E8FD),
    bubbleAiBorder = Color(0xFFC2C6D6),
    avatarAi = Color(0xFFF1F3FF),
    statusReady = Color(0xFF22C55E),
    statusLoading = Color(0xFF0058BE),
    headerBackground = Color(0xFFF9F9FF),
    headerBorder = Color(0xFFC2C6D6),
    onSurfaceVariant = Color(0xFF424754),
    surfaceContainerLow = Color(0xFFF1F3FF),
    surfaceContainerHigh = Color(0xFFE1E8FD),
    surfaceContainerHighest = Color(0xFFDCE2F7)
)

private val DarkExtendedColors = ExtendedColors(
    chatBackground = Color(0xFF000000),
    inputBackground = Color(0xFF000000),
    bubbleUser = Color(0xFF2563EB),
    bubbleAi = Color(0xFF1A1A1A),
    bubbleAiBorder = Color(0xFF333333),
    avatarAi = Color(0xFF1A1A1A),
    statusReady = Color(0xFF22C55E),
    statusLoading = Color(0xFF3B82F6),
    headerBackground = Color(0xFF000000),
    headerBorder = Color(0x1AFFFFFF),
    onSurfaceVariant = Color(0xFFA1A1A1),
    surfaceContainerLow = Color(0xFF1A1A1A),
    surfaceContainerHigh = Color(0xFF1A1A1A),
    surfaceContainerHighest = Color(0xFF333333)
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF3B82F6),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF2563EB),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF262626),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF262626),
    onSecondaryContainer = Color(0xFFFFFFFF),
    error = Color(0xFFEF4444),
    onError = Color(0xFFFFFFFF),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFFA1A1A1),
    surfaceContainerHigh = Color(0xFF1A1A1A),
    surfaceContainer = Color(0xFF0A0A0A),
    surfaceContainerLow = Color(0xFF1A1A1A),
    outline = Color(0xFF404040),
    outlineVariant = Color(0xFF525252),
    inverseSurface = Color(0xFF121212),
    inverseOnSurface = Color(0xFFFFFFFF)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0058BE),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF2170E4),
    onPrimaryContainer = Color(0xFFFEFCFF),
    secondary = Color(0xFF495E8A),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFB6CCFF),
    onSecondaryContainer = Color(0xFF405682),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    background = Color(0xFFF9F9FF),
    onBackground = Color(0xFF141B2B),
    surface = Color(0xFFF9F9FF),
    onSurface = Color(0xFF141B2B),
    surfaceVariant = Color(0xFFDCE2F7),
    onSurfaceVariant = Color(0xFF424754),
    surfaceContainerHigh = Color(0xFFE1E8FD),
    surfaceContainer = Color(0xFFE9EDFF),
    surfaceContainerLow = Color(0xFFF1F3FF),
    outline = Color(0xFF727785),
    outlineVariant = Color(0xFFC2C6D6)
)

private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

val LocalThemeToggle = staticCompositionLocalOf<() -> Unit> { {} }

val LocalIsDarkTheme = staticCompositionLocalOf { false }

@Composable
fun GemmaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    onToggleTheme: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors,
        LocalThemeToggle provides onToggleTheme,
        LocalIsDarkTheme provides darkTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}

val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    get() = LocalExtendedColors.current
