package org.abma.offlinelai_kmp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.abma.offlinelai_kmp.demo.*
import org.abma.offlinelai_kmp.domain.model.ModelState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoScreen(controller: DemoController, modelState: ModelState, modelPath: String?, onBack: () -> Unit, onSettings: () -> Unit, onFlashlight: () -> Unit) {
    val state by controller.state.collectAsState()
    var showReference by rememberSaveable { mutableStateOf(false) }
    var showRaw by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Offline demos") }, navigationIcon = {
                IconButton(onClick = { controller.cancel(); onBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to chat")
                }
            })
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(modelPath?.substringAfterLast('/') ?: "No model loaded", style = MaterialTheme.typography.labelLarge)
            Text("Fictional samples. Inference runs on the loaded model. Inputs stay in memory. A new run replaces previous results.", style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = onFlashlight, enabled = !state.running && modelState != ModelState.GENERATING && modelState != ModelState.LOADING) {
                Text("Flashlight control in chat")
            }
            Text("Ask the model to turn the flashlight on or off. The app shows its proposed call and the device result. Requires a phone with a flashlight.", style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DemoKind.entries.forEach { kind ->
                    FilterChip(selected = state.kind == kind, onClick = { controller.select(kind) }, enabled = !state.running, label = { Text(kind.title) })
                }
            }
            if (state.kind == DemoKind.NOTES) {
                Text("Turn notes into action items", style = MaterialTheme.typography.headlineSmall)
                Text("Extract commitments, check their source, then edit the result. Missing owners and deadlines stay unspecified.")
                OutlinedTextField(value = state.notes, onValueChange = controller::updateNotes, enabled = !state.running, label = { Text("Meeting notes") }, minLines = 5, maxLines = 10, modifier = Modifier.fillMaxWidth(), supportingText = { Text("${state.notes.length}/${DemoSamples.MAX_NOTES_LENGTH}") })
                TextButton(onClick = { controller.updateNotes(DemoSamples.notes) }, enabled = !state.running) { Text("Use sample notes") }
            } else {
                Text("Ask the local conference FAQ", style = MaterialTheme.typography.headlineSmall)
                Text("The model selects a passage from the bundled reference. The app checks that the quote exists before showing it.")
                OutlinedTextField(value = state.question, onValueChange = controller::updateQuestion, enabled = !state.running, label = { Text("Question") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                DemoSamples.questions.forEach { question ->
                    TextButton(onClick = { controller.updateQuestion(question) }, enabled = !state.running) { Text(question) }
                }
                TextButton(onClick = { showReference = !showReference }) { Text(if (showReference) "Hide reference" else "Read bundled reference") }
                if (showReference) DemoSamples.sections.forEach { section ->
                    Text("[${section.id}] ${section.title}", style = MaterialTheme.typography.titleSmall)
                    Text(section.text, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (modelState == ModelState.NOT_LOADED || modelState == ModelState.ERROR) {
                Text("Load a compatible text model before running a demo.")
                OutlinedButton(onClick = onSettings) { Text("Open Settings") }
            }
            if (state.running) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Text("Generating on device… ${state.rawResponse.length} characters received")
                OutlinedButton(onClick = controller::cancel) { Text("Stop generation") }
            } else {
                Button(onClick = controller::run, enabled = modelState == ModelState.READY, modifier = Modifier.fillMaxWidth()) {
                    Text(if (state.kind == DemoKind.NOTES) "Extract action items" else "Find an answer")
                }
                if (modelState == ModelState.LOADING || modelState == ModelState.GENERATING) Text("Waiting for the model…")
            }
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (state.kind == DemoKind.NOTES) state.items?.let { items ->
                HorizontalDivider()
                Text(if (items.isEmpty()) "No commitments found" else "${items.size} action items to review", style = MaterialTheme.typography.titleLarge)
                Text("Check meaning against the evidence. Changes below are yours and are not revalidated.", style = MaterialTheme.typography.bodySmall)
                items.forEachIndexed { index, item ->
                    OutlinedTextField(item.task, { controller.editItem(index, item.copy(task = it)) }, label = { Text("Task ${index + 1}") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(item.owner.orEmpty(), { controller.editItem(index, item.copy(owner = it.ifBlank { null })) }, label = { Text("Owner") }, placeholder = { Text("Not specified") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(item.deadline.orEmpty(), { controller.editItem(index, item.copy(deadline = it.ifBlank { null })) }, label = { Text("Deadline") }, placeholder = { Text("Not specified") }, modifier = Modifier.fillMaxWidth())
                    Text("Source: “${item.evidence}”", style = MaterialTheme.typography.bodySmall)
                    HorizontalDivider()
                }
            }
            if (state.kind == DemoKind.FAQ) state.answer?.let { answer ->
                HorizontalDivider()
                Text(if (answer.section == null) "Not found in this reference" else "Answer from the reference", style = MaterialTheme.typography.titleLarge)
                Text(answer.quote ?: "Try a question about the workshop location, setup, offline demos, or help desk.")
                answer.section?.let { Text("Source: [${it.id}] ${it.title}", style = MaterialTheme.typography.labelLarge) }
            }
            state.timing?.let {
                Text("First response: ${it.firstChunkMs?.let { ms -> "$ms ms" } ?: "none"} · Total: ${it.totalMs} ms", style = MaterialTheme.typography.labelMedium)
                Text("Measured for this run. First response means the first nonempty text chunk, not a tokenizer measurement.", style = MaterialTheme.typography.bodySmall)
            }
            if (state.rawResponse.isNotBlank()) {
                TextButton(onClick = { showRaw = !showRaw }) { Text(if (showRaw) "Hide model output" else "Inspect model output") }
                if (showRaw) Text(state.rawResponse, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
