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

// ==================== HTML Design Color Palette ====================

// Gradient colors (matching HTML: from-indigo-500 to-purple-600)
val GradientIndigo = Color(0xFF6366F1)  // indigo-500
val GradientPurple = Color(0xFF9333EA)  // purple-600

// Accent colors
val EmeraldGreen = Color(0xFF10B981)    // emerald-500 - for "Ready" status
val PrimaryBlue = Color(0xFF3B82F6)     // blue-500 - primary action color
val SlateGray = Color(0xFF64748B)       // slate-500 - muted text

// Legacy gradient colors (keep for compatibility)
val GradientStart = GradientIndigo
val GradientEnd = GradientPurple

// ==================== Extended Color Scheme ====================

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

// Light theme extended colors
private val LightExtendedColors = ExtendedColors(
    chatBackground = Color(0xFFF8FAFC),      // slate-50
    inputBackground = Color(0xFFF1F5F9),     // slate-100
    bubbleUser = PrimaryBlue,                 // blue-500
    bubbleAi = Color(0xFFFFFFFF),            // white
    bubbleAiBorder = Color(0xFFF1F5F9),      // slate-100
    avatarAi = Color(0xFFE2E8F0),            // slate-200
    statusReady = EmeraldGreen,               // emerald-500
    statusLoading = Color(0xFF6366F1),       // indigo-500
    headerBackground = Color(0xFFFFFFFF),    // white
    headerBorder = Color(0xFFE2E8F0)         // slate-200
)

// Dark theme extended colors
private val DarkExtendedColors = ExtendedColors(
    chatBackground = Color(0xFF020617),      // slate-950
    inputBackground = Color(0xFF1E293B),     // slate-800
    bubbleUser = PrimaryBlue,                 // blue-500
    bubbleAi = Color(0xFF1E293B),            // slate-800
    bubbleAiBorder = Color(0xFF334155),      // slate-700
    avatarAi = Color(0xFF1E293B),            // slate-800
    statusReady = EmeraldGreen,               // emerald-500
    statusLoading = Color(0xFF818CF8),       // indigo-400
    headerBackground = Color(0xFF0F172A),    // slate-900
    headerBorder = Color(0xFF1E293B)         // slate-800
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

// ==================== Material3 Color Schemes ====================

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
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0F172A),           // slate-900
    onBackground = Color(0xFFF1F5F9),         // slate-100
    surface = Color(0xFF0F172A),              // slate-900
    onSurface = Color(0xFFF1F5F9),            // slate-100
    surfaceVariant = Color(0xFF1E293B),       // slate-800
    onSurfaceVariant = Color(0xFF94A3B8),     // slate-400
    surfaceContainerHigh = Color(0xFF1E293B), // slate-800
    surfaceContainer = Color(0xFF0F172A),     // slate-900
    surfaceContainerLow = Color(0xFF020617),  // slate-950
    outline = Color(0xFF475569),              // slate-600
    outlineVariant = Color(0xFF334155)        // slate-700
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),     // blue-100
    onPrimaryContainer = Color(0xFF1E3A8A),   // blue-900
    secondary = GradientPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E8FF),   // purple-100
    onSecondaryContainer = Color(0xFF581C87), // purple-900
    tertiary = EmeraldGreen,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),    // emerald-100
    onTertiaryContainer = Color(0xFF064E3B),  // emerald-900
    error = Color(0xFFDC2626),                // red-600
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),       // red-100
    onErrorContainer = Color(0xFF7F1D1D),     // red-900
    background = Color(0xFFF8FAFC),           // slate-50
    onBackground = Color(0xFF0F172A),         // slate-900
    surface = Color(0xFFFFFFFF),              // white
    onSurface = Color(0xFF0F172A),            // slate-900
    surfaceVariant = Color(0xFFF1F5F9),       // slate-100
    onSurfaceVariant = Color(0xFF64748B),     // slate-500
    surfaceContainerHigh = Color(0xFFF1F5F9), // slate-100
    surfaceContainer = Color(0xFFF8FAFC),     // slate-50
    surfaceContainerLow = Color(0xFFF8FAFC),  // slate-50
    outline = Color(0xFFCBD5E1),              // slate-300
    outlineVariant = Color(0xFFE2E8F0)        // slate-200
)

// ==================== Typography ====================

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

// ==================== Theme Composable ====================

@Composable
fun GemmaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}

// ==================== Extension Properties ====================

/**
 * Access extended colors from MaterialTheme
 * Usage: MaterialTheme.extendedColors.chatBackground
 */
val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    get() = LocalExtendedColors.current
