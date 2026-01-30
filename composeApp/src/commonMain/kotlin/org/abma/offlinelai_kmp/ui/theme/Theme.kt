package org.abma.offlinelai_kmp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Google AI Edge Gallery inspired color palette
val GemmaBlue = Color(0xFF1A73E8)
val GemmaLightBlue = Color(0xFF8AB4F8)
val GemmaPurple = Color(0xFF9B72CB)
val GemmaGreen = Color(0xFF34A853)
val GemmaOrange = Color(0xFFEA8600)

// Gradient colors for model icons
val GradientStart = Color(0xFF4285F4)
val GradientEnd = Color(0xFF9B72CB)

private val DarkColorScheme = darkColorScheme(
    primary = GemmaLightBlue,
    onPrimary = Color(0xFF062E6F),
    primaryContainer = Color(0xFF0842A0),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = GemmaPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3D1F47),
    onSecondaryContainer = Color(0xFFF2DAFF),
    tertiary = GemmaGreen,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF1B4D2E),
    onTertiaryContainer = Color(0xFFB8F5C9),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0F0F0F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1A1A1A),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF2D2D2D),
    onSurfaceVariant = Color(0xFFCAC4D0),
    surfaceContainerHigh = Color(0xFF252525),
    surfaceContainer = Color(0xFF1F1F1F),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F)
)

private val LightColorScheme = lightColorScheme(
    primary = GemmaBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E3FD),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = GemmaPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF2DAFF),
    onSecondaryContainer = Color(0xFF2C0042),
    tertiary = GemmaGreen,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB8F5C9),
    onTertiaryContainer = Color(0xFF002111),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE8EAED),
    onSurfaceVariant = Color(0xFF5F6368),
    surfaceContainerHigh = Color(0xFFF1F3F4),
    surfaceContainer = Color(0xFFF8F9FA),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFDADCE0)
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

@Composable
fun GemmaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
