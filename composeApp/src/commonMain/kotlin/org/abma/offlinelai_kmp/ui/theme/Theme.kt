package org.abma.offlinelai_kmp.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Legacy brand constants preserved for backward compatibility
val BrandIndigo = Color(0xFF6366F1)
val BrandSlate = Color(0xFF475569)
val EmeraldGreen = Color(0xFF34D399)
val PrimaryColor = Color(0xFF101518)
val SecondaryColor = Color(0xFF64748B)
val AccentColor = Color(0xFF84B8FF)
val GradientStart = Color(0xFF161C1F)
val GradientEnd = Color(0xFF232B2E)

/**
 * Canonical spacing tokens (1:1 parity with RelaySpace).
 */
object RelaySpace {
    val xxs: Dp = 4.dp
    val xs: Dp = 8.dp
    val sm: Dp = 12.dp
    val md: Dp = 16.dp
    val lg: Dp = 24.dp
    val xl: Dp = 32.dp
}

/**
 * Canonical corner radius tokens (1:1 parity with RelayRadius).
 */
object RelayRadius {
    val xs: Dp = 8.dp
    val sm: Dp = 10.dp
    val md: Dp = 14.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val pill: Dp = 999.dp
}

/**
 * Canonical control heights (1:1 parity with RelayControlHeight).
 */
object RelayControlHeight {
    val compact: Dp = 28.dp
    val pill: Dp = 30.dp
    val bar: Dp = 34.dp
    val composer: Dp = 38.dp
    val header: Dp = 52.dp
}

/**
 * Signature Relay Prism & Thinking Gradients.
 */
object RelayGradients {
    val prismStops: List<Color> = listOf(
        Color(0xFF4A90E2),
        Color(0xFF6C5CE5),
        Color(0xFF9B48D4),
        Color(0xFFE8388F),
        Color(0xFFFF4646),
        Color(0xFFFF8E3C),
        Color(0xFFFDE2A3)
    )

    val thinkingRibbons: List<Color> = listOf(
        Color(0xFF38BDF8), // Sky
        Color(0xFF6366F1), // Indigo
        Color(0xFF2DD4BF)  // Teal
    )
}

@Immutable
data class ExtendedColors(
    // Existing tokens (preserved for full backward compatibility)
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

    // Relay & Sentient OS Surface Hierarchy
    val pitch: Color,
    val canvas: Color,
    val cardBG: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val glassBackdrop: Color,

    // Interactive Fills
    val subtleFill: Color,
    val badgeFill: Color,
    val hoverFill: Color,
    val activeFill: Color,

    // Crisp 1dp Hairline Borders
    val borderSubtle: Color,
    val borderDefault: Color,
    val borderStrong: Color,
    val borderFocus: Color,

    // Typography Ink Hierarchy
    val textPrimary: Color,
    val textBright: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textDim: Color,

    // Inverted High-Contrast Primary Button Tokens
    val buttonFace: Color,
    val buttonInk: Color,

    // Semantic, Role & Spectrum Accents
    val workbenchAccent: Color,
    val accentBlue: Color,
    val accentEmerald: Color,
    val accentAmethyst: Color,
    val accentAmber: Color,
    val accentCoral: Color,
    val accentCyan: Color,
    val accentGold: Color
)

private val LightExtendedColors = ExtendedColors(
    // Existing properties mapped to Relay Day / Porcelain palette
    chatBackground = Color(0xFFF7F9F8),
    inputBackground = Color(0xFFFFFFFF),
    bubbleUser = Color(0xFFF1F5F3),
    bubbleAi = Color(0xFFFFFFFF),
    bubbleAiBorder = Color(0x120F172A),
    avatarAi = Color(0xFFF1F5F3),
    statusReady = Color(0xFF047753),
    statusLoading = Color(0xFF2563EB),
    headerBackground = Color(0xF5FFFFFF),
    headerBorder = Color(0x120F172A),
    onSurfaceVariant = Color(0xFF454952),
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFF1F5F3),
    surfaceContainerHighest = Color(0xFFE6ECE9),

    // Relay Day / Porcelain Hierarchy
    pitch = Color(0xFFF1F5F3),
    canvas = Color(0xFFF7F9F8),
    cardBG = Color(0xFFFFFFFF),
    surface = Color(0xFFF1F5F3),
    surfaceElevated = Color(0xFFFFFFFF),
    glassBackdrop = Color(0xF5FFFFFF),

    subtleFill = Color(0x080F172A),
    badgeFill = Color(0x0D0F172A),
    hoverFill = Color(0x0C0F172A),
    activeFill = Color(0x140F172A),

    borderSubtle = Color(0x120F172A),
    borderDefault = Color(0x1C0F172A),
    borderStrong = Color(0x33111318),
    borderFocus = Color(0x38111318),

    textPrimary = Color(0xFF111318),
    textBright = Color(0xFF181B22),
    textSecondary = Color(0xFF454952),
    textMuted = Color(0xFF595F6B),
    textDim = Color(0xFF676F7D),

    buttonFace = Color(0xFF111318),
    buttonInk = Color(0xFFFFFFFF),

    workbenchAccent = Color(0xFF1B5B4E),
    accentBlue = Color(0xFF2563EB),
    accentEmerald = Color(0xFF047753),
    accentAmethyst = Color(0xFF7C3AED),
    accentAmber = Color(0xFFA65404),
    accentCoral = Color(0xFFCD2121),
    accentCyan = Color(0xFF07708A),
    accentGold = Color(0xFFB45309)
)

private val DarkExtendedColors = ExtendedColors(
    // Existing properties mapped to Relay Night / Obsidian & Slate palette
    chatBackground = Color(0xFF101518),
    inputBackground = Color(0xFF1C2326),
    bubbleUser = Color(0xFF1C2326),
    bubbleAi = Color(0xFF161C1F),
    bubbleAiBorder = Color(0x13FFFFFF),
    avatarAi = Color(0xFF1C2326),
    statusReady = Color(0xFF34D399),
    statusLoading = Color(0xFF84B8FF),
    headerBackground = Color(0xF5161C1F),
    headerBorder = Color(0x13FFFFFF),
    onSurfaceVariant = Color(0xFFC4C6CD),
    surfaceContainerLow = Color(0xFF161C1F),
    surfaceContainerHigh = Color(0xFF1C2326),
    surfaceContainerHighest = Color(0xFF232B2E),

    // Relay Night / Obsidian & Slate Hierarchy
    pitch = Color(0xFF0D1214),
    canvas = Color(0xFF101518),
    cardBG = Color(0xFF161C1F),
    surface = Color(0xFF1C2326),
    surfaceElevated = Color(0xFF232B2E),
    glassBackdrop = Color(0xF5161C1F),

    subtleFill = Color(0x0AFFFFFF),
    badgeFill = Color(0x12FFFFFF),
    hoverFill = Color(0x11FFFFFF),
    activeFill = Color(0x1CFFFFFF),

    borderSubtle = Color(0x13FFFFFF),
    borderDefault = Color(0x1FFFFFFF),
    borderStrong = Color(0x40FFFFFF),
    borderFocus = Color(0x3DFFFFFF),

    textPrimary = Color(0xFFF5F7FA),
    textBright = Color(0xFFEAEEF5),
    textSecondary = Color(0xFFC4C6CD),
    textMuted = Color(0xFFA6AAB6),
    textDim = Color(0xFF979EAD),

    buttonFace = Color(0xFFF5F7FA),
    buttonInk = Color(0xFF101319),

    workbenchAccent = Color(0xFF82CDB8),
    accentBlue = Color(0xFF84B8FF),
    accentEmerald = Color(0xFF34D399),
    accentAmethyst = Color(0xFFC0A8FA),
    accentAmber = Color(0xFFFBBF24),
    accentCoral = Color(0xFFF87171),
    accentCyan = Color(0xFFA5F3FC),
    accentGold = Color(0xFFF4D69A)
)

val LocalExtendedColors = staticCompositionLocalOf { DarkExtendedColors }

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF84B8FF),
    onPrimary = Color(0xFF101319),
    primaryContainer = Color(0xFF1C2326),
    onPrimaryContainer = Color(0xFFF5F7FA),
    secondary = Color(0xFF82CDB8),
    onSecondary = Color(0xFF101319),
    secondaryContainer = Color(0xFF1C2326),
    onSecondaryContainer = Color(0xFFEAEEF5),
    tertiary = Color(0xFFC0A8FA),
    onTertiary = Color(0xFF101319),
    error = Color(0xFFF87171),
    onError = Color(0xFF101319),
    errorContainer = Color(0xFF32191B),
    onErrorContainer = Color(0xFFFCA5A5),
    background = Color(0xFF101518),
    onBackground = Color(0xFFF5F7FA),
    surface = Color(0xFF101518),
    onSurface = Color(0xFFF5F7FA),
    surfaceVariant = Color(0xFF1C2326),
    onSurfaceVariant = Color(0xFFC4C6CD),
    surfaceContainerHigh = Color(0xFF232B2E),
    surfaceContainer = Color(0xFF1C2326),
    surfaceContainerLow = Color(0xFF161C1F),
    outline = Color(0xFF979EAD),
    outlineVariant = Color(0x1FFFFFFF),
    inverseSurface = Color(0xFFF5F7FA),
    inverseOnSurface = Color(0xFF101319)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEFF4FF),
    onPrimaryContainer = Color(0xFF111318),
    secondary = Color(0xFF1B5B4E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE7F3EF),
    onSecondaryContainer = Color(0xFF1B5B4E),
    tertiary = Color(0xFF7C3AED),
    onTertiary = Color(0xFFFFFFFF),
    error = Color(0xFFCD2121),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),
    background = Color(0xFFF7F9F8),
    onBackground = Color(0xFF111318),
    surface = Color(0xFFF7F9F8),
    onSurface = Color(0xFF111318),
    surfaceVariant = Color(0xFFF1F5F3),
    onSurfaceVariant = Color(0xFF454952),
    surfaceContainerHigh = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFF1F5F3),
    surfaceContainerLow = Color(0xFFFFFFFF),
    outline = Color(0xFF676F7D),
    outlineVariant = Color(0x1C0F172A),
    inverseSurface = Color(0xFF111318),
    inverseOnSurface = Color(0xFFFFFFFF)
)

/**
 * Antigravity & Relay Optical Typography Scale.
 */
private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 40.sp,
        lineHeight = 46.sp,
        letterSpacing = (-0.8).sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.45).sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 25.sp,
        letterSpacing = (-0.3).sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.45).sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.2).sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.5.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.05.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.5.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.1.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 10.5.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.2.sp
    )
)

val LocalThemeToggle = staticCompositionLocalOf<() -> Unit> { {} }
val LocalIsDarkTheme = staticCompositionLocalOf { true }

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

// ═══════════════════════════════════════════════════════════════════════════════
// RELAY & SENTIENT OS FOUNDATION ATOMS
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Antigravity Slate & Daylight Atmospheric Canvas (`RelayAtmosphere`).
 * Draws the adaptive canvas background plus subtle top radial lighting.
 */
@Composable
fun RelayAtmosphere(
    modifier: Modifier = Modifier,
    focused: Boolean = false,
    showsContours: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = MaterialTheme.extendedColors
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val topCenter = Offset(size.width * 0.5f, size.height * 0.04f)
            val topRadius = size.width.coerceAtLeast(580.dp.toPx()) * 0.72f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.accentBlue.copy(alpha = if (focused) 0.10f else 0.065f),
                        colors.accentAmethyst.copy(alpha = if (focused) 0.055f else 0.03f),
                        Color.Transparent
                    ),
                    center = topCenter,
                    radius = topRadius
                ),
                radius = topRadius,
                center = topCenter
            )

            if (showsContours) {
                val sideCenter = Offset(size.width * 0.80f, size.height * 0.24f)
                val sideRadius = size.width.coerceAtLeast(460.dp.toPx()) * 0.55f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colors.workbenchAccent.copy(alpha = if (focused) 0.055f else 0.035f),
                            Color.Transparent
                        ),
                        center = sideCenter,
                        radius = sideRadius
                    ),
                    radius = sideRadius,
                    center = sideCenter
                )
            }
        }
        content()
    }
}

/**
 * Concentric 3-ring status indicator dot (`RelayHealthDot`).
 */
@Composable
fun RelayHealthDot(
    color: Color,
    modifier: Modifier = Modifier,
    pulsing: Boolean = false
) {
    val transition = rememberInfiniteTransition(label = "health_dot")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.14f,
        targetValue = if (pulsing) 0.34f else 0.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier.size(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = if (pulsing) pulseAlpha else 0.16f))
        )
        Box(
            modifier = Modifier
                .size(11.dp)
                .clip(CircleShape)
                .border(0.75.dp, color.copy(alpha = 0.34f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

/**
 * Uppercase monospace eyebrow label (`RelayMonoCaps`).
 */
@Composable
fun RelayMonoCaps(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.extendedColors.textMuted,
    fontSize: TextUnit = 10.5.sp,
    letterSpacing: TextUnit = 1.2.sp
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            fontSize = fontSize,
            letterSpacing = letterSpacing,
            color = color
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

/**
 * Crisp Antigravity pill badge for statuses, tags, and metadata (`RelayBadge`).
 */
@Composable
fun RelayBadge(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tint: Color = MaterialTheme.extendedColors.textSecondary,
    prominent: Boolean = false,
    monospace: Boolean = false
) {
    val colors = MaterialTheme.extendedColors
    val bgColor = if (prominent) tint.copy(alpha = 0.12f) else colors.badgeFill
    val strokeColor = if (prominent) tint.copy(alpha = 0.28f) else colors.borderSubtle
    val contentColor = if (prominent) tint else colors.textSecondary

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(RelayRadius.pill),
        color = bgColor,
        border = BorderStroke(1.dp, strokeColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(11.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    letterSpacing = if (monospace) 0.2.sp else 0.sp
                ),
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Relay's signature obsidian squircle brand mark with the step-glyph (`RelayMark`).
 */
@Composable
fun RelayMark(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    active: Boolean = false
) {
    val colors = MaterialTheme.extendedColors
    val shape = RoundedCornerShape(size * 0.26f)

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1E2229),
                        Color(0xFF101318)
                    )
                )
            )
            .border(0.75.dp, Color.White.copy(alpha = 0.18f), shape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val glyphWidth = w * 0.50f
            val glyphHeight = h * 0.16f
            val left = (w - glyphWidth) / 2f
            val right = left + glyphWidth
            val top = (h - glyphHeight) / 2f
            val bottom = top + glyphHeight

            val path = Path().apply {
                moveTo(left, bottom)
                lineTo(left + glyphWidth * (11f / 28f), bottom)
                lineTo(left + glyphWidth * (17f / 28f), top)
                lineTo(right, top)
            }
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.95f),
                style = Stroke(
                    width = (w * 0.088f).coerceAtLeast(1.6.dp.toPx()),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
            if (active) {
                drawCircle(
                    color = colors.accentEmerald,
                    radius = (w * 0.065f).coerceAtLeast(2.dp.toPx()),
                    center = Offset(right - w * 0.03f, bottom + h * 0.12f)
                )
            }
        }
    }
}

/**
 * Antigravity surface card with crisp 1dp border and adaptive Night/Day elevation (`RelaySurfaceCard`).
 */
@Composable
fun RelaySurfaceCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = RelayRadius.md,
    elevated: Boolean = false,
    strokeColor: Color? = null,
    contentPadding: PaddingValues = PaddingValues(RelaySpace.md),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = MaterialTheme.extendedColors
    val shape = RoundedCornerShape(cornerRadius)
    val bg = if (elevated) colors.surface else colors.cardBG
    val border = BorderStroke(1.dp, strokeColor ?: colors.borderSubtle)

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            color = bg,
            border = border
        ) {
            Box(modifier = Modifier.padding(contentPadding), content = content)
        }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = bg,
            border = border
        ) {
            Box(modifier = Modifier.padding(contentPadding), content = content)
        }
    }
}

/**
 * Clean matte graphite squircle icon badge (`RelayIconBadge`).
 */
@Composable
fun RelayIconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.extendedColors.textBright,
    size: Dp = 36.dp,
    iconSize: Dp = 17.dp
) {
    val colors = MaterialTheme.extendedColors
    val shape = RoundedCornerShape(size * 0.28f)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(colors.badgeFill)
            .border(1.dp, colors.borderSubtle, shape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Capsule progress bar with Relay's Amethyst -> Blue -> Cyan glow gradient (`RelayGlowProgressBar`).
 */
@Composable
fun RelayGlowProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp
) {
    val colors = MaterialTheme.extendedColors
    val clamped = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(RelayRadius.pill))
            .background(colors.surfaceElevated)
    ) {
        if (clamped > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(clamped)
                    .clip(RoundedCornerShape(RelayRadius.pill))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                colors.accentAmethyst,
                                colors.accentBlue,
                                colors.accentCyan
                            )
                        )
                    )
            )
        }
    }
}

/**
 * Staggered 3-ribbon directional underlight sweeping left-to-right across the bottom edge
 * while Relay / LocalIntelligence is preparing or generating (`NotchThinkingUnderlight`).
 */
@Composable
fun NotchThinkingUnderlight(
    active: Boolean,
    modifier: Modifier = Modifier,
    height: Dp = 4.dp
) {
    if (!active) return
    val infiniteTransition = rememberInfiniteTransition(label = "thinking_underlight")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "underlight_phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val ribbons = RelayGradients.thinkingRibbons
        ribbons.forEachIndexed { index, color ->
            val progress = (phase + index / 3f) % 1f
            val centerX = size.width * (-0.3f + 1.6f * progress)
            val ribbonWidth = size.width * 0.48f
            val intensity = sin(progress * PI).toFloat().coerceIn(0f, 1f) * 0.92f
            val startX = (centerX - ribbonWidth / 2f).coerceAtLeast(0f)
            val endX = (centerX + ribbonWidth / 2f).coerceAtMost(size.width)
            if (endX > startX) {
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            color.copy(alpha = intensity),
                            Color.Transparent
                        ),
                        startX = centerX - ribbonWidth / 2f,
                        endX = centerX + ribbonWidth / 2f
                    ),
                    topLeft = Offset(startX, 0f),
                    size = Size(endX - startX, size.height),
                    cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
                )
            }
        }
    }
}

/**
 * Relay's orbital prism orb (`RelayPrismOrb`) for workspace start, loading, and attention states.
 */
enum class RelayOrbMode {
    IDLE,
    PROCESSING,
    ATTENTION
}

@Composable
fun RelayPrismOrb(
    modifier: Modifier = Modifier,
    mode: RelayOrbMode = RelayOrbMode.IDLE,
    size: Dp = 84.dp
) {
    val colors = MaterialTheme.extendedColors
    val infiniteTransition = rememberInfiniteTransition(label = "prism_orb")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (mode == RelayOrbMode.PROCESSING) 3600 else 9200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb_phase"
    )

    val light = if (mode == RelayOrbMode.ATTENTION) colors.accentAmber else colors.accentBlue
    val rim = if (mode == RelayOrbMode.ATTENTION) colors.accentCoral else colors.accentAmethyst

    Canvas(modifier = modifier.size(size)) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val unit = minOf(this.size.width, this.size.height)
        val coreRadius = unit * 0.25f
        val tilt = -0.38f + 0.12f * sin(phase)

        // Outer ambient halo
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    light.copy(alpha = 0.24f),
                    rim.copy(alpha = 0.10f),
                    Color.Transparent
                ),
                center = center,
                radius = unit * 0.5f
            ),
            radius = unit * 0.5f,
            center = center
        )

        fun orbitPoint(angle: Float, radius: Float, depth: Float, orbitTilt: Float): Offset {
            val h = radius * cos(angle)
            val v = depth * sin(angle)
            return Offset(
                x = center.x + h * cos(orbitTilt) - v * sin(orbitTilt),
                y = center.y + h * sin(orbitTilt) + v * cos(orbitTilt)
            )
        }

        fun orbitPath(startAngle: Float, endAngle: Float, radius: Float, depth: Float, orbitTilt: Float): Path {
            val path = Path()
            val steps = 32
            val first = orbitPoint(startAngle, radius, depth, orbitTilt)
            path.moveTo(first.x, first.y)
            for (i in 1..steps) {
                val a = startAngle + (endAngle - startAngle) * (i.toFloat() / steps)
                val pt = orbitPoint(a, radius, depth, orbitTilt)
                path.lineTo(pt.x, pt.y)
            }
            return path
        }

        val orbitRadius = unit * 0.44f
        val orbitDepth = unit * 0.16f

        // Secondary tilted ring
        val outerRing = orbitPath(0f, (2f * PI).toFloat(), unit * 0.46f, unit * 0.20f, tilt + 0.36f)
        drawPath(
            path = outerRing,
            color = rim.copy(alpha = 0.26f),
            style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
        )

        // Back half of primary ring
        val backArc = orbitPath(PI.toFloat(), (2f * PI).toFloat(), orbitRadius, orbitDepth, tilt)
        drawPath(
            path = backArc,
            color = light.copy(alpha = 0.32f),
            style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
        )

        val satellite = orbitPoint(phase, orbitRadius, orbitDepth, tilt)
        val inFront = sin(phase) >= 0f
        val satRadius = unit * (if (inFront) 0.025f else 0.016f)

        if (!inFront) {
            drawCircle(
                color = light.copy(alpha = 0.72f),
                radius = satRadius,
                center = satellite
            )
        }

        // Core sphere
        val highlight = Offset(center.x - coreRadius * 0.35f, center.y - coreRadius * 0.42f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White,
                    light,
                    rim.copy(alpha = 0.85f),
                    colors.canvas
                ),
                center = highlight,
                radius = coreRadius * 1.65f
            ),
            radius = coreRadius,
            center = center
        )
        drawCircle(
            color = light.copy(alpha = 0.55f),
            radius = coreRadius,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )

        // Front half of primary ring
        val frontArc = orbitPath(0f, PI.toFloat(), orbitRadius, orbitDepth, tilt)
        drawPath(
            path = frontArc,
            brush = Brush.linearGradient(
                colors = listOf(
                    light.copy(alpha = 0.65f),
                    Color.White.copy(alpha = 0.90f),
                    rim.copy(alpha = 0.85f)
                )
            ),
            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        )

        if (inFront) {
            drawCircle(
                color = Color.White.copy(alpha = 0.92f),
                radius = satRadius,
                center = satellite
            )
        }
    }
}
