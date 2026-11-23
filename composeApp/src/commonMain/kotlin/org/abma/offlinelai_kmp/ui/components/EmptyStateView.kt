package org.abma.offlinelai_kmp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.abma.offlinelai_kmp.ui.theme.RelayBadge
import org.abma.offlinelai_kmp.ui.theme.RelayIconBadge
import org.abma.offlinelai_kmp.ui.theme.RelayMonoCaps
import org.abma.offlinelai_kmp.ui.theme.RelayOrbMode
import org.abma.offlinelai_kmp.ui.theme.RelayPrismOrb
import org.abma.offlinelai_kmp.ui.theme.RelayRadius
import org.abma.offlinelai_kmp.ui.theme.RelaySurfaceCard
import org.abma.offlinelai_kmp.ui.theme.extendedColors

enum class EmptyStateType {
    NO_MODEL,
    NO_MESSAGES,
    ERROR,
    LOADING
}

private data class WorkspaceStarterCard(
    val title: String,
    val subtitle: String,
    val tag: String,
    val icon: ImageVector,
    val tint: Color,
    val prompt: String
)

/**
 * Relay `WorkspaceStartView` & Sentient OS Empty State Component.
 *
 * Features the orbital `RelayPrismOrb`, monospace eyebrow, high-contrast Relay primary CTA button,
 * and interactive Starter Draft & Hardware Tool cards when ready to chat.
 */
@Composable
fun EmptyStateView(
    type: EmptyStateType,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    onSelectStarterPrompt: ((String) -> Unit)? = null,
    onOpenWorkbench: (() -> Unit)? = null
) {
    val colors = MaterialTheme.extendedColors

    val orbMode = when (type) {
        EmptyStateType.ERROR -> RelayOrbMode.ATTENTION
        EmptyStateType.LOADING -> RelayOrbMode.PROCESSING
        EmptyStateType.NO_MODEL -> RelayOrbMode.ATTENTION
        EmptyStateType.NO_MESSAGES -> RelayOrbMode.IDLE
    }

    val eyebrowText = when (type) {
        EmptyStateType.NO_MODEL -> "SENTIENT OS · MODEL RUNTIME REQUIRED"
        EmptyStateType.NO_MESSAGES -> "SENTIENT OS · AIR-GAPPED NEURAL WORKSPACE"
        EmptyStateType.ERROR -> "SENTIENT OS · RUNTIME DIAGNOSTIC ALERT"
        EmptyStateType.LOADING -> "SENTIENT OS · INITIALIZING TENSORS"
    }

    val eyebrowColor = when (type) {
        EmptyStateType.ERROR -> colors.accentCoral
        EmptyStateType.NO_MODEL -> colors.accentAmber
        else -> colors.workbenchAccent
    }

    val starterCards = listOf(
        WorkspaceStarterCard(
            title = "Toggle Device Torch",
            subtitle = "Hardware bridge · toggle_torch",
            tag = "TOOL",
            icon = Icons.Default.FlashlightOn,
            tint = colors.accentAmber,
            prompt = "Turn on the flashlight"
        ),
        WorkspaceStarterCard(
            title = "KMP Architecture Brief",
            subtitle = "Design clean expect/actual state flows",
            tag = "CODE",
            icon = Icons.Default.Code,
            tint = colors.accentAmethyst,
            prompt = "Explain best practices for Kotlin Multiplatform expect/actual architecture with Compose StateFlow."
        ),
        WorkspaceStarterCard(
            title = "Extract Action Items",
            subtitle = "Turn rough meeting notes into tasks",
            tag = "DRAFT",
            icon = Icons.Default.AutoAwesome,
            tint = colors.accentBlue,
            prompt = "Extract action items with owner and deadline from: Sara will finalize the LiteRT benchmark slides by Thursday 5 PM, and Omar will verify iOS torch permissions tomorrow morning."
        ),
        WorkspaceStarterCard(
            title = "Device Clock & Status",
            subtitle = "Inspect current ISO-8601 device time",
            tag = "OS",
            icon = Icons.Default.AccessTime,
            tint = colors.accentCyan,
            prompt = "What is the current device time?"
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier.widthIn(max = 540.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RelayPrismOrb(
                mode = orbMode,
                size = 86.dp
            )

            Spacer(Modifier.height(16.dp))

            RelayMonoCaps(
                text = eyebrowText,
                color = eyebrowColor
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )

            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(20.dp))

                // RelayGlowButton high-contrast capsule CTA
                Surface(
                    onClick = onAction,
                    shape = RoundedCornerShape(RelayRadius.pill),
                    color = colors.buttonFace,
                    border = BorderStroke(1.dp, colors.borderDefault)
                ) {
                    Row(
                        modifier = Modifier
                            .height(38.dp)
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = actionLabel,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = colors.buttonInk
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = colors.buttonInk,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Starter Prompt & Tool Deck Cards when ready to chat
            if (type == EmptyStateType.NO_MESSAGES && onSelectStarterPrompt != null) {
                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RelayMonoCaps(
                        text = "QUICK STARTS & DEVICE TOOLS",
                        color = colors.textMuted
                    )
                    if (onOpenWorkbench != null) {
                        Surface(
                            onClick = onOpenWorkbench,
                            shape = RoundedCornerShape(RelayRadius.pill),
                            color = colors.subtleFill,
                            border = BorderStroke(1.dp, colors.borderSubtle)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = colors.workbenchAccent,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Open Offline Workbench →",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp
                                    ),
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    starterCards.chunked(2).forEach { rowCards ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowCards.forEach { card ->
                                RelaySurfaceCard(
                                    modifier = Modifier.weight(1f),
                                    cornerRadius = RelayRadius.md,
                                    elevated = false,
                                    contentPadding = PaddingValues(12.dp),
                                    onClick = { onSelectStarterPrompt(card.prompt) }
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RelayIconBadge(
                                                icon = card.icon,
                                                tint = card.tint,
                                                size = 32.dp,
                                                iconSize = 15.dp
                                            )
                                            RelayBadge(
                                                text = card.tag,
                                                tint = card.tint,
                                                prominent = true,
                                                monospace = true
                                            )
                                        }
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = card.title,
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.sp
                                                ),
                                                color = colors.textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = card.subtitle,
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                                color = colors.textSecondary,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
