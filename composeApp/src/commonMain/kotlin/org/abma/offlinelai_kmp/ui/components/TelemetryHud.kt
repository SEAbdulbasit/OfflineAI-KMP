package org.abma.offlinelai_kmp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.abma.offlinelai_kmp.domain.model.GenerationMetrics
import org.abma.offlinelai_kmp.domain.model.PerformanceStats
import org.abma.offlinelai_kmp.domain.model.PersonaPreset
import org.abma.offlinelai_kmp.ui.theme.LocalIsDarkTheme
import org.abma.offlinelai_kmp.ui.theme.LocalThemeToggle
import org.abma.offlinelai_kmp.ui.theme.NotchThinkingUnderlight
import org.abma.offlinelai_kmp.ui.theme.RelayBadge
import org.abma.offlinelai_kmp.ui.theme.RelayControlHeight
import org.abma.offlinelai_kmp.ui.theme.RelayGlowProgressBar
import org.abma.offlinelai_kmp.ui.theme.RelayHealthDot
import org.abma.offlinelai_kmp.ui.theme.RelayIconBadge
import org.abma.offlinelai_kmp.ui.theme.RelayMonoCaps
import org.abma.offlinelai_kmp.ui.theme.RelayRadius
import org.abma.offlinelai_kmp.ui.theme.RelaySpace
import org.abma.offlinelai_kmp.ui.theme.RelaySurfaceCard
import org.abma.offlinelai_kmp.ui.theme.extendedColors
import kotlin.math.roundToInt

/**
 * Sentient OS Live Neural Telemetry Strip & Expandable Inspector Drawer.
 */
@Composable
fun SentientTelemetryHud(
    latestMetrics: GenerationMetrics? = null,
    performanceStats: PerformanceStats? = null,
    liveTokensPerSecond: Double = 0.0,
    liveTokenCount: Int = 0,
    contextUsagePercent: Float = 0f,
    estimatedContextTokens: Int = 0,
    maxContextTokens: Int = 7500,
    deviceModel: String = "On-Device Engine",
    osVersion: String = "",
    platformName: String = "Local",
    pinnedMemoryCount: Int = 0,
    isGenerating: Boolean = false,
    isThrottled: Boolean = false,
    isExpanded: Boolean = false,
    onToggleExpand: () -> Unit = {},
    onResetMetrics: () -> Unit = {},
    onClearPinnedMemories: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.extendedColors
    val ctxInt = contextUsagePercent.roundToInt().coerceIn(0, 100)
    val tpsValue = if (isGenerating && liveTokensPerSecond > 0.0) {
        liveTokensPerSecond
    } else {
        latestMetrics?.tokensPerSecond
            ?: performanceStats?.averageTokensPerSecond?.takeIf { it > 0.0 }
    }
    val ttftValue = latestMetrics?.timeToFirstTokenMs
        ?: performanceStats?.averageTimeToFirstToken?.takeIf { it > 0L }

    val tpsText = if (tpsValue != null && tpsValue > 0.0) {
        buildString {
            append("${formatMetricDouble(tpsValue)} tok/s")
            if (isGenerating && liveTokenCount > 0) append(" ($liveTokenCount)")
        }
    } else if (isGenerating) {
        "STREAMING"
    } else {
        "IDLE · READY"
    }

    val ttftText = if (ttftValue != null && ttftValue > 0L) {
        "${ttftValue}ms TTFT"
    } else {
        "-- ms TTFT"
    }

    val ctxColor = when {
        ctxInt >= 85 -> colors.accentCoral
        ctxInt >= 65 -> colors.accentAmber
        else -> colors.accentAmethyst
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.cardBG)
    ) {
        // Compact always-on Sentient OS Telemetry Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpand)
                .padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Live TPS chip
                TelemetryMetricPill(
                    label = tpsText,
                    dotColor = if (isGenerating) colors.accentCyan else colors.accentEmerald,
                    pulsing = isGenerating,
                    tint = colors.accentCyan
                )

                // TTFT chip
                TelemetryMetricPill(
                    label = ttftText,
                    tint = colors.accentBlue
                )

                // Context window meter chip
                Surface(
                    shape = RoundedCornerShape(RelayRadius.pill),
                    color = colors.badgeFill,
                    border = BorderStroke(1.dp, colors.borderSubtle)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "CTX ${ctxInt}%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.5.sp
                            ),
                            color = ctxColor
                        )
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(RelayRadius.pill))
                                .background(colors.surfaceElevated)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth((ctxInt / 100f).coerceAtLeast(0.04f))
                                    .clip(RoundedCornerShape(RelayRadius.pill))
                                    .background(ctxColor)
                            )
                        }
                    }
                }

                // Pinned memory indicator
                if (pinnedMemoryCount > 0) {
                    RelayBadge(
                        text = "$pinnedMemoryCount PINNED",
                        icon = Icons.Default.Bookmark,
                        tint = colors.accentGold,
                        prominent = true,
                        monospace = true
                    )
                }

                // Hardware device badge
                if (deviceModel.isNotBlank()) {
                    RelayBadge(
                        text = deviceModel,
                        icon = Icons.Default.Memory,
                        tint = colors.workbenchAccent,
                        prominent = false,
                        monospace = true
                    )
                }
            }

            Spacer(Modifier.width(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (isExpanded) "HIDE HUD" else "HUD",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    ),
                    color = colors.textMuted
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse telemetry HUD" else "Expand telemetry HUD",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Animated underlight while actively generating
        NotchThinkingUnderlight(active = isGenerating, height = 2.5.dp)

        // Expandable Sentient OS Neural Telemetry Inspector Drawer
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.pitch)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RelayMonoCaps(
                        text = "SENTIENT OS · NEURAL TELEMETRY INSPECTOR",
                        color = colors.workbenchAccent
                    )
                    RelayBadge(
                        text = if (isThrottled) "THERMAL THROTTLE WARN" else "THERMAL OPTIMAL",
                        tint = if (isThrottled) colors.accentAmber else colors.accentEmerald,
                        prominent = true,
                        monospace = true
                    )
                }

                // 4-Metric Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val avgTps = performanceStats?.averageTokensPerSecond ?: 0.0
                    val maxTps = performanceStats?.maxTokensPerSecond ?: 0.0
                    val avgTtft = performanceStats?.averageTimeToFirstToken ?: 0L
                    val totalTok = performanceStats?.totalTokensGenerated ?: 0

                    TelemetryStatBox(
                        title = "AVG SPEED",
                        value = if (avgTps > 0) "${formatMetricDouble(avgTps)} t/s" else "--",
                        accent = colors.accentCyan,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryStatBox(
                        title = "PEAK SPEED",
                        value = if (maxTps > 0) "${formatMetricDouble(maxTps)} t/s" else "--",
                        accent = colors.accentEmerald,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryStatBox(
                        title = "AVG TTFT",
                        value = if (avgTtft > 0) "${avgTtft}ms" else "--",
                        accent = colors.accentBlue,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryStatBox(
                        title = "GENERATED",
                        value = "$totalTok tok",
                        accent = colors.accentAmethyst,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Context Window Fill Meter
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Context Window Utilization",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                        Text(
                            text = "$estimatedContextTokens / $maxContextTokens tokens (${ctxInt}%)",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = ctxColor
                        )
                    }
                    RelayGlowProgressBar(
                        progress = (ctxInt / 100f).coerceIn(0f, 1f),
                        modifier = Modifier.fillMaxWidth(),
                        height = 5.dp
                    )
                }

                // Device & Session Actions Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val hwLabel = buildString {
                        append(platformName)
                        if (deviceModel.isNotBlank()) append(" · $deviceModel")
                        if (osVersion.isNotBlank()) append(" ($osVersion)")
                    }
                    Text(
                        text = hwLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (pinnedMemoryCount > 0) {
                            Surface(
                                onClick = onClearPinnedMemories,
                                shape = RoundedCornerShape(RelayRadius.xs),
                                color = colors.surface,
                                border = BorderStroke(1.dp, colors.borderSubtle)
                            ) {
                                Text(
                                    text = "Unpin All ($pinnedMemoryCount)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.accentGold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if ((performanceStats?.totalGenerations ?: 0) > 0) {
                            Surface(
                                onClick = onResetMetrics,
                                shape = RoundedCornerShape(RelayRadius.xs),
                                color = colors.surface,
                                border = BorderStroke(1.dp, colors.borderSubtle)
                            ) {
                                Text(
                                    text = "Reset Stats",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = colors.borderSubtle, thickness = 1.dp)
    }
}

@Composable
private fun TelemetryMetricPill(
    label: String,
    tint: Color,
    dotColor: Color? = null,
    pulsing: Boolean = false
) {
    val colors = MaterialTheme.extendedColors
    Surface(
        shape = RoundedCornerShape(RelayRadius.pill),
        color = colors.badgeFill,
        border = BorderStroke(1.dp, colors.borderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (dotColor != null) {
                RelayHealthDot(color = dotColor, pulsing = pulsing)
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.5.sp
                ),
                color = tint,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun TelemetryStatBox(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.extendedColors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(RelayRadius.xs),
        color = colors.cardBG,
        border = BorderStroke(1.dp, colors.borderSubtle)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    letterSpacing = 0.8.sp
                ),
                color = colors.textMuted,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                ),
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Relay Persona / Agent Mode Selector Bar (`RelayEngineTab` style).
 */
@Composable
fun RelayPersonaBar(
    activePersona: PersonaPreset,
    onSelectPersona: (PersonaPreset) -> Unit,
    customSystemPrompt: String = "",
    onUpdateCustomPrompt: (String) -> Unit = {},
    onNavigateToDemos: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.extendedColors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.canvas)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            PersonaPreset.entries.forEach { preset ->
                val selected = preset == activePersona
                val accent = personaAccentColor(preset)
                val icon = personaIcon(preset)

                Surface(
                    onClick = { onSelectPersona(preset) },
                    shape = RoundedCornerShape(9.dp),
                    color = if (selected) colors.surfaceElevated else colors.surface,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (selected) accent.copy(alpha = 0.55f) else colors.borderSubtle
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .height(32.dp)
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (selected) Icons.Default.CheckCircle else icon,
                            contentDescription = null,
                            tint = if (selected) accent else colors.textMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = preset.displayName,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            color = if (selected) colors.textPrimary else colors.textSecondary
                        )
                        Text(
                            text = preset.shortTag,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.5.sp
                            ),
                            color = if (selected) accent else colors.textMuted
                        )
                    }
                }
            }

            if (onNavigateToDemos != null) {
                Surface(
                    onClick = onNavigateToDemos,
                    shape = RoundedCornerShape(9.dp),
                    color = colors.subtleFill,
                    border = BorderStroke(1.dp, colors.borderDefault)
                ) {
                    Row(
                        modifier = Modifier
                            .height(32.dp)
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = colors.workbenchAccent,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Offline Workbench",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            color = colors.textBright
                        )
                    }
                }
            }
        }

        // Custom Directive Inline Editor when PersonaPreset.CUSTOM is active
        AnimatedVisibility(
            visible = activePersona == PersonaPreset.CUSTOM,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .padding(bottom = 8.dp)
            ) {
                OutlinedTextField(
                    value = customSystemPrompt,
                    onValueChange = onUpdateCustomPrompt,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            "Custom System Directive",
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    placeholder = {
                        Text(
                            "Define custom role instructions or output constraints...",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted
                        )
                    },
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = colors.textPrimary),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.cardBG,
                        focusedBorderColor = colors.borderFocus,
                        unfocusedBorderColor = colors.borderSubtle,
                        cursorColor = colors.accentBlue
                    )
                )
            }
        }

        HorizontalDivider(color = colors.borderSubtle, thickness = 1.dp)
    }
}

@Composable
fun personaAccentColor(persona: PersonaPreset): Color {
    val colors = MaterialTheme.extendedColors
    return when (persona) {
        PersonaPreset.DEFAULT -> colors.accentBlue
        PersonaPreset.CODE_ARCHITECT -> colors.accentAmethyst
        PersonaPreset.CREATIVE_SYNTH -> colors.accentGold
        PersonaPreset.TACTICAL_BRIEF -> colors.accentEmerald
        PersonaPreset.CUSTOM -> colors.workbenchAccent
    }
}

fun personaIcon(persona: PersonaPreset): ImageVector = when (persona) {
    PersonaPreset.DEFAULT -> Icons.Default.AutoAwesome
    PersonaPreset.CODE_ARCHITECT -> Icons.Default.Code
    PersonaPreset.CREATIVE_SYNTH -> Icons.Default.Tune
    PersonaPreset.TACTICAL_BRIEF -> Icons.Default.Bolt
    PersonaPreset.CUSTOM -> Icons.Default.Build
}

/**
 * Compact one-click Night / Light appearance switcher button (`RelayAppearanceToggleButton`)
 * for TopAppBars and headers.
 */
@Composable
fun RelayAppearanceToggleButton(
    modifier: Modifier = Modifier,
    showsLabel: Boolean = false
) {
    val isDark = LocalIsDarkTheme.current
    val onToggle = LocalThemeToggle.current
    val colors = MaterialTheme.extendedColors

    Surface(
        onClick = onToggle,
        modifier = modifier.height(32.dp),
        shape = RoundedCornerShape(RelayRadius.pill),
        color = colors.subtleFill,
        border = BorderStroke(1.dp, colors.borderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (showsLabel) 10.dp else 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                contentDescription = if (isDark) "Switch to Light appearance" else "Switch to Night appearance",
                tint = colors.textSecondary,
                modifier = Modifier.size(14.dp)
            )
            if (showsLabel) {
                Text(
                    text = if (isDark) "Night" else "Light",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textPrimary
                )
            }
        }
    }
}

/**
 * Segmented Night / Light appearance picker (`RelayAppearancePicker`) for Settings.
 */
@Composable
fun RelayAppearancePicker(
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val onToggle = LocalThemeToggle.current
    val colors = MaterialTheme.extendedColors

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.borderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(true to "Night", false to "Light").forEach { (darkOption, label) ->
                val selected = isDark == darkOption
                Surface(
                    onClick = { if (isDark != darkOption) onToggle() },
                    shape = RoundedCornerShape(7.dp),
                    color = if (selected) colors.surfaceElevated else Color.Transparent,
                    border = if (selected) BorderStroke(1.dp, colors.borderSubtle) else null
                ) {
                    Row(
                        modifier = Modifier
                            .height(28.dp)
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = if (darkOption) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            tint = if (selected) colors.textPrimary else colors.textMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                            ),
                            color = if (selected) colors.textPrimary else colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Sentient OS Command & Tool Deck Modal Bottom Sheet triggered by the `+` button in the Composer.
 */
data class QuickDeviceToolItem(
    val toolName: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val tint: Color,
    val directArgumentsJson: String? = null,
    val draftPrompt: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelayCommandDeckSheet(
    onDismiss: () -> Unit,
    onExecuteQuickTool: (toolName: String, argumentsJson: String) -> Unit,
    onFillPrompt: (String) -> Unit,
    onSendPrompt: (String) -> Unit,
    onExportChat: (shareViaSystemSheet: Boolean) -> Unit,
    onClearChat: () -> Unit,
    onNavigateToDemos: () -> Unit,
    onNavigateToSettings: () -> Unit,
    hasMessages: Boolean = false
) {
    val colors = MaterialTheme.extendedColors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val quickTools = listOf(
        QuickDeviceToolItem(
            toolName = "toggle_torch",
            title = "Flashlight ON",
            subtitle = "toggle_torch(enable=true)",
            icon = Icons.Default.FlashlightOn,
            tint = colors.accentAmber,
            directArgumentsJson = """{"enable":true}""",
            draftPrompt = "Turn on the flashlight"
        ),
        QuickDeviceToolItem(
            toolName = "toggle_torch",
            title = "Flashlight OFF",
            subtitle = "toggle_torch(enable=false)",
            icon = Icons.Default.FlashlightOff,
            tint = colors.textSecondary,
            directArgumentsJson = """{"enable":false}""",
            draftPrompt = "Turn off the flashlight"
        ),
        QuickDeviceToolItem(
            toolName = "get_current_time",
            title = "Device Clock",
            subtitle = "get_current_time()",
            icon = Icons.Default.AccessTime,
            tint = colors.accentCyan,
            directArgumentsJson = """{}""",
            draftPrompt = "What is the current device time?"
        ),
        QuickDeviceToolItem(
            toolName = "open_maps",
            title = "Explore Maps",
            subtitle = "open_maps(query)",
            icon = Icons.Default.Map,
            tint = colors.accentEmerald,
            directArgumentsJson = null,
            draftPrompt = "Open maps and search for coffee shops near me"
        ),
        QuickDeviceToolItem(
            toolName = "open_url",
            title = "Open Browser URL",
            subtitle = "open_url(url)",
            icon = Icons.Default.OpenInBrowser,
            tint = colors.accentBlue,
            directArgumentsJson = null,
            draftPrompt = "Open https://kotlinlang.org in my browser"
        ),
        QuickDeviceToolItem(
            toolName = "open_email",
            title = "Draft Email",
            subtitle = "open_email(to, subject)",
            icon = Icons.Default.Email,
            tint = colors.accentAmethyst,
            directArgumentsJson = null,
            draftPrompt = "Open email composer to team@example.com with subject Project Update"
        ),
        QuickDeviceToolItem(
            toolName = "open_dialer",
            title = "Phone Dialer",
            subtitle = "open_dialer(phone_number)",
            icon = Icons.Default.Call,
            tint = colors.workbenchAccent,
            directArgumentsJson = null,
            draftPrompt = "Open the phone dialer with number +1-555-0199"
        ),
        QuickDeviceToolItem(
            toolName = "open_app_settings",
            title = "OS App Settings",
            subtitle = "open_app_settings()",
            icon = Icons.Default.Settings,
            tint = colors.accentGold,
            directArgumentsJson = """{}""",
            draftPrompt = "Open the app system settings"
        )
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.cardBG,
        contentColor = colors.textPrimary,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    RelayMonoCaps(
                        text = "SENTIENT OS · COMMAND & TOOL DECK",
                        color = colors.workbenchAccent
                    )
                    Text(
                        text = "On-Device Tools & Workspace Actions",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Command Deck",
                        tint = colors.textSecondary
                    )
                }
            }

            // Section 1: On-Device Tool Execution Grid
            RelayMonoCaps(text = "DEVICE HARDWARE & OS BRIDGES", color = colors.textMuted)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                quickTools.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { item ->
                            RelaySurfaceCard(
                                modifier = Modifier.weight(1f),
                                cornerRadius = RelayRadius.sm,
                                elevated = true,
                                contentPadding = PaddingValues(10.dp),
                                onClick = {
                                    if (item.directArgumentsJson != null) {
                                        onExecuteQuickTool(item.toolName, item.directArgumentsJson)
                                    } else {
                                        onFillPrompt(item.draftPrompt)
                                    }
                                    onDismiss()
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    RelayIconBadge(
                                        icon = item.icon,
                                        tint = item.tint,
                                        size = 32.dp,
                                        iconSize = 15.dp
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.5.sp
                                            ),
                                            color = colors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = item.subtitle,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 9.5.sp
                                            ),
                                            color = colors.textMuted,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = colors.borderSubtle)

            // Section 2: Workspace & Transcript Operations
            RelayMonoCaps(text = "WORKSPACE & TRANSCRIPT ACTIONS", color = colors.textMuted)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CommandActionChip(
                    title = "Offline Workbench",
                    icon = Icons.Default.Bolt,
                    tint = colors.workbenchAccent,
                    onClick = {
                        onDismiss()
                        onNavigateToDemos()
                    }
                )
                CommandActionChip(
                    title = "Runtime & Models",
                    icon = Icons.Default.Speed,
                    tint = colors.accentBlue,
                    onClick = {
                        onDismiss()
                        onNavigateToSettings()
                    }
                )
                if (hasMessages) {
                    CommandActionChip(
                        title = "Summarize Chat",
                        icon = Icons.Default.AutoAwesome,
                        tint = colors.accentAmethyst,
                        onClick = {
                            onDismiss()
                            onSendPrompt("Summarize our conversation so far into key bullet points and action items.")
                        }
                    )
                    CommandActionChip(
                        title = "Share Markdown",
                        icon = Icons.Default.IosShare,
                        tint = colors.accentEmerald,
                        onClick = {
                            onDismiss()
                            onExportChat(true)
                        }
                    )
                    CommandActionChip(
                        title = "Copy Transcript",
                        icon = Icons.Default.ContentCopy,
                        tint = colors.textBright,
                        onClick = {
                            onDismiss()
                            onExportChat(false)
                        }
                    )
                    CommandActionChip(
                        title = "Clear Session",
                        icon = Icons.Default.DeleteSweep,
                        tint = colors.accentCoral,
                        onClick = {
                            onDismiss()
                            onClearChat()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CommandActionChip(
    title: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.extendedColors
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(RelayRadius.xs),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.borderSubtle)
    ) {
        Row(
            modifier = Modifier
                .height(34.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = colors.textPrimary
            )
        }
    }
}

private fun formatMetricDouble(value: Double): String {
    val rounded = (value * 10.0).roundToInt() / 10.0
    return rounded.toString()
}
