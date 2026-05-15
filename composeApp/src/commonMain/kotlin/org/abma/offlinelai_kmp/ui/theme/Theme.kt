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
    val headerBorder: Color
)

private val LightExtendedColors = ExtendedColors(
    chatBackground = Color(0xFFFBFBFB),
    inputBackground = Color(0xFFF3F4F6),
    bubbleUser = Color(0xFF1E293B), // Slate 800
    bubbleAi = Color(0xFFFFFFFF),
    bubbleAiBorder = Color(0xFFE5E7EB),
    avatarAi = Color(0xFFF3F4F6),
    statusReady = EmeraldGreen,
    statusLoading = BrandIndigo,
    headerBackground = Color(0xFFFFFFFF),
    headerBorder = Color(0xFFF3F4F6)
)

private val DarkExtendedColors = ExtendedColors(
    chatBackground = Color(0xFF0F172A), // Slate 900
    inputBackground = Color(0xFF1E293B), // Slate 800
    bubbleUser = Color(0xFF3B82F6), // Blue 500
    bubbleAi = Color(0xFF1E293B),
    bubbleAiBorder = Color(0xFF334155),
    avatarAi = Color(0xFF1E293B),
    statusReady = EmeraldGreen,
    statusLoading = Color(0xFF818CF8),
    headerBackground = Color(0xFF0F172A),
    headerBorder = Color(0xFF1E293B)
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF3B82F6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E293B),
    onPrimaryContainer = Color(0xFFBFDBFE),
    secondary = Color(0xFF64748B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = Color(0xFFE2E8F0),
    tertiary = EmeraldGreen,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = Color(0xFFD1FAE5),
    error = Color(0xFFF87171),
    onError = Color.White,
    errorContainer = Color(0xFF991B1B),
    onErrorContainer = Color(0xFFFEE2E2),
    background = Color(0xFF0F172A),
    onBackground = Color.White,
    surface = Color(0xFF0F172A),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    surfaceContainerHigh = Color(0xFF1E293B),
    surfaceContainer = Color(0xFF0F172A),
    surfaceContainerLow = Color(0xFF0A0A0A),
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF334155),
    inverseSurface = Color(0xFFF8FAFC),
    inverseOnSurface = Color(0xFF0F172A)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E40AF),
    secondary = Color(0xFF475569),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1F5F9),
    onSecondaryContainer = Color(0xFF1E293B),
    tertiary = EmeraldGreen,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF064E3B),
    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),
    background = Color(0xFFFBFBFB),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = Color(0xFF475569),
    surfaceContainerHigh = Color(0xFFF3F4F6),
    surfaceContainer = Color(0xFFFBFBFB),
    surfaceContainerLow = Color(0xFFFBFBFB),
    outline = Color(0xFFD1D5DB),
    outlineVariant = Color(0xFFE5E7EB)
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
