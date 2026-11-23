package org.abma.offlinelai_kmp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.abma.offlinelai_kmp.ui.theme.NotchThinkingUnderlight
import org.abma.offlinelai_kmp.ui.theme.RelayHealthDot
import org.abma.offlinelai_kmp.ui.theme.RelayRadius
import org.abma.offlinelai_kmp.ui.theme.extendedColors
import kotlin.math.PI
import kotlin.math.sin

private data class SlashCommandSuggestion(
    val command: String,
    val label: String,
    val prompt: String
)

private val DefaultSlashSuggestions = listOf(
    SlashCommandSuggestion("/torch on", "Turn flashlight ON", "Turn on the flashlight"),
    SlashCommandSuggestion("/torch off", "Turn flashlight OFF", "Turn off the flashlight"),
    SlashCommandSuggestion("/time", "Get current device time", "What is the current device time?"),
    SlashCommandSuggestion("/maps", "Search nearby places", "Open maps and search for coffee shops near me"),
    SlashCommandSuggestion("/url", "Open URL in browser", "Open https://kotlinlang.org in the browser"),
    SlashCommandSuggestion("/email", "Compose an email", "Open email composer to hello@example.com with subject Quick Sync"),
    SlashCommandSuggestion("/summarize", "Summarize conversation", "Summarize our conversation into key takeaways and action items.")
)

/**
 * Canonical Relay & Sentient OS Composer Dock (`MessageInput`).
 *
 * Includes:
 * - Adaptive quick-action `RelaySourceChip` strip
 * - Live `/slash` command auto-complete suggestions
 * - Inline 14-bar `NotchAudioWaveform` voice dictation bar
 * - Relay `16.dp` squircle composer container with `+` Command Deck trigger, mic button,
 *   high-contrast `RelaySendButton`, and `NotchStopButton`
 */
@Composable
fun MessageInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    placeholder: String = "Message LocalIntelligence or type / for tools…",
    isGenerating: Boolean = false,
    isRecording: Boolean = false,
    onToggleRecording: () -> Unit = {},
    onStopGeneration: (() -> Unit)? = null,
    onOpenCommandDeck: (() -> Unit)? = null,
    quickActions: List<Pair<String, String>> = emptyList(),
    onQuickActionClick: ((String) -> Unit)? = null
) {
    val colors = MaterialTheme.extendedColors
    var isFocused by remember { mutableStateOf(false) }

    val matchingSlashCommands = remember(value) {
        val trimmed = value.trim()
        if (trimmed.startsWith("/")) {
            DefaultSlashSuggestions.filter {
                it.command.startsWith(trimmed, ignoreCase = true) ||
                    it.label.contains(trimmed.removePrefix("/"), ignoreCase = true)
            }
        } else {
            emptyList()
        }
    }

    val borderColor by animateColorAsState(
        targetValue = when {
            isRecording -> colors.accentCoral.copy(alpha = 0.65f)
            isFocused -> colors.borderFocus
            else -> colors.borderDefault
        },
        tween(durationMillis = 160),
        label = "composer_border"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.canvas)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Optional Quick-Action Chips Row (`RelaySourceChip` style)
        if (quickActions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                quickActions.forEach { (label, prompt) ->
                    RelaySourceChip(
                        label = label,
                        enabled = enabled && !isGenerating,
                        onClick = {
                            if (onQuickActionClick != null) {
                                onQuickActionClick(prompt)
                            } else {
                                onValueChange(prompt)
                            }
                        }
                    )
                }
            }
        }

        // Slash-command popup strip when user types `/`
        AnimatedVisibility(
            visible = matchingSlashCommands.isNotEmpty() && !isGenerating,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(RelayRadius.sm),
                color = colors.cardBG,
                border = BorderStroke(1.dp, colors.borderDefault)
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    matchingSlashCommands.forEach { suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(RelayRadius.xs))
                                .clickable { onValueChange(suggestion.prompt) }
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = suggestion.command,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.5.sp
                                    ),
                                    color = colors.accentCyan
                                )
                                Text(
                                    text = suggestion.label,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                            Text(
                                text = "Insert",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = colors.textMuted
                            )
                        }
                    }
                }
            }
        }

        // Inline Voice Dictation Bar (`inlineVoiceDictationBar` + `NotchAudioWaveform`)
        AnimatedVisibility(
            visible = isRecording,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = colors.cardBG,
                border = BorderStroke(1.dp, colors.accentCoral.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RelayHealthDot(color = colors.accentCoral, pulsing = true)

                    NotchAudioWaveform(isRecording = isRecording)

                    Text(
                        text = if (value.isNotBlank()) value else "Listening on-device… speak now",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textBright,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        onClick = onToggleRecording,
                        shape = RoundedCornerShape(RelayRadius.xs),
                        color = colors.accentCoral.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, colors.accentCoral.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "Finish",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            ),
                            color = colors.accentCoral,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Main Relay Squircle Composer Dock (`cornerRadius = 16.dp`)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(RelayRadius.lg),
            color = colors.surface,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Left `+` Command & Tool Deck Trigger Button
                    if (onOpenCommandDeck != null) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(RelayRadius.xs))
                                .background(colors.subtleFill)
                                .border(1.dp, colors.borderSubtle, RoundedCornerShape(RelayRadius.xs))
                                .clickable(onClick = onOpenCommandDeck),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Open Command & Tool Deck",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Center Text Input
                    TextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { isFocused = it.isFocused },
                        placeholder = {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.textMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            cursorColor = colors.accentBlue
                        ),
                        enabled = enabled && !isGenerating,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = colors.textPrimary
                        ),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (value.isNotBlank() && enabled && !isGenerating) {
                                    onSend()
                                }
                            }
                        ),
                        singleLine = false,
                        maxLines = 5
                    )

                    // Right Voice Dictation Button
                    if (!isGenerating) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isRecording) colors.accentCoral.copy(alpha = 0.18f) else colors.subtleFill
                                )
                                .border(
                                    1.dp,
                                    if (isRecording) colors.accentCoral.copy(alpha = 0.5f) else colors.borderSubtle,
                                    CircleShape
                                )
                                .clickable(enabled = enabled, onClick = onToggleRecording),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isRecording) "Stop voice input" else "Dictate with voice",
                                tint = if (isRecording) colors.accentCoral else if (enabled) colors.textSecondary else colors.textDim,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Right RelaySendButton / NotchStopButton
                    if (isGenerating) {
                        // NotchStopButton
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(colors.accentCoral.copy(alpha = 0.18f))
                                .border(1.dp, colors.accentCoral.copy(alpha = 0.5f), CircleShape)
                                .clickable { onStopGeneration?.invoke() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop generation",
                                tint = colors.accentCoral,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    } else {
                        val canSend = enabled && value.isNotBlank()
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (canSend) colors.buttonFace else colors.subtleFill)
                                .border(
                                    1.dp,
                                    if (canSend) colors.borderDefault else colors.borderSubtle,
                                    CircleShape
                                )
                                .clickable(enabled = canSend, onClick = onSend),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Send message",
                                tint = if (canSend) colors.buttonInk else colors.textDim,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }

                // Subtle bottom underlight if currently generating
                NotchThinkingUnderlight(active = isGenerating, height = 2.5.dp)
            }
        }
    }
}

/**
 * Relay's `RelaySourceChip` for quick device tool & prompt starters above the composer.
 */
@Composable
fun RelaySourceChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    detail: String? = null,
    enabled: Boolean = true,
    active: Boolean = false
) {
    val colors = MaterialTheme.extendedColors
    val accent = if (active) colors.accentEmerald else colors.accentBlue

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(RelayRadius.xs),
        color = if (active) colors.surfaceElevated else colors.surface,
        border = BorderStroke(
            1.dp,
            if (enabled) accent.copy(alpha = if (active) 0.45f else 0.22f) else colors.borderSubtle
        )
    ) {
        Row(
            modifier = Modifier
                .height(30.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = null,
                tint = if (enabled) accent else colors.textDim,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                ),
                color = if (enabled) colors.textPrimary else colors.textDim,
                maxLines = 1
            )
            if (detail != null) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    ),
                    color = colors.textMuted,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Animated 12-bar audio waveform modelled on Relay's `NotchAudioWaveform`.
 */
@Composable
fun NotchAudioWaveform(
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.extendedColors
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val barWeights = listOf(0.30f, 0.52f, 0.84f, 0.45f, 0.95f, 0.65f, 0.88f, 0.48f, 0.76f, 0.36f)

    Row(
        modifier = modifier.height(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        barWeights.forEachIndexed { index, base ->
            val dynamicScale = if (isRecording) {
                (base * (0.55f + 0.45f * sin(phase + index * 0.7f))).coerceIn(0.2f, 1f)
            } else {
                base * 0.35f
            }
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height((16f * dynamicScale).dp)
                    .clip(RoundedCornerShape(RelayRadius.pill))
                    .background(colors.accentCoral)
            )
        }
    }
}
