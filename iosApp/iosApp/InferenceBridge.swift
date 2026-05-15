import Foundation
import UIKit
import MediaPipeTasksGenAI

/// iOS Bridge for LLM Inference
public class InferenceBridge: NSObject {

    public static let shared = InferenceBridge()

    private var llmInference: LlmInference?
    private var currentModelPath: String?

    private override init() {
        super.init()
        print("InferenceBridge: Initializing")
        print("InferenceBridge: Init basic completed")
    }

    public func start() {
        print("InferenceBridge: start() called")
        setupObserver()
        print("InferenceBridge: start() completed")
    }

    private func setupObserver() {
        print("InferenceBridge: Adding observer for GemmaGenerateRequest")
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleGenerateRequest(_:)),
            name: NSNotification.Name("GemmaGenerateRequest"),
            object: nil
        )
    }

    @objc private func handleGenerateRequest(_ notification: Notification) {
        print("InferenceBridge: handleGenerateRequest called")
        guard let userInfo = notification.userInfo else {
            print("InferenceBridge: Error - userInfo is nil")
            storeResponse("Error: Invalid request (no userInfo)")
            return
        }
        
        print("InferenceBridge: userInfo keys: \(userInfo.keys)")

        guard let prompt = userInfo["prompt"] as? String,
              let modelPath = userInfo["modelPath"] as? String
        else {
            print("InferenceBridge: Error - Invalid notification userInfo types or missing keys")
            storeResponse("Error: Invalid request (missing keys)")
            return
        }

        print("InferenceBridge: Request received for prompt: \(prompt.prefix(20))...")
        
        // 1. (Re)Initialize Inference if model path changed
        if llmInference == nil || modelPath != currentModelPath {
            print("InferenceBridge: Initializing LlmInference with path: \(modelPath)")
            do {
                llmInference = try LlmInference(modelPath: modelPath)
                currentModelPath = modelPath
                print("InferenceBridge: LlmInference initialized successfully")
            } catch {
                print("InferenceBridge: Error initializing LlmInference: \(error.localizedDescription)")
                storeResponse("Error: Failed to initialize model at \(modelPath)")
                return
            }
        }

        // 2. Generate Response
        print("InferenceBridge: Starting generation...")
        do {
            // Using synchronous version for simplicity as it's already in a background-friendly way from Kotlin side
            // and we want to ensure we return a full response to the polling Kotlin side.
            if let response = try llmInference?.generateResponse(inputText: prompt) {
                print("InferenceBridge: Generation completed, length: \(response.count)")
                storeResponse(response)
            } else {
                print("InferenceBridge: Error - Generation returned nil")
                storeResponse("Error: Empty response from model")
            }
        } catch {
            print("InferenceBridge: Generation error: \(error.localizedDescription)")
            storeResponse("Error: \(error.localizedDescription)")
        }
    }

    private func storeResponse(_ response: String) {
        print("InferenceBridge: Storing response in UserDefaults")
        UserDefaults.standard.set(response, forKey: "gemma_response")
        UserDefaults.standard.synchronize()
    }

    public func close() {
        llmInference = nil
        currentModelPath = nil
    }

    deinit {
        NotificationCenter.default.removeObserver(self)
    }
}
