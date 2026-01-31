import Foundation
import MediaPipeTasksGenAI
import MediaPipeTasksGenAIC

/// Swift wrapper for MediaPipe LLM Inference
///
/// Usage:
/// 1. Run 'pod install' in iosApp directory
/// 2. Open iosApp.xcworkspace (NOT xcodeproj)
///
@objc public class GemmaInferenceHelper: NSObject {

    private var llmInference: LlmInference?
    private var isModelLoaded: Bool = false
    private var currentLoadingProgress: Float = 0.0

    @objc public override init() {
        super.init()
    }

    /// Load the model from the specified path
    @objc public func loadModel(
        modelPath: String,
        maxTokens: Int32,
        topK: Int32,
        temperature: Float,
        completion: @escaping (Bool, String?) -> Void
    ) {
        currentLoadingProgress = 0.1

        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
            guard let self = self else { return }

            // Find the model file
            let resolvedPath = self.resolveModelPath(modelPath)

            guard FileManager.default.fileExists(atPath: resolvedPath) else {
                DispatchQueue.main.async {
                    completion(false, "Model file not found at: \(resolvedPath)")
                }
                return
            }

            self.currentLoadingProgress = 0.3

            do {
                // Create LLM Inference options - only use modelPath
                // Other parameters may not be available in all versions
                let options = LlmInference.Options(modelPath: resolvedPath)

                self.currentLoadingProgress = 0.5

                // Initialize the inference engine
                self.llmInference = try LlmInference(options: options)

                self.currentLoadingProgress = 1.0
                self.isModelLoaded = true

                DispatchQueue.main.async {
                    completion(true, nil)
                }
            } catch {
                self.isModelLoaded = false
                self.currentLoadingProgress = 0.0
                DispatchQueue.main.async {
                    completion(false, "Failed to load model: \(error.localizedDescription)")
                }
            }
        }
    }

    /// Generate response synchronously
    @objc public func generateResponseSync(prompt: String) -> String {
        guard isModelLoaded else {
            return "Error: Model not loaded"
        }

        guard let inference = llmInference else {
            return "Error: Inference engine not initialized"
        }

        do {
            return try inference.generateResponse(inputText: prompt)
        } catch {
            return "Error: \(error.localizedDescription)"
        }
    }

    /// Generate response with streaming callback
    @objc public func generateResponseStreaming(
        prompt: String,
        onToken: @escaping (String) -> Void,
        onComplete: @escaping (String?, String?) -> Void
    ) {
        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
            let response = self?.generateResponseSync(prompt: prompt) ?? "Error"

            // Simulate streaming by sending chunks
            let words = response.split(separator: " ")
            for (index, word) in words.enumerated() {
                let token = String(word) + (index < words.count - 1 ? " " : "")
                DispatchQueue.main.async {
                    onToken(token)
                }
                Thread.sleep(forTimeInterval: 0.05)
            }

            DispatchQueue.main.async {
                onComplete(response, nil)
            }
        }
    }

    /// Check if the model is loaded
    @objc public func isLoaded() -> Bool {
        return isModelLoaded
    }

    /// Get current loading progress (0.0 to 1.0)
    @objc public func getLoadingProgress() -> Float {
        return currentLoadingProgress
    }

    /// Close and release resources
    @objc public func close() {
        llmInference = nil
        isModelLoaded = false
        currentLoadingProgress = 0.0
    }

    // MARK: - Private Helpers

    private func resolveModelPath(_ modelPath: String) -> String {
        let fileManager = FileManager.default

        // Documents directory
        if let documentsPath = fileManager.urls(for: .documentDirectory, in: .userDomainMask).first {
            let docPath = documentsPath.appendingPathComponent(modelPath).path
            if fileManager.fileExists(atPath: docPath) {
                return docPath
            }

            let modelsPath = documentsPath.appendingPathComponent("models/\(modelPath)").path
            if fileManager.fileExists(atPath: modelsPath) {
                return modelsPath
            }
        }

        // App bundle
        let baseName = modelPath
            .replacingOccurrences(of: ".bin", with: "")
            .replacingOccurrences(of: ".task", with: "")
        let ext = modelPath.hasSuffix(".bin") ? "bin" : "task"

        if let bundlePath = Bundle.main.path(forResource: baseName, ofType: ext) {
            return bundlePath
        }

        // Caches directory
        if let cachesPath = fileManager.urls(for: .cachesDirectory, in: .userDomainMask).first {
            let cachePath = cachesPath.appendingPathComponent(modelPath).path
            if fileManager.fileExists(atPath: cachePath) {
                return cachePath
            }
        }

        // Direct path
        return modelPath
    }
}
