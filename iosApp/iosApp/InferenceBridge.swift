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

    private func handleGenerateRequest(_ notification: Notification) {
        // TODO: Workshop Step 3 - Handle generation request from Kotlin
    }

    private func loadModel(path: String) -> Bool {
        // TODO: Workshop Step 3 - Implement MediaPipe LLM loading
        return false
    }

    private func generate(prompt: String) -> String {
        // TODO: Workshop Step 3 - Implement MediaPipe LLM generation
        return ""
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
