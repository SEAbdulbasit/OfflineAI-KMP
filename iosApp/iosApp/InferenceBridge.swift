import Foundation
import LiteRTLM

public class InferenceBridge: NSObject {
    public static let shared = InferenceBridge()

    private var engine: Engine?
    private var currentModelPath: String?
    private var generationTask: Task<Void, Never>?
    private var isObserving = false

    private override init() {
        super.init()
    }

    public func start() {
        setupObserver()
    }

    private func setupObserver() {
        guard !isObserving else { return }
        isObserving = true
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleGenerateRequest(_:)),
            name: NSNotification.Name("GemmaGenerateRequest"),
            object: nil
        )
    }

    @objc private func handleGenerateRequest(_ notification: Notification) {
        guard let userInfo = notification.userInfo,
            let prompt = userInfo["prompt"] as? String,
            let modelPath = userInfo["modelPath"] as? String,
            !prompt.isEmpty,
            !modelPath.isEmpty
        else {
            postError("Invalid request")
            return
        }

        generationTask?.cancel()
        generationTask = Task { [weak self] in
            await self?.generateResponse(prompt: prompt, modelPath: modelPath)
        }
    }

    private func generateResponse(prompt: String, modelPath: String) async {
        do {
            try Task.checkCancellation()
            try await initializeEngineIfNeeded(modelPath: modelPath)
            try Task.checkCancellation()

            guard let engine else {
                throw NSError(
                    domain: "InferenceBridge",
                    code: -1,
                    userInfo: [NSLocalizedDescriptionKey: "Inference engine is unavailable"]
                )
            }

            let conversation = try await engine.createConversation()
            let message = Message(prompt)

            for try await chunk in conversation.sendMessageStream(message) {
                try Task.checkCancellation()
                postToken(chunk.toString)
            }

            postDone()
        } catch is CancellationError {
            postError("Generation cancelled")
        } catch {
            postError(error.localizedDescription)
        }
    }

    private func initializeEngineIfNeeded(modelPath: String) async throws {
        guard engine == nil || currentModelPath != modelPath else { return }

        engine = nil
        currentModelPath = nil

        guard FileManager.default.fileExists(atPath: modelPath) else {
            throw NSError(
                domain: "InferenceBridge",
                code: -2,
                userInfo: [NSLocalizedDescriptionKey: "Model file not found at \(modelPath)"]
            )
        }

        let config = try EngineConfig(
            modelPath: modelPath,
            backend: .gpu,
            cacheDir: try cacheDirectoryPath()
        )
        let newEngine = Engine(engineConfig: config)
        try await newEngine.initialize()

        engine = newEngine
        currentModelPath = modelPath
    }

    private func cacheDirectoryPath() throws -> String {
        guard let cachesDirectory = FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask).first else {
            throw NSError(
                domain: "InferenceBridge",
                code: -3,
                userInfo: [NSLocalizedDescriptionKey: "Unable to resolve cache directory"]
            )
        }

        let cacheURL = cachesDirectory.appendingPathComponent("LiteRTLM", isDirectory: true)
        try FileManager.default.createDirectory(
            at: cacheURL,
            withIntermediateDirectories: true,
            attributes: nil
        )
        return cacheURL.path
    }

    private func postToken(_ token: String) {
        postNotification(name: "GemmaTokenResponse", userInfo: ["token": token])
    }

    private func postDone() {
        postNotification(name: "GemmaGenerationDone", userInfo: nil)
    }

    private func postError(_ error: String) {
        postNotification(name: "GemmaGenerationError", userInfo: ["error": error])
    }

    private func postNotification(name: String, userInfo: [AnyHashable: Any]?) {
        DispatchQueue.main.async {
            NotificationCenter.default.post(
                name: NSNotification.Name(name),
                object: nil,
                userInfo: userInfo
            )
        }
    }

    public func close() {
        generationTask?.cancel()
        generationTask = nil
        engine = nil
        currentModelPath = nil
    }

    deinit {
        NotificationCenter.default.removeObserver(self)
    }
}
