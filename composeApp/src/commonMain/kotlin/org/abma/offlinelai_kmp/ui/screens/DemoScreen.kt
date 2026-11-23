package org.abma.offlinelai_kmp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.abma.offlinelai_kmp.demo.*
import org.abma.offlinelai_kmp.domain.model.ModelState
import org.abma.offlinelai_kmp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoScreen(
    controller: DemoController,
    modelState: ModelState,
    modelPath: String?,
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onFlashlight: () -> Unit
) {
    val state by controller.state.collectAsState()
    var showReference by rememberSaveable { mutableStateOf(false) }
    var showRaw by rememberSaveable { mutableStateOf(false) }
    val colors = MaterialTheme.extendedColors

    val activeModelName = modelPath
        ?.substringAfterLast('/')
        ?.substringBeforeLast('.')
        ?: "No model loaded"

    RelayAtmosphere(
        modifier = Modifier.fillMaxSize(),
        focused = state.running
    ) {
        Scaffold(
            topBar = {
                DemoWorkbenchTopBar(
                    modelState = modelState,
                    activeModelName = activeModelName,
                    timing = state.timing,
                    onBack = {
                        controller.cancel()
                        onBack()
                    }
                )
            },
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // ═══ BANNER: MODEL NOT LOADED WARNING ═══
                if (modelState == ModelState.NOT_LOADED || modelState == ModelState.ERROR) {
                    RelaySurfaceCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = RelayRadius.lg,
                        strokeColor = colors.accentAmber.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RelayIconBadge(
                                    icon = Icons.Default.WarningAmber,
                                    tint = colors.accentAmber,
                                    size = 36.dp,
                                    iconSize = 18.dp
                                )
                                Column {
                                    Text(
                                        text = "No Active On-Device Model",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Load a compatible LiteRT-LM model in Runtime & Models before running a workflow.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                onClick = onSettings,
                                shape = RoundedCornerShape(RelayRadius.pill),
                                color = colors.buttonFace
                            ) {
                                Text(
                                    text = "Models",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.buttonInk,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // ═══ DEVICE HARDWARE CONTROL BRIDGE (FLASHLIGHT HANDOFF) ═══
                RelaySurfaceCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = RelayRadius.lg
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RelayIconBadge(
                                icon = Icons.Default.FlashlightOn,
                                tint = colors.accentAmber,
                                size = 38.dp,
                                iconSize = 18.dp
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                RelayMonoCaps(
                                    text = "DEVICE TOOLING BRIDGE",
                                    color = colors.accentAmber,
                                    fontSize = 9.5.sp
                                )
                                Text(
                                    text = "Hardware Flashlight Control in Chat",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Hands off a torch prompt to Chat to inspect model JSON proposal & device execution.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        Spacer(Modifier.width(10.dp))

                        Surface(
                            onClick = onFlashlight,
                            enabled = !state.running && modelState != ModelState.GENERATING && modelState != ModelState.LOADING,
                            shape = RoundedCornerShape(RelayRadius.pill),
                            color = colors.subtleFill,
                            border = BorderStroke(1.dp, colors.borderDefault)
                        ) {
                            Text(
                                text = "Test in Chat",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                // ═══ WORKFLOW SEGMENTED SWITCHER ═══
                Surface(
                    shape = RoundedCornerShape(RelayRadius.md),
                    color = colors.pitch,
                    border = BorderStroke(1.dp, colors.borderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DemoKind.entries.forEachIndexed { idx, kind ->
                            val selected = state.kind == kind
                            Surface(
                                onClick = { controller.select(kind) },
                                enabled = !state.running,
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(RelayRadius.sm),
                                color = if (selected) colors.surfaceElevated else Color.Transparent,
                                border = if (selected) BorderStroke(1.dp, colors.borderDefault) else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "0${idx + 1} · ${kind.title}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (selected) colors.textPrimary else colors.textMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // ═══ WORKFLOW 1: ACTION ITEMS EXTRACTOR ═══
                if (state.kind == DemoKind.NOTES) {
                    RelaySurfaceCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = RelayRadius.lg
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    RelayMonoCaps(
                                        text = "VERIFIABLE EXTRACTION WORKFLOW",
                                        color = colors.accentBlue,
                                        fontSize = 9.5.sp
                                    )
                                    Text(
                                        text = "Turn Notes into Grounded Action Items",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = colors.textPrimary
                                    )
                                }

                                Surface(
                                    onClick = { controller.updateNotes(DemoSamples.notes) },
                                    enabled = !state.running,
                                    shape = RoundedCornerShape(RelayRadius.pill),
                                    color = colors.subtleFill,
                                    border = BorderStroke(1.dp, colors.borderSubtle)
                                ) {
                                    Text(
                                        text = "Sample Notes",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.textSecondary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Extracts explicit commitments and verifies that every task, owner, and deadline exists verbatim in the source sentence.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )

                            OutlinedTextField(
                                value = state.notes,
                                onValueChange = controller::updateNotes,
                                enabled = !state.running,
                                label = { Text("Meeting notes corpus") },
                                minLines = 5,
                                maxLines = 10,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(RelayRadius.sm),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = colors.pitch,
                                    unfocusedContainerColor = colors.pitch,
                                    disabledContainerColor = colors.pitch,
                                    focusedBorderColor = colors.borderFocus,
                                    unfocusedBorderColor = colors.borderDefault,
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary
                                ),
                                supportingText = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Unagreed ideas are filtered out automatically",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.textMuted
                                        )
                                        Text(
                                            text = "${state.notes.length}/${DemoSamples.MAX_NOTES_LENGTH}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.textMuted
                                        )
                                    }
                                }
                            )

                            WorkflowActionFooter(
                                running = state.running,
                                rawResponseLength = state.rawResponse.length,
                                modelState = modelState,
                                primaryLabel = "Extract & Verify Action Items",
                                onRun = controller::run,
                                onCancel = controller::cancel
                            )
                        }
                    }
                } else {
                    // ═══ WORKFLOW 2: LOCAL CONFERENCE FAQ ═══
                    RelaySurfaceCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = RelayRadius.lg
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    RelayMonoCaps(
                                        text = "GROUNDED CITATION VERIFIER",
                                        color = colors.workbenchAccent,
                                        fontSize = 9.5.sp
                                    )
                                    Text(
                                        text = "Ask the Bundled Conference FAQ",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = colors.textPrimary
                                    )
                                }

                                Surface(
                                    onClick = { showReference = !showReference },
                                    shape = RoundedCornerShape(RelayRadius.pill),
                                    color = colors.subtleFill,
                                    border = BorderStroke(1.dp, colors.borderSubtle)
                                ) {
                                    Text(
                                        text = if (showReference) "Hide Corpus" else "View Corpus",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.textSecondary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }

                            Text(
                                text = "The model selects a passage from the local reference below. The app verifies that the quote is an exact substring before displaying it.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )

                            OutlinedTextField(
                                value = state.question,
                                onValueChange = controller::updateQuestion,
                                enabled = !state.running,
                                label = { Text("Question") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3,
                                shape = RoundedCornerShape(RelayRadius.sm),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = colors.pitch,
                                    unfocusedContainerColor = colors.pitch,
                                    disabledContainerColor = colors.pitch,
                                    focusedBorderColor = colors.borderFocus,
                                    unfocusedBorderColor = colors.borderDefault,
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary
                                )
                            )

                            // Sample questions chips
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                RelayMonoCaps(
                                    text = "SAMPLE QUERIES",
                                    color = colors.textMuted,
                                    fontSize = 9.sp
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    DemoSamples.questions.forEach { q ->
                                        val isCurrent = state.question == q
                                        Surface(
                                            onClick = { controller.updateQuestion(q) },
                                            enabled = !state.running,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(RelayRadius.xs),
                                            color = if (isCurrent) colors.activeFill else colors.pitch,
                                            border = BorderStroke(
                                                1.dp,
                                                if (isCurrent) colors.accentCyan else colors.borderSubtle
                                            )
                                        ) {
                                            Text(
                                                text = q,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (isCurrent) colors.textPrimary else colors.textSecondary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            if (showReference) {
                                Surface(
                                    shape = RoundedCornerShape(RelayRadius.sm),
                                    color = colors.pitch,
                                    border = BorderStroke(1.dp, colors.borderSubtle),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        RelayMonoCaps(
                                            text = "BUNDLED REFERENCE CORPUS (${DemoSamples.sections.size} SECTIONS)",
                                            color = colors.accentCyan,
                                            fontSize = 9.5.sp
                                        )
                                        DemoSamples.sections.forEach { section ->
                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Text(
                                                    text = "[${section.id}] ${section.title}",
                                                    style = MaterialTheme.typography.labelLarge.copy(
                                                        fontFamily = FontFamily.Monospace
                                                    ),
                                                    color = colors.textPrimary
                                                )
                                                Text(
                                                    text = section.text,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = colors.textSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            WorkflowActionFooter(
                                running = state.running,
                                rawResponseLength = state.rawResponse.length,
                                modelState = modelState,
                                primaryLabel = "Find & Verify Grounded Answer",
                                onRun = controller::run,
                                onCancel = controller::cancel
                            )
                        }
                    }
                }

                // ═══ ERROR BANNER ═══
                state.error?.let { errText ->
                    RelaySurfaceCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = RelayRadius.md,
                        strokeColor = colors.accentCoral.copy(alpha = 0.5f)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = colors.accentCoral,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = errText,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.accentCoral
                            )
                        }
                    }
                }

                // ═══ RESULTS: ACTION ITEMS CARDS ═══
                if (state.kind == DemoKind.NOTES) {
                    state.items?.let { items ->
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    RelayMonoCaps(
                                        text = "EXTRACTION OUTPUT",
                                        color = colors.accentEmerald,
                                        fontSize = 9.5.sp
                                    )
                                    Text(
                                        text = if (items.isEmpty()) "No Explicit Commitments Found" else "${items.size} Action Items to Review",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = colors.textPrimary
                                    )
                                }
                                RelayBadge(
                                    text = "EVIDENCE CHECKED",
                                    icon = Icons.Default.VerifiedUser,
                                    tint = colors.accentEmerald,
                                    prominent = true,
                                    monospace = true
                                )
                            }

                            items.forEachIndexed { index, item ->
                                RelaySurfaceCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    cornerRadius = RelayRadius.md,
                                    strokeColor = colors.borderDefault
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RelayBadge(
                                                text = "ACTION #${index + 1}",
                                                tint = colors.accentBlue,
                                                prominent = true,
                                                monospace = true
                                            )
                                            RelayBadge(
                                                text = "VERIFIED SUBSTRING",
                                                tint = colors.accentEmerald,
                                                monospace = true
                                            )
                                        }

                                        OutlinedTextField(
                                            value = item.task,
                                            onValueChange = { controller.editItem(index, item.copy(task = it)) },
                                            label = { Text("Task ${index + 1}") },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(RelayRadius.xs),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = colors.pitch,
                                                unfocusedContainerColor = colors.pitch,
                                                focusedBorderColor = colors.borderFocus,
                                                unfocusedBorderColor = colors.borderSubtle
                                            )
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = item.owner.orEmpty(),
                                                onValueChange = {
                                                    controller.editItem(index, item.copy(owner = it.ifBlank { null }))
                                                },
                                                label = { Text("Owner") },
                                                placeholder = { Text("Not specified") },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                                shape = RoundedCornerShape(RelayRadius.xs),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedContainerColor = colors.pitch,
                                                    unfocusedContainerColor = colors.pitch,
                                                    focusedBorderColor = colors.borderFocus,
                                                    unfocusedBorderColor = colors.borderSubtle
                                                )
                                            )

                                            OutlinedTextField(
                                                value = item.deadline.orEmpty(),
                                                onValueChange = {
                                                    controller.editItem(index, item.copy(deadline = it.ifBlank { null }))
                                                },
                                                label = { Text("Deadline") },
                                                placeholder = { Text("Not specified") },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                                shape = RoundedCornerShape(RelayRadius.xs),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedContainerColor = colors.pitch,
                                                    unfocusedContainerColor = colors.pitch,
                                                    focusedBorderColor = colors.borderFocus,
                                                    unfocusedBorderColor = colors.borderSubtle
                                                )
                                            )
                                        }

                                        // Verified Source Evidence Footer
                                        Surface(
                                            shape = RoundedCornerShape(RelayRadius.xs),
                                            color = colors.pitch,
                                            border = BorderStroke(1.dp, colors.accentEmerald.copy(alpha = 0.25f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.VerifiedUser,
                                                    contentDescription = null,
                                                    tint = colors.accentEmerald,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "Source: “${item.evidence}”",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = colors.textSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ═══ RESULTS: GROUNDED FAQ ANSWER CARD ═══
                if (state.kind == DemoKind.FAQ) {
                    state.answer?.let { answer ->
                        val verified = answer.section != null
                        RelaySurfaceCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = RelayRadius.lg,
                            strokeColor = if (verified) colors.accentEmerald.copy(alpha = 0.55f) else colors.accentAmber.copy(alpha = 0.55f)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (verified) "Verified Reference Answer" else "Not Found in Reference Corpus",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = colors.textPrimary
                                    )
                                    RelayBadge(
                                        text = if (verified) "VERIFIED QUOTE" else "ABSTAINED",
                                        icon = if (verified) Icons.Default.VerifiedUser else Icons.Default.Info,
                                        tint = if (verified) colors.accentEmerald else colors.accentAmber,
                                        prominent = true,
                                        monospace = true
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(RelayRadius.sm),
                                    color = colors.pitch,
                                    border = BorderStroke(1.dp, colors.borderSubtle),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = answer.quote
                                            ?: "The bundled corpus does not contain an answer to this question. Try asking about the workshop location, setup requirements, offline demos, or help desk.",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = colors.textPrimary,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }

                                answer.section?.let { sec ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        RelayMonoCaps(
                                            text = "CITATION SOURCE:",
                                            color = colors.textMuted,
                                            fontSize = 9.5.sp
                                        )
                                        RelayBadge(
                                            text = "[${sec.id}] ${sec.title}",
                                            tint = colors.accentCyan,
                                            prominent = true,
                                            monospace = true
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ═══ TELEMETRY & RAW MODEL OUTPUT INSPECTOR ═══
                if (state.timing != null || state.rawResponse.isNotBlank()) {
                    RelaySurfaceCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = RelayRadius.md
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            state.timing?.let { timing ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        RelayMonoCaps(
                                            text = "RUN TELEMETRY",
                                            color = colors.accentCyan,
                                            fontSize = 9.5.sp
                                        )
                                        RelayBadge(
                                            text = "First chunk: ${timing.firstChunkMs?.let { "$it ms" } ?: "n/a"}",
                                            tint = colors.accentBlue,
                                            monospace = true
                                        )
                                        RelayBadge(
                                            text = "Total: ${timing.totalMs} ms",
                                            tint = colors.accentEmerald,
                                            monospace = true
                                        )
                                    }

                                    if (state.rawResponse.isNotBlank()) {
                                        Surface(
                                            onClick = { showRaw = !showRaw },
                                            shape = RoundedCornerShape(RelayRadius.pill),
                                            color = colors.subtleFill,
                                            border = BorderStroke(1.dp, colors.borderSubtle)
                                        ) {
                                            Text(
                                                text = if (showRaw) "Hide JSON" else "Inspect JSON",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = colors.textSecondary,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (showRaw && state.rawResponse.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(RelayRadius.xs),
                                    color = colors.pitch,
                                    border = BorderStroke(1.dp, colors.borderSubtle),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = state.rawResponse,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        color = colors.textBright,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(36.dp))
            }
        }
    }
}

@Composable
private fun DemoWorkbenchTopBar(
    modelState: ModelState,
    activeModelName: String,
    timing: DemoTiming?,
    onBack: () -> Unit
) {
    val colors = MaterialTheme.extendedColors
    val statusColor = when (modelState) {
        ModelState.READY -> colors.accentEmerald
        ModelState.GENERATING, ModelState.LOADING -> colors.accentBlue
        ModelState.ERROR -> colors.accentCoral
        ModelState.NOT_LOADED -> colors.textMuted
    }

    Surface(
        color = colors.glassBackdrop,
        tonalElevation = 0.dp,
        modifier = Modifier.statusBarsPadding()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        onClick = onBack,
                        shape = RoundedCornerShape(RelayRadius.pill),
                        color = colors.subtleFill,
                        border = BorderStroke(1.dp, colors.borderDefault),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to chat",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Chat",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.textPrimary
                            )
                        }
                    }

                    Column {
                        RelayMonoCaps(
                            text = "SENTIENT WORKBENCH",
                            color = colors.workbenchAccent,
                            fontSize = 9.5.sp
                        )
                        Text(
                            text = "Offline Demos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    timing?.let { t ->
                        RelayBadge(
                            text = "${t.totalMs}ms",
                            icon = Icons.Outlined.Bolt,
                            tint = colors.accentCyan,
                            prominent = true,
                            monospace = true
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(RelayRadius.pill),
                        color = colors.subtleFill,
                        border = BorderStroke(1.dp, colors.borderSubtle),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            RelayHealthDot(
                                color = statusColor,
                                pulsing = modelState == ModelState.GENERATING || modelState == ModelState.LOADING
                            )
                            Text(
                                text = activeModelName,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 110.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                color = colors.borderSubtle,
                thickness = 1.dp
            )
        }
    }
}

@Composable
private fun WorkflowActionFooter(
    running: Boolean,
    rawResponseLength: Int,
    modelState: ModelState,
    primaryLabel: String,
    onRun: () -> Unit,
    onCancel: () -> Unit
) {
    val colors = MaterialTheme.extendedColors

    if (running) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            NotchThinkingUnderlight(
                active = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RelayHealthDot(color = colors.accentBlue, pulsing = true)
                    Text(
                        text = "Streaming on device… $rawResponseLength chars received",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textSecondary
                    )
                }

                Surface(
                    onClick = onCancel,
                    shape = RoundedCornerShape(RelayRadius.pill),
                    color = colors.accentCoral.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, colors.accentCoral.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Stop",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.accentCoral,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    } else {
        Button(
            onClick = onRun,
            enabled = modelState == ModelState.READY,
            modifier = Modifier.fillMaxWidth().height(42.dp),
            shape = RoundedCornerShape(RelayRadius.sm),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.buttonFace,
                contentColor = colors.buttonInk,
                disabledContainerColor = colors.subtleFill,
                disabledContentColor = colors.textDim
            )
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = primaryLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (modelState == ModelState.LOADING || modelState == ModelState.GENERATING) {
            Text(
                text = "Waiting for active model session to complete…",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted
            )
        }
    }
}
