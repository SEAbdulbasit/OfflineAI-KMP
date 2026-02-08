import SwiftUI
import ComposeApp

class ChatViewModelObservable: ObservableObject {
    private let wrapper = IosChatViewModelWrapper()

    @Published var messages: [IosChatMessage] = []
    @Published var modelState: String = "NOT_LOADED"
    @Published var loadingProgress: Float = 0.0
    @Published var currentInput: String = ""
    @Published var errorMessage: String? = nil
    @Published var currentModelPath: String? = nil
    @Published var loadedModels: [IosLoadedModel] = []
    @Published var isGenerating: Bool = false
    @Published var isToolCallInProgress: Bool = false

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
                self?.isToolCallInProgress = state.isToolCallInProgress
            }
        }
    }

    func loadModel(path: String) {
        wrapper.loadModel(modelPath: path)
    }

    func sendMessage() {
        wrapper.sendMessage(text: currentInput)
        currentInput = ""
    }

    func updateInput(_ text: String) {
        currentInput = text
    }

    func clearChat() {
        wrapper.clearChat()
    }

    func dismissError() {
        wrapper.dismissError()
    }

    func removeModel(path: String) {
        wrapper.removeModel(path: path)
    }

    func refreshModels() {
        wrapper.refreshModels()
    }

    deinit {
        wrapper.dispose()
    }
}

struct ChatView: View {
    @StateObject private var viewModel = ChatViewModelObservable()
    @State private var showSettings = false

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                if viewModel.modelState == "NOT_LOADED" {
                    EmptyStateView(
                        title: "No Model Loaded",
                        message: "Go to Settings to select and load a Gemma model",
                        actionLabel: "Open Settings"
                    ) {
                        showSettings = true
                    }
                } else if viewModel.modelState == "LOADING" {
                    LoadingView(progress: viewModel.loadingProgress)
                } else if viewModel.modelState == "ERROR" {
                    EmptyStateView(
                        title: "Error",
                        message: viewModel.errorMessage ?? "Failed to load model",
                        actionLabel: "Try Again"
                    ) {
                        showSettings = true
                    }
                } else {
                    MessageListView(messages: viewModel.messages)
                }

                ChatInputBar(
                    text: $viewModel.currentInput,
                    isEnabled: viewModel.modelState == "READY",
                    isGenerating: viewModel.isGenerating
                ) {
                    viewModel.sendMessage()
                }
            }
            .navigationTitle("Gemma Chat")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        showSettings = true
                    } label: {
                        Image(systemName: "gearshape.fill")
                    }
                }

                ToolbarItem(placement: .navigationBarLeading) {
                    if !viewModel.messages.isEmpty {
                        Button {
                            viewModel.clearChat()
                        } label: {
                            Image(systemName: "trash")
                        }
                    }
                }
            }
            .sheet(isPresented: $showSettings) {
                SettingsView(viewModel: viewModel)
            }
        }
    }
}

struct MessageListView: View {
    let messages: [IosChatMessage]

    var body: some View {
        ScrollViewReader { proxy in
            ScrollView {
                LazyVStack(spacing: 12) {
                    ForEach(messages, id: \.id) { message in
                        MessageBubble(message: message)
                            .id(message.id)
                    }
                }
                .padding()
            }
            .onChange(of: messages.count) { oldValue, newValue in
                if let lastMessage = messages.last {
                    withAnimation {
                        proxy.scrollTo(lastMessage.id, anchor: .bottom)
                    }
                }
            }
        }
    }
}

struct MessageBubble: View {
    let message: IosChatMessage

    var body: some View {
        HStack {
            if message.isFromUser {
                Spacer()
            }

            VStack(alignment: message.isFromUser ? .trailing : .leading, spacing: 4) {
                Text(message.content)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
                    .background(
                        message.isFromUser
                            ? Color.blue
                            : (message.isError ? Color.red.opacity(0.2) : Color(.systemGray5))
                    )
                    .foregroundColor(message.isFromUser ? .white : .primary)
                    .cornerRadius(20)

                if message.isStreaming {
                    HStack(spacing: 4) {
                        ProgressView()
                            .scaleEffect(0.7)
                        Text("Generating...")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
            }

            if !message.isFromUser {
                Spacer()
            }
        }
    }
}

struct ChatInputBar: View {
    @Binding var text: String
    let isEnabled: Bool
    let isGenerating: Bool
    let onSend: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            TextField("Message Gemma...", text: $text)
                .textFieldStyle(.roundedBorder)
                .disabled(!isEnabled || isGenerating)

            Button(action: onSend) {
                Image(systemName: isGenerating ? "stop.fill" : "arrow.up.circle.fill")
                    .font(.title2)
                    .foregroundColor(text.isEmpty || !isEnabled ? .gray : .blue)
            }
            .disabled(text.isEmpty || !isEnabled)
        }
        .padding()
        .background(Color(.systemBackground))
    }
}

struct EmptyStateView: View {
    let title: String
    let message: String
    let actionLabel: String
    let action: () -> Void

    var body: some View {
        VStack(spacing: 20) {
            Spacer()

            Image(systemName: "cpu")
                .font(.system(size: 60))
                .foregroundColor(.blue)

            Text(title)
                .font(.title2)
                .fontWeight(.bold)

            Text(message)
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal)

            Button(action: action) {
                Text(actionLabel)
                    .fontWeight(.semibold)
                    .padding(.horizontal, 24)
                    .padding(.vertical, 12)
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(10)
            }

            Spacer()
        }
    }
}

struct LoadingView: View {
    let progress: Float

    var body: some View {
        VStack(spacing: 20) {
            Spacer()

            ProgressView()
                .scaleEffect(1.5)

            Text("Loading Model...")
                .font(.headline)

            ProgressView(value: Double(progress))
                .frame(width: 200)

            Text("\(Int(progress * 100))%")
                .font(.caption)
                .foregroundColor(.secondary)

            Spacer()
        }
    }
}

struct SettingsView: View {
    @ObservedObject var viewModel: ChatViewModelObservable
    @Environment(\.dismiss) var dismiss
    @State private var showFilePicker = false

    var body: some View {
        NavigationStack {
            List {
                Section("Import Model") {
                    Button {
                        showFilePicker = true
                    } label: {
                        HStack {
                            Image(systemName: "folder")
                            Text("Browse Files")
                        }
                    }
                }

                if !viewModel.loadedModels.isEmpty {
                    Section("Loaded Models") {
                        ForEach(viewModel.loadedModels, id: \.path) { model in
                            ModelRow(
                                model: model,
                                isActive: viewModel.currentModelPath == model.path,
                                onLoad: {
                                    viewModel.loadModel(path: model.path)
                                    dismiss()
                                },
                                onDelete: {
                                    viewModel.removeModel(path: model.path)
                                }
                            )
                        }
                    }
                }
            }
            .navigationTitle("Settings")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") {
                        dismiss()
                    }
                }
            }
            .fileImporter(
                isPresented: $showFilePicker,
                allowedContentTypes: [.data],
                allowsMultipleSelection: false
            ) { result in
                switch result {
                case .success(let urls):
                    if let url = urls.first {
                        handleFileSelection(url)
                    }
                case .failure(let error):
                    print("File picker error: \(error)")
                }
            }
        }
    }

    private func handleFileSelection(_ url: URL) {
        guard url.startAccessingSecurityScopedResource() else { return }
        defer { url.stopAccessingSecurityScopedResource() }

        let fileManager = FileManager.default
        let documentsDir = fileManager.urls(for: .documentDirectory, in: .userDomainMask).first!
        let destURL = documentsDir.appendingPathComponent(url.lastPathComponent)

        do {
            if fileManager.fileExists(atPath: destURL.path) {
                try fileManager.removeItem(at: destURL)
            }
            try fileManager.copyItem(at: url, to: destURL)
            viewModel.loadModel(path: destURL.path)
            dismiss()
        } catch {
            print("Error copying file: \(error)")
        }
    }
}

struct ModelRow: View {
    let model: IosLoadedModel
    let isActive: Bool
    let onLoad: () -> Void
    let onDelete: () -> Void

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text(model.name)
                        .fontWeight(.medium)

                    if isActive {
                        Image(systemName: "checkmark.circle.fill")
                            .foregroundColor(.green)
                            .font(.caption)
                    }
                }

                Text("On-device")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }

            Spacer()

            Button(action: onLoad) {
                Image(systemName: "play.fill")
                    .foregroundColor(.blue)
            }
            .buttonStyle(.plain)

            Button(action: onDelete) {
                Image(systemName: "trash")
                    .foregroundColor(.red)
            }
            .buttonStyle(.plain)
        }
        .padding(.vertical, 4)
    }
}

#Preview {
    ChatView()
}
