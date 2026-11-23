package org.abma.offlinelai_kmp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.abma.offlinelai_kmp.domain.model.ChatMessage
import org.abma.offlinelai_kmp.domain.model.GenerationMetrics
import org.abma.offlinelai_kmp.ui.theme.NotchThinkingUnderlight
import org.abma.offlinelai_kmp.ui.theme.RelayBadge
import org.abma.offlinelai_kmp.ui.theme.RelayHealthDot
import org.abma.offlinelai_kmp.ui.theme.RelayIconBadge
import org.abma.offlinelai_kmp.ui.theme.RelayMark
import org.abma.offlinelai_kmp.ui.theme.RelayMonoCaps
import org.abma.offlinelai_kmp.ui.theme.RelayRadius
import org.abma.offlinelai_kmp.ui.theme.RelaySurfaceCard
import org.abma.offlinelai_kmp.ui.theme.extendedColors
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime

/**
 * Canonical Relay & Sentient OS Message & Tool-Call Transcript Component.
 *
 * Replaces both the legacy `ChatBubble` and private `MessageBubble` in `ChatScreen.kt`:
 * - User turns render as right-aligned Relay editorial slate/porcelain pills with inline edit/branching.
 * - AI turns render with `RelayMark`, persona role badge, structured fenced code blocks (`TranscriptCodeBlockCard`),
 *   structured OS Action Execution cards (`RelayActionExecutionCard`), `NotchThinkingUnderlight` during streaming,
 *   and a per-message telemetry + action footer (Copy with checkmark confirmation, Regenerate, Pin to Memory).
 */
@OptIn(ExperimentalTime::class)
@Composable
fun ChatBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier,
    onCopy: () -> Unit = {},
    onCopyText: ((String) -> Unit)? = null,
    onRegenerate: () -> Unit = {},
    onTogglePin: () -> Unit = {},
    onEditUserMessage: ((String) -> Unit)? = null
) {
    val isUser = message.isFromUser
    val colors = MaterialTheme.extendedColors
    var didCopy by remember { mutableStateOf(false) }
    var isEditing by remember(message.id) { mutableStateOf(false) }
    var editDraft by remember(message.id, message.content) { mutableStateOf(message.content) }

    LaunchedEffect(didCopy) {
        if (didCopy) {
            delay(1500)
            didCopy = false
        }
    }

    val timestampText = remember(message.timestamp) {
        try {
            val instant = Instant.fromEpochMilliseconds(message.timestamp)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            val hour = localDateTime.hour.toString().padStart(2, '0')
            val minute = localDateTime.minute.toString().padStart(2, '0')
            "$hour:$minute"
        } catch (_: Exception) {
            ""
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (isUser) {
            // ── USER MESSAGE: Right-Aligned Relay Editorial Pill ──
            Column(
                modifier = Modifier.widthIn(max = 340.dp),
                horizontalAlignment = Alignment.End
            ) {
                if (message.isPinned) {
                    RelayBadge(
                        text = "PINNED TO CONTEXT",
                        icon = Icons.Default.Bookmark,
                        tint = colors.accentGold,
                        prominent = true,
                        monospace = true,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                if (isEditing && onEditUserMessage != null) {
                    RelaySurfaceCard(
                        cornerRadius = RelayRadius.md,
                        elevated = true,
                        strokeColor = colors.borderFocus,
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            RelayMonoCaps(text = "EDIT & BRANCH CONVERSATION", color = colors.accentBlue)
                            OutlinedTextField(
                                value = editDraft,
                                onValueChange = { editDraft = it },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.textPrimary),
                                shape = RoundedCornerShape(RelayRadius.sm),
                                maxLines = 5,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = colors.cardBG,
                                    unfocusedContainerColor = colors.cardBG,
                                    focusedBorderColor = colors.borderFocus,
                                    unfocusedBorderColor = colors.borderSubtle,
                                    cursorColor = colors.accentBlue
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    onClick = {
                                        editDraft = message.content
                                        isEditing = false
                                    },
                                    shape = RoundedCornerShape(RelayRadius.xs),
                                    color = colors.subtleFill,
                                    border = BorderStroke(1.dp, colors.borderSubtle)
                                ) {
                                    Text(
                                        text = "Cancel",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = colors.textSecondary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    onClick = {
                                        val trimmed = editDraft.trim()
                                        if (trimmed.isNotEmpty()) {
                                            isEditing = false
                                            onEditUserMessage(trimmed)
                                        }
                                    },
                                    shape = RoundedCornerShape(RelayRadius.xs),
                                    color = colors.buttonFace
                                ) {
                                    Text(
                                        text = "Branch & Send",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.buttonInk,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(RelayRadius.md),
                        color = colors.surface,
                        border = BorderStroke(
                            1.dp,
                            if (message.isPinned) colors.accentGold.copy(alpha = 0.45f) else colors.borderSubtle
                        )
                    ) {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.textPrimary,
                            modifier = Modifier.padding(horizontal = 15.dp, vertical = 11.dp)
                        )
                    }
                }

                // User message actions & timestamp footer
                Row(
                    modifier = Modifier.padding(top = 5.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MessageActionPill(
                        icon = if (didCopy) Icons.Default.Check else Icons.Default.ContentCopy,
                        label = if (didCopy) "Copied" else null,
                        tint = if (didCopy) colors.accentEmerald else colors.textMuted,
                        onClick = {
                            didCopy = true
                            onCopy()
                        }
                    )

                    if (onEditUserMessage != null && !isEditing) {
                        MessageActionPill(
                            icon = Icons.Default.Edit,
                            tint = colors.textMuted,
                            onClick = {
                                editDraft = message.content
                                isEditing = true
                            }
                        )
                    }

                    MessageActionPill(
                        icon = if (message.isPinned) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        tint = if (message.isPinned) colors.accentGold else colors.textMuted,
                        onClick = onTogglePin
                    )

                    if (timestampText.isNotEmpty()) {
                        Text(
                            text = timestampText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.5.sp
                            ),
                            color = colors.textMuted
                        )
                    }
                }
            }
        } else {
            // ── ASSISTANT / TOOL MESSAGE: Left-Aligned Sentient OS Card ──
            val parsedTool = remember(message.content, message.toolCallName, message.toolArgumentsJson, message.toolResultText) {
                parseToolExecution(message)
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                // Assistant Header Bar: RelayMark + Role/Persona Badge + Status
                Row(
                    modifier = Modifier.padding(bottom = 6.dp, start = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RelayMark(size = 20.dp, active = message.isStreaming)

                    Text(
                        text = "LocalIntelligence",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp
                        ),
                        color = colors.textPrimary
                    )

                    val roleBadgeText = when {
                        parsedTool != null -> "OS TOOL BRIDGE"
                        !message.personaLabel.isNullOrBlank() -> message.personaLabel
                        else -> "ON-DEVICE"
                    }
                    val roleBadgeTint = when {
                        parsedTool != null -> colors.accentAmber
                        message.isError -> colors.accentCoral
                        else -> colors.workbenchAccent
                    }

                    RelayBadge(
                        text = roleBadgeText,
                        tint = roleBadgeTint,
                        prominent = parsedTool != null || !message.personaLabel.isNullOrBlank(),
                        monospace = true
                    )

                    if (message.isPinned) {
                        RelayBadge(
                            text = "PINNED",
                            icon = Icons.Default.Bookmark,
                            tint = colors.accentGold,
                            prominent = true,
                            monospace = true
                        )
                    }

                    if (message.isStreaming) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            RelayHealthDot(color = colors.accentCyan, pulsing = true)
                            Text(
                                text = "SYNTHESIZING",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.5.sp
                                ),
                                color = colors.accentCyan
                            )
                        }
                    }
                }

                // Assistant Content Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(RelayRadius.md),
                    color = if (message.isError) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f) else colors.cardBG,
                    border = BorderStroke(
                        width = 1.dp,
                        color = when {
                            message.isError -> colors.accentCoral.copy(alpha = 0.45f)
                            message.isPinned -> colors.accentGold.copy(alpha = 0.45f)
                            parsedTool != null -> colors.accentAmber.copy(alpha = 0.38f)
                            message.isStreaming -> colors.accentBlue.copy(alpha = 0.35f)
                            else -> colors.borderSubtle
                        }
                    )
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            when {
                                message.isStreaming && message.content.isEmpty() -> {
                                    SentientThinkingRow()
                                }

                                parsedTool != null -> {
                                    RelayActionExecutionCard(toolInfo = parsedTool)
                                }

                                message.content.isNotEmpty() -> {
                                    if (message.isError) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ErrorOutline,
                                                contentDescription = null,
                                                tint = colors.accentCoral,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = message.content,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = colors.accentCoral
                                            )
                                        }
                                    } else {
                                        TranscriptRichContent(
                                            rawText = message.content,
                                            onCopyCode = { code ->
                                                if (onCopyText != null) {
                                                    onCopyText(code)
                                                } else {
                                                    onCopy()
                                                }
                                            }
                                        )
                                    }

                                    if (message.isStreaming) {
                                        StreamingCursorPill()
                                    }
                                }
                            }
                        }

                        // Relay NotchThinkingUnderlight along bottom lip while streaming
                        NotchThinkingUnderlight(
                            active = message.isStreaming,
                            height = 3.5.dp
                        )
                    }
                }

                // Assistant Footer: Telemetry Pill + Copy / Regenerate / Pin / Timestamp
                if (!message.isStreaming) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, start = 4.dp, end = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Action Buttons + Timestamp
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (message.content.isNotEmpty()) {
                                MessageActionPill(
                                    icon = if (didCopy) Icons.Default.Check else Icons.Default.ContentCopy,
                                    label = if (didCopy) "Copied" else "Copy",
                                    tint = if (didCopy) colors.accentEmerald else colors.textSecondary,
                                    onClick = {
                                        didCopy = true
                                        onCopy()
                                    }
                                )

                                MessageActionPill(
                                    icon = Icons.Default.Refresh,
                                    label = "Retry",
                                    tint = colors.textSecondary,
                                    onClick = onRegenerate
                                )

                                MessageActionPill(
                                    icon = if (message.isPinned) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    label = if (message.isPinned) "Pinned" else "Pin",
                                    tint = if (message.isPinned) colors.accentGold else colors.textSecondary,
                                    onClick = onTogglePin
                                )
                            }

                            if (timestampText.isNotEmpty()) {
                                Text(
                                    text = timestampText,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.5.sp
                                    ),
                                    color = colors.textMuted
                                )
                            }
                        }

                        // Right: Per-Message Generation Telemetry Pill
                        message.metrics?.let { metrics ->
                            MessageTelemetryBadge(metrics = metrics)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageActionPill(
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
    label: String? = null
) {
    val colors = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 5.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(14.dp)
        )
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = tint
            )
        }
    }
}

@Composable
private fun MessageTelemetryBadge(metrics: GenerationMetrics) {
    val colors = MaterialTheme.extendedColors
    val tpsRounded = (metrics.tokensPerSecond * 10.0).roundToInt() / 10.0
    val label = buildString {
        if (tpsRounded > 0.0) append("⚡ $tpsRounded t/s")
        if (metrics.timeToFirstTokenMs > 0L) {
            if (isNotEmpty()) append(" · ")
            append("${metrics.timeToFirstTokenMs}ms")
        }
        if (metrics.responseTokens > 0) {
            if (isNotEmpty()) append(" · ")
            append("${metrics.responseTokens} tok")
        }
    }

    if (label.isNotEmpty()) {
        Surface(
            shape = RoundedCornerShape(RelayRadius.pill),
            color = colors.badgeFill,
            border = BorderStroke(1.dp, colors.borderSubtle)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                ),
                color = colors.accentCyan,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                maxLines = 1
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// SENTIENT OS ACTION / TOOL EXECUTION CARD
// ═══════════════════════════════════════════════════════════════════════════════

private data class ParsedToolExecution(
    val toolName: String,
    val argumentsJson: String,
    val resultText: String,
    val isSuccess: Boolean
)

private fun parseToolExecution(message: ChatMessage): ParsedToolExecution? {
    if (!message.toolCallName.isNullOrBlank()) {
        val result = message.toolResultText ?: message.content
        val failed = result.contains("Invalid", ignoreCase = true) ||
            result.contains("Unable", ignoreCase = true) ||
            result.contains("Error", ignoreCase = true) ||
            result.contains("Failed", ignoreCase = true) ||
            result.contains("Unknown tool", ignoreCase = true)
        return ParsedToolExecution(
            toolName = message.toolCallName,
            argumentsJson = message.toolArgumentsJson ?: "{}",
            resultText = result,
            isSuccess = !failed
        )
    }

    val raw = message.content.trim()
    val proposalPrefix = "Model proposal\n"
    val resultSeparator = "\n\nDevice result\n"
    if (raw.startsWith(proposalPrefix) && raw.contains(resultSeparator)) {
        val afterProposal = raw.removePrefix(proposalPrefix)
        val parts = afterProposal.split(resultSeparator, limit = 2)
        val callLine = parts.getOrNull(0)?.trim().orEmpty()
        val resultLine = parts.getOrNull(1)?.trim().orEmpty()
        val spaceIdx = callLine.indexOf(' ')
        val toolName = if (spaceIdx > 0) callLine.substring(0, spaceIdx).trim() else callLine
        val argsJson = if (spaceIdx > 0) callLine.substring(spaceIdx + 1).trim() else "{}"
        val failed = resultLine.contains("Invalid", ignoreCase = true) ||
            resultLine.contains("Unable", ignoreCase = true) ||
            resultLine.contains("Error", ignoreCase = true) ||
            resultLine.contains("Failed", ignoreCase = true)
        return ParsedToolExecution(
            toolName = toolName,
            argumentsJson = argsJson,
            resultText = resultLine,
            isSuccess = !failed
        )
    }
    return null
}

@Composable
private fun RelayActionExecutionCard(toolInfo: ParsedToolExecution) {
    val colors = MaterialTheme.extendedColors
    val toolIcon = when (toolInfo.toolName) {
        "toggle_torch" -> Icons.Default.FlashlightOn
        "get_current_time" -> Icons.Default.AccessTime
        "open_url" -> Icons.Default.OpenInBrowser
        "open_dialer" -> Icons.Default.Call
        "open_email" -> Icons.Default.Email
        "open_maps" -> Icons.Default.Map
        "share_text" -> Icons.Default.Share
        "copy_to_clipboard" -> Icons.Default.ContentCopy
        "open_app_settings" -> Icons.Default.Settings
        else -> Icons.Default.Build
    }
    val statusColor = if (toolInfo.isSuccess) colors.accentEmerald else colors.accentCoral

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RelayIconBadge(
                    icon = toolIcon,
                    tint = colors.accentAmber,
                    size = 34.dp,
                    iconSize = 16.dp
                )
                Column {
                    RelayMonoCaps(
                        text = "ON-DEVICE TOOL CALL",
                        color = colors.textMuted,
                        fontSize = 9.5.sp
                    )
                    Text(
                        text = toolInfo.toolName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colors.textPrimary
                    )
                }
            }

            RelayBadge(
                text = if (toolInfo.isSuccess) "VERIFIED" else "REJECTED",
                icon = if (toolInfo.isSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                tint = statusColor,
                prominent = true,
                monospace = true
            )
        }

        // JSON Arguments Pill
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(RelayRadius.xs),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.borderSubtle)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "ARGS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.5.sp
                    ),
                    color = colors.accentAmber
                )
                Text(
                    text = toolInfo.argumentsJson,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.5.sp
                    ),
                    color = colors.textBright
                )
            }
        }

        // Device Execution Result Well
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(RelayRadius.sm),
            color = colors.pitch,
            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.28f))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                RelayMonoCaps(
                    text = "DEVICE EXECUTION EVIDENCE",
                    color = statusColor,
                    fontSize = 9.5.sp
                )
                Text(
                    text = toolInfo.resultText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textPrimary
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// RICH MARKDOWN & CODE BLOCK PARSER (`TranscriptCodeBlockCard`)
// ═══════════════════════════════════════════════════════════════════════════════

private sealed interface MarkdownSegment {
    data class Prose(val text: String) : MarkdownSegment
    data class CodeBlock(val language: String, val code: String) : MarkdownSegment
}

private fun parseMarkdownSegments(raw: String): List<MarkdownSegment> {
    if (raw.isEmpty()) return emptyList()
    val lines = raw.replace("\r\n", "\n").split("\n")
    val segments = mutableListOf<MarkdownSegment>()
    val proseLines = mutableListOf<String>()
    val codeLines = mutableListOf<String>()
    var inCodeBlock = false
    var fenceLang = "code"

    fun flushProse() {
        if (proseLines.isNotEmpty()) {
            val joined = proseLines.joinToString("\n").trim()
            if (joined.isNotEmpty()) {
                segments.add(MarkdownSegment.Prose(joined))
            }
            proseLines.clear()
        }
    }

    fun flushCode() {
        val joined = codeLines.joinToString("\n").trimEnd('\n')
        segments.add(MarkdownSegment.CodeBlock(fenceLang, joined))
        codeLines.clear()
    }

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.startsWith("```")) {
            val afterTicks = trimmed.drop(3).trim()
            if (!inCodeBlock) {
                flushProse()
                inCodeBlock = true
                val lang = afterTicks.takeWhile { !it.isWhitespace() && it != '`' }.lowercase()
                fenceLang = lang.ifEmpty { "code" }
            } else {
                flushCode()
                inCodeBlock = false
                fenceLang = "code"
            }
        } else if (inCodeBlock) {
            codeLines.add(line)
        } else {
            proseLines.add(line)
        }
    }

    if (inCodeBlock) {
        flushCode()
    } else {
        flushProse()
    }

    return if (segments.isEmpty() && raw.isNotBlank()) {
        listOf(MarkdownSegment.Prose(raw.trim()))
    } else {
        segments
    }
}

@Composable
private fun TranscriptRichContent(
    rawText: String,
    onCopyCode: (String) -> Unit
) {
    val segments = remember(rawText) { parseMarkdownSegments(rawText) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        segments.forEach { segment ->
            when (segment) {
                is MarkdownSegment.Prose -> TranscriptProseView(prose = segment.text)
                is MarkdownSegment.CodeBlock -> TranscriptCodeBlockCard(
                    language = segment.language,
                    code = segment.code,
                    onCopyCode = { onCopyCode(segment.code) }
                )
            }
        }
    }
}

@Composable
private fun TranscriptProseView(prose: String) {
    val colors = MaterialTheme.extendedColors
    val lines = remember(prose) { prose.split("\n") }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        lines.forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.isEmpty() -> {
                    Spacer(Modifier.height(2.dp))
                }

                trimmed.startsWith("### ") -> {
                    Text(
                        text = trimmed.removePrefix("### ").trim(),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary
                    )
                }

                trimmed.startsWith("## ") -> {
                    Text(
                        text = trimmed.removePrefix("## ").trim(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary
                    )
                }

                trimmed.startsWith("# ") -> {
                    Text(
                        text = trimmed.removePrefix("# ").trim(),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary
                    )
                }

                trimmed.startsWith("> ") -> {
                    Surface(
                        shape = RoundedCornerShape(RelayRadius.sm),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.borderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = trimmed.removePrefix("> ").trim(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ") -> {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = colors.workbenchAccent
                        )
                        Text(
                            text = trimmed.drop(2).trim(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.textPrimary
                        )
                    }
                }

                else -> {
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.textPrimary
                    )
                }
            }
        }
    }
}

/**
 * Relay's `TranscriptCodeBlockCard` with 28.dp surface header bar, language badge,
 * emerald copy confirmation pill, and `pitch` (#0D1214) horizontally scrollable code well.
 */
@Composable
fun TranscriptCodeBlockCard(
    language: String,
    code: String,
    onCopyCode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.extendedColors
    var didCopy by remember { mutableStateOf(false) }

    LaunchedEffect(didCopy) {
        if (didCopy) {
            delay(1500)
            didCopy = false
        }
    }

    val shape = RoundedCornerShape(RelayRadius.sm)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = colors.pitch,
        border = BorderStroke(1.dp, colors.borderSubtle)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 28.dp Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .background(colors.surface)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = language.ifBlank { "code" },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    ),
                    color = colors.textSecondary
                )

                Surface(
                    onClick = {
                        didCopy = true
                        onCopyCode()
                    },
                    shape = RoundedCornerShape(RelayRadius.pill),
                    color = if (didCopy) colors.hoverFill else colors.subtleFill,
                    border = BorderStroke(
                        1.dp,
                        if (didCopy) colors.accentEmerald.copy(alpha = 0.35f) else colors.borderSubtle
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (didCopy) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy code",
                            tint = if (didCopy) colors.accentEmerald else colors.textSecondary,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = if (didCopy) "Copied" else "Copy code",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = if (didCopy) colors.accentEmerald else colors.textSecondary
                        )
                    }
                }
            }

            HorizontalDivider(color = colors.borderSubtle, thickness = 1.dp)

            // Horizontally scrollable monospace body in `pitch`
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.pitch)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = code,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    ),
                    color = colors.textBright
                )
            }
        }
    }
}

@Composable
private fun SentientThinkingRow(
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.extendedColors
    val infiniteTransition = rememberInfiniteTransition(label = "sentient_thinking")

    Row(
        modifier = modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                val offsetY by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = -3.5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 460,
                            delayMillis = index * 160,
                            easing = LinearEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "thinking_dot_$index"
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .offset(y = offsetY.dp)
                        .clip(CircleShape)
                        .background(
                            when (index) {
                                0 -> colors.accentBlue
                                1 -> colors.accentAmethyst
                                else -> colors.accentCyan
                            }
                        )
                )
            }
        }
        Text(
            text = "Synthesizing on-device response…",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            ),
            color = colors.textMuted
        )
    }
}

@Composable
private fun StreamingCursorPill(
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.extendedColors
    val infiniteTransition = rememberInfiniteTransition(label = "streaming_cursor")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(480),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )

    Box(
        modifier = modifier
            .size(width = 8.dp, height = 15.dp)
            .alpha(alpha)
            .clip(RoundedCornerShape(2.dp))
            .background(colors.accentBlue)
    )
}
