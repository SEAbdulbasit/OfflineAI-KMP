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

val GradientIndigo = Color(0xFF6366F1)
val GradientPurple = Color(0xFF9333EA)

val EmeraldGreen = Color(0xFF10B981)
val PrimaryBlue = Color(0xFF3B82F6)
val SlateGray = Color(0xFF64748B)

val GradientStart = GradientIndigo
val GradientEnd = GradientPurple

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
    val headerBorder: Color
)

private val LightExtendedColors = ExtendedColors(
    chatBackground = Color(0xFFF8FAFC),
    inputBackground = Color(0xFFF1F5F9),
    bubbleUser = PrimaryBlue,
    bubbleAi = Color(0xFFFFFFFF),
    bubbleAiBorder = Color(0xFFF1F5F9),
    avatarAi = Color(0xFFE2E8F0),
    statusReady = EmeraldGreen,
    statusLoading = Color(0xFF6366F1),
    headerBackground = Color(0xFFFFFFFF),
    headerBorder = Color(0xFFE2E8F0)
)

private val DarkExtendedColors = ExtendedColors(
    chatBackground = Color(0xFF0A0A0A),
    inputBackground = Color(0xFF1E1E1E),
    bubbleUser = PrimaryBlue,
    bubbleAi = Color(0xFF1E1E1E),
    bubbleAiBorder = Color(0xFF2D2D2D),
    avatarAi = Color(0xFF1E1E1E),
    statusReady = EmeraldGreen,
    statusLoading = Color(0xFF818CF8),
    headerBackground = Color(0xFF121212),
    headerBorder = Color(0xFF2D2D2D)
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A5F),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = GradientPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3D1F47),
    onSecondaryContainer = Color(0xFFF2DAFF),
    tertiary = EmeraldGreen,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF1B4D2E),
    onTertiaryContainer = Color(0xFFB8F5C9),
    error = Color(0xFFCF6679),
    onError = Color.Black,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color.Black,
    onBackground = Color.White,
    surface = Color(0xFF121212),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFFCAC4D0),
    surfaceContainerHigh = Color(0xFF1E1E1E),
    surfaceContainer = Color(0xFF121212),
    surfaceContainerLow = Color(0xFF0A0A0A),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
    inverseSurface = Color(0xFFE6E1E5),
    inverseOnSurface = Color(0xFF1C1B1F)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = GradientPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E8FF),
    onSecondaryContainer = Color(0xFF581C87),
    tertiary = EmeraldGreen,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF064E3B),
    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B),
    surfaceContainerHigh = Color(0xFFF1F5F9),
    surfaceContainer = Color(0xFFF8FAFC),
    surfaceContainerLow = Color(0xFFF8FAFC),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0)
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
