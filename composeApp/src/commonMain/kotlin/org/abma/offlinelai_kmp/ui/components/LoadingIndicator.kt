package org.abma.offlinelai_kmp.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.abma.offlinelai_kmp.ui.theme.NotchThinkingUnderlight
import org.abma.offlinelai_kmp.ui.theme.RelayBadge
import org.abma.offlinelai_kmp.ui.theme.RelayGlowProgressBar
import org.abma.offlinelai_kmp.ui.theme.RelayMonoCaps
import org.abma.offlinelai_kmp.ui.theme.RelayOrbMode
import org.abma.offlinelai_kmp.ui.theme.RelayPrismOrb
import org.abma.offlinelai_kmp.ui.theme.RelayRadius
import org.abma.offlinelai_kmp.ui.theme.RelaySurfaceCard
import org.abma.offlinelai_kmp.ui.theme.extendedColors

/**
 * Relay & Sentient OS Neural Model Preparation & Loading View (`LoadingIndicator`).
 *
 * Features the animated `RelayPrismOrb(mode = PROCESSING)`, monospace progress readout,
 * `RelayGlowProgressBar`, and `NotchThinkingUnderlight`.
 */
@Composable
fun LoadingIndicator(
    message: String,
    progress: Float? = null,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.extendedColors
    val hasDeterminateProgress = progress != null && progress > 0f
    val clampedProgress = (progress ?: 0f).coerceIn(0f, 1f)

    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        RelaySurfaceCard(
            modifier = Modifier.widthIn(max = 360.dp),
            cornerRadius = RelayRadius.lg,
            elevated = false,
            contentPadding = PaddingValues(0.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RelayPrismOrb(
                        mode = RelayOrbMode.PROCESSING,
                        size = 80.dp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    RelayMonoCaps(
                        text = "SENTIENT OS · NEURAL ENGINELOADER",
                        color = colors.workbenchAccent
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = message,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Allocating on-device weights and KV-cache memory…",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )

                    if (hasDeterminateProgress) {
                        Spacer(modifier = Modifier.height(16.dp))

                        RelayBadge(
                            text = "${(clampedProgress * 100).toInt()}% LOADED",
                            tint = colors.accentCyan,
                            prominent = true,
                            monospace = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        RelayGlowProgressBar(
                            progress = clampedProgress,
                            modifier = Modifier.width(220.dp),
                            height = 6.dp
                        )
                    } else {
                        Spacer(modifier = Modifier.height(14.dp))

                        RelayBadge(
                            text = "WARMING UP TENSORS",
                            tint = colors.accentBlue,
                            prominent = true,
                            monospace = true
                        )
                    }
                }

                NotchThinkingUnderlight(
                    active = true,
                    height = 4.dp
                )
            }
        }
    }
}

@Composable
fun SpinningLoader(
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.extendedColors
    val infiniteTransition = rememberInfiniteTransition(label = "spinner")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing)
        ),
        label = "rotation"
    )

    CircularProgressIndicator(
        modifier = modifier.rotate(rotation),
        color = colors.accentBlue,
        trackColor = colors.surfaceElevated,
        strokeWidth = 3.dp,
        strokeCap = StrokeCap.Round
    )
}
