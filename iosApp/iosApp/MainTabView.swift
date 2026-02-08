import SwiftUI
import ComposeApp

// Observable wrapper for General Chat
class GeneralChatObservable: ObservableObject {
    private let wrapper = IosGeneralChatWrapper()

    @Published var messages: [IosChatMessage] = []
    @Published var modelState: String = "NOT_LOADED"
    @Published var loadingProgress: Float = 0.0
    @Published var currentInput: String = ""
    @Published var errorMessage: String? = nil
    @Published var currentModelPath: String? = nil
    @Published var loadedModels: [IosLoadedModel] = []
    @Published var isGenerating: Bool = false

    init() {
        wrapper.observeState { [weak self] state in
            DispatchQueue.main.async {
                self?.messages = state.messages
                self?.modelState = state.modelState
                self?.loadingProgress = state.loadingProgress
                self?.currentInput = state.currentInput
                self?.errorMessage = state.errorMessage
                self?.currentModelPath = state.currentModelPath
                self?.loadedModels = state.loadedModels
                self?.isGenerating = state.isGenerating
            }
        }
    }

    func loadModel(path: String) { wrapper.loadModel(modelPath: path) }
    func sendMessage() { wrapper.sendMessage(text: currentInput); currentInput = "" }
    func clearChat() { wrapper.clearChat() }
    func dismissError() { wrapper.dismissError() }
    func removeModel(path: String) { wrapper.removeModel(path: path) }
    func refreshModels() { wrapper.refreshModels() }
    deinit { wrapper.dispose() }
}

// Observable wrapper for Actions
class ActionsObservable: ObservableObject {
    private let wrapper = IosActionsWrapper()

    @Published var messages: [IosChatMessage] = []
    @Published var modelState: String = "NOT_LOADED"
    @Published var loadingProgress: Float = 0.0
    @Published var currentInput: String = ""
    @Published var errorMessage: String? = nil
    @Published var currentModelPath: String? = nil
    @Published var loadedModels: [IosLoadedModel] = []
    @Published var isGenerating: Bool = false

    init() {
        wrapper.observeState { [weak self] state in
            DispatchQueue.main.async {
                self?.messages = state.messages
                self?.modelState = state.modelState
                self?.loadingProgress = state.loadingProgress
                self?.currentInput = state.currentInput
                self?.errorMessage = state.errorMessage
                self?.currentModelPath = state.currentModelPath
                self?.loadedModels = state.loadedModels
                self?.isGenerating = state.isGenerating
            }
        }
    }

    func loadModel(path: String) { wrapper.loadModel(modelPath: path) }
    func sendMessage() { wrapper.sendMessage(text: currentInput); currentInput = "" }
    func clearChat() { wrapper.clearChat() }
    func dismissError() { wrapper.dismissError() }
    func removeModel(path: String) { wrapper.removeModel(path: path) }
    func refreshModels() { wrapper.refreshModels() }
    deinit { wrapper.dispose() }
}

// Main tabbed view
struct MainTabView: View {
    @StateObject private var chatViewModel = GeneralChatObservable()
    @StateObject private var actionsViewModel = ActionsObservable()
    @State private var selectedTab = 0
    @State private var showSettings = false

    var body: some View {
        TabView(selection: $selectedTab) {
            // General Chat Tab
            NavigationStack {
                ConversationView(
                    title: "Chat",
                    placeholder: "Ask me anything...",
                    messages: chatViewModel.messages,
                    modelState: chatViewModel.modelState,
                    loadingProgress: chatViewModel.loadingProgress,
                    currentInput: $chatViewModel.currentInput,
                    errorMessage: chatViewModel.errorMessage,
                    isGenerating: chatViewModel.isGenerating,
                    onSend: { chatViewModel.sendMessage() },
                    onClear: { chatViewModel.clearChat() },
                    onSettings: { showSettings = true }
                )
            }
            .tabItem {
                Label("Chat", systemImage: "bubble.left.and.bubble.right")
            }
            .tag(0)

            // Actions Tab
            NavigationStack {
                ConversationView(
                    title: "Actions",
                    placeholder: "Try: Open google.com, Call 123-456...",
                    messages: actionsViewModel.messages,
                    modelState: actionsViewModel.modelState,
                    loadingProgress: actionsViewModel.loadingProgress,
                    currentInput: $actionsViewModel.currentInput,
                    errorMessage: actionsViewModel.errorMessage,
                    isGenerating: actionsViewModel.isGenerating,
                    onSend: { actionsViewModel.sendMessage() },
                    onClear: { actionsViewModel.clearChat() },
                    onSettings: { showSettings = true }
                )
            }
            .tabItem {
                Label("Actions", systemImage: "bolt.fill")
            }
            .tag(1)
        }
        .sheet(isPresented: $showSettings) {
            ModelSettingsView(
                chatViewModel: chatViewModel,
                actionsViewModel: actionsViewModel
            )
        }
    }
}

// Reusable conversation view
struct ConversationView: View {
    let title: String
    let placeholder: String
    let messages: [IosChatMessage]
    let modelState: String
    let loadingProgress: Float
    @Binding var currentInput: String
    let errorMessage: String?
    let isGenerating: Bool
    let onSend: () -> Void
    let onClear: () -> Void
    let onSettings: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            if modelState == "NOT_LOADED" {
                ContentUnavailableView(
                    "No Model Loaded",
                    systemImage: "cpu",
                    description: Text("Go to Settings to load a model")
                )
            } else if modelState == "LOADING" {
                VStack(spacing: 16) {
                    ProgressView()
                        .scaleEffect(1.5)
                    Text("Loading model...")
                        .foregroundStyle(.secondary)
                    ProgressView(value: loadingProgress)
                        .frame(width: 200)
                }
            } else if modelState == "ERROR" {
                ContentUnavailableView(
                    "Error",
                    systemImage: "exclamationmark.triangle",
                    description: Text(errorMessage ?? "Failed to load model")
                )
            } else {
                // Messages
                ScrollViewReader { proxy in
                    ScrollView {
                        LazyVStack(spacing: 12) {
                            if messages.isEmpty {
                                Text("Start a conversation")
                                    .foregroundStyle(.secondary)
                                    .padding(.top, 100)
                            } else {
                                ForEach(messages, id: \.id) { message in
                                    MessageRow(message: message)
                                        .id(message.id)
                                }
                            }
                        }
                        .padding()
                    }
                    .onChange(of: messages.count) { _, _ in
                        if let last = messages.last {
                            withAnimation { proxy.scrollTo(last.id, anchor: .bottom) }
                        }
                    }
                }
            }

            // Input bar
            HStack(spacing: 12) {
                TextField(placeholder, text: $currentInput)
                    .textFieldStyle(.roundedBorder)
                    .disabled(modelState != "READY")

                Button {
                    onSend()
                } label: {
                    if isGenerating {
                        ProgressView()
                            .frame(width: 24, height: 24)
                    } else {
                        Image(systemName: "arrow.up.circle.fill")
                            .font(.title2)
                    }
                }
                .disabled(currentInput.isEmpty || modelState != "READY" || isGenerating)
            }
            .padding()
            .background(.bar)
        }
        .navigationTitle(title)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button { onSettings() } label: {
                    Image(systemName: "gearshape.fill")
                }
            }
            ToolbarItem(placement: .navigationBarLeading) {
                if !messages.isEmpty {
                    Button { onClear() } label: {
                        Image(systemName: "trash")
                    }
                }
            }
        }
    }
}

// Message row
struct MessageRow: View {
    let message: IosChatMessage

    var body: some View {
        HStack {
            if message.isFromUser { Spacer() }

            VStack(alignment: message.isFromUser ? .trailing : .leading) {
                Text(message.content)
                    .padding(12)
                    .background(message.isFromUser ? Color.blue : Color(.systemGray5))
                    .foregroundColor(message.isFromUser ? .white : .primary)
                    .cornerRadius(16)

                if message.isStreaming {
                    HStack(spacing: 4) {
                        ProgressView()
                            .scaleEffect(0.7)
                        Text("Generating...")
                            .font(.caption2)
                            .foregroundStyle(.secondary)
                    }
                }
            }
            .frame(maxWidth: UIScreen.main.bounds.width * 0.75, alignment: message.isFromUser ? .trailing : .leading)

            if !message.isFromUser { Spacer() }
        }
    }
}

// Model settings view
struct ModelSettingsView: View {
    @ObservedObject var chatViewModel: GeneralChatObservable
    @ObservedObject var actionsViewModel: ActionsObservable
    @Environment(\.dismiss) var dismiss
    @State private var showFilePicker = false

    var body: some View {
        NavigationStack {
            List {
                Section("Current Model") {
                    if let path = chatViewModel.currentModelPath ?? actionsViewModel.currentModelPath {
                        HStack {
                            Image(systemName: "cpu.fill")
                                .foregroundStyle(.blue)
                            Text(path.components(separatedBy: "/").last ?? path)
                        }
                    } else {
                        Text("No model loaded")
                            .foregroundStyle(.secondary)
                    }
                }

                Section("Load Model") {
                    Button {
                        showFilePicker = true
                    } label: {
                        Label("Select Model File", systemImage: "doc.badge.plus")
                    }
                }

                Section("Loaded Models") {
                    let models = chatViewModel.loadedModels.isEmpty ? actionsViewModel.loadedModels : chatViewModel.loadedModels
                    if models.isEmpty {
                        Text("No models loaded")
                            .foregroundStyle(.secondary)
                    } else {
                        ForEach(models, id: \.path) { model in
                            Button {
                                chatViewModel.loadModel(path: model.path)
                                actionsViewModel.loadModel(path: model.path)
                                dismiss()
                            } label: {
                                HStack {
                                    Text(model.name)
                                    Spacer()
                                    if model.path == chatViewModel.currentModelPath {
                                        Image(systemName: "checkmark")
                                            .foregroundStyle(.blue)
                                    }
                                }
                            }
                            .swipeActions {
                                Button(role: .destructive) {
                                    chatViewModel.removeModel(path: model.path)
                                    actionsViewModel.removeModel(path: model.path)
                                } label: {
                                    Label("Delete", systemImage: "trash")
                                }
                            }
                        }
                    }
                }
            }
            .navigationTitle("Settings")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") { dismiss() }
                }
            }
            .fileImporter(
                isPresented: $showFilePicker,
                allowedContentTypes: [.data],
                allowsMultipleSelection: false
            ) { result in
                if case .success(let urls) = result, let url = urls.first {
                    let path = copyModelToDocuments(url: url)
                    if let path = path {
                        chatViewModel.loadModel(path: path)
                        actionsViewModel.loadModel(path: path)
                        dismiss()
                    }
                }
            }
        }
    }

    private func copyModelToDocuments(url: URL) -> String? {
        guard url.startAccessingSecurityScopedResource() else { return nil }
        defer { url.stopAccessingSecurityScopedResource() }

        let documentsPath = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
        let destinationURL = documentsPath.appendingPathComponent(url.lastPathComponent)

        do {
            if FileManager.default.fileExists(atPath: destinationURL.path) {
                try FileManager.default.removeItem(at: destinationURL)
            }
            try FileManager.default.copyItem(at: url, to: destinationURL)
            return destinationURL.path
        } catch {
            print("Failed to copy model: \(error)")
            return nil
        }
    }
}
