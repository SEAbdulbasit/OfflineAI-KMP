import Foundation
import MediaPipeTasksGenAI
import MediaPipeTasksGenAIC

@objc public class InferenceBridge: NSObject {

    @objc public static let shared = InferenceBridge()

    private var llmInference: LlmInference?
    private var currentModelPath: String?

    private override init() {
        super.init()
        setupObserver()
    }

    private func setupObserver() {
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
            let modelPath = userInfo["modelPath"] as? String
        else {
            storeResponse("Error: Invalid request")
            return
        }

        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
            guard let self = self else { return }

            // Load model if needed
            if self.llmInference == nil || self.currentModelPath != modelPath {
                guard self.loadModel(path: modelPath) else {
                    self.storeResponse("Error: Failed to load model")
                    return
                }
            }

            // Generate response
            self.storeResponse(self.generate(prompt: prompt))
        }
    }

    private func loadModel(path: String) -> Bool {
        guard FileManager.default.fileExists(atPath: path) else {
            print("InferenceBridge: File not found at \(path)")
            return false
        }

        do {
            llmInference = try LlmInference(options: LlmInference.Options(modelPath: path))
            currentModelPath = path
            print("InferenceBridge: Model loaded from \(path)")
            return true
        } catch {
            print("InferenceBridge: Load failed - \(error.localizedDescription)")
            return false
        }
    }

    private func generate(prompt: String) -> String {
        guard let inference = llmInference else {
            return "Error: Model not loaded"
        }

        do {
            return try inference.generateResponse(inputText: prompt)
        } catch {
            return "Error: \(error.localizedDescription)"
        }
    }

    private func storeResponse(_ response: String) {
        UserDefaults.standard.set(response, forKey: "gemma_response")
    }

    @objc public func close() {
        llmInference = nil
        currentModelPath = nil
    }

    deinit {
        NotificationCenter.default.removeObserver(self)
    }
}
