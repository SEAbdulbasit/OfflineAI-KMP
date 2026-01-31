import Foundation
import MediaPipeTasksGenAI
import MediaPipeTasksGenAIC

/// Singleton bridge for MediaPipe LLM Inference
/// Listens for notifications from Kotlin and performs inference
@objc public class InferenceBridge: NSObject {

    @objc public static let shared = InferenceBridge()

    private var llmInference: LlmInference?
    private var isModelLoaded: Bool = false
    private var currentModelPath: String?

    private override init() {
        super.init()
        setupNotificationObserver()
    }

    private func setupNotificationObserver() {
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
              let modelPath = userInfo["modelPath"] as? String else {
            UserDefaults.standard.set("Error: Invalid notification data", forKey: "gemma_response")
            return
        }

        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
            guard let self = self else { return }

            // Load model if needed
            if !self.isModelLoaded || self.currentModelPath != modelPath {
                let success = self.loadModelSync(path: modelPath)
                if !success {
                    UserDefaults.standard.set("Error: Failed to load model", forKey: "gemma_response")
                    return
                }
            }

            // Generate response
            let response = self.generateSync(prompt: prompt)
            UserDefaults.standard.set(response, forKey: "gemma_response")
        }
    }

    /// Load model synchronously
    private func loadModelSync(path: String) -> Bool {
        guard FileManager.default.fileExists(atPath: path) else {
            print("InferenceBridge: Model file not found at \(path)")
            return false
        }

        do {
            let options = LlmInference.Options(modelPath: path)
            self.llmInference = try LlmInference(options: options)
            self.isModelLoaded = true
            self.currentModelPath = path
            print("InferenceBridge: Model loaded successfully from \(path)")
            return true
        } catch {
            print("InferenceBridge: Failed to load model: \(error.localizedDescription)")
            self.isModelLoaded = false
            return false
        }
    }

    /// Generate response synchronously
    private func generateSync(prompt: String) -> String {
        guard isModelLoaded, let inference = llmInference else {
            return "Error: Model not loaded. Please load a model first."
        }

        do {
            let response = try inference.generateResponse(inputText: prompt)
            return response
        } catch {
            return "Error generating response: \(error.localizedDescription)"
        }
    }

    /// Public method to manually load model
    @objc public func loadModel(path: String) -> Bool {
        return loadModelSync(path: path)
    }

    /// Public method to generate
    @objc public func generate(prompt: String) -> String {
        return generateSync(prompt: prompt)
    }

    /// Check if model is loaded
    @objc public func isLoaded() -> Bool {
        return isModelLoaded
    }

    /// Close and release resources
    @objc public func close() {
        llmInference = nil
        isModelLoaded = false
        currentModelPath = nil
    }

    deinit {
        NotificationCenter.default.removeObserver(self)
    }
}
