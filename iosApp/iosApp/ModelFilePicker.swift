import SwiftUI
import UniformTypeIdentifiers

/// A SwiftUI view that presents a document picker to select model files
/// and copies them to the app's Documents directory
struct ModelFilePickerView: View {
    @Binding var isPresented: Bool
    @Binding var selectedFilePath: String?
    @State private var showingPicker = false
    @State private var errorMessage: String?
    @State private var isImporting = false

    var body: some View {
        VStack(spacing: 20) {
            Text("Select Model File")
                .font(.headline)

            Text("Choose a Gemma model file (.bin or .task) from Files")
                .font(.subheadline)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)

            if isImporting {
                ProgressView("Importing model...")
            } else {
                Button(action: { showingPicker = true }) {
                    Label("Browse Files", systemImage: "folder")
                        .padding()
                        .frame(maxWidth: .infinity)
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                }
            }

            if let error = errorMessage {
                Text(error)
                    .foregroundColor(.red)
                    .font(.caption)
            }

            // Show existing models in app Documents
            let existingModels = listExistingModels()
            if !existingModels.isEmpty {
                Divider()

                Text("Models in App Storage")
                    .font(.subheadline)
                    .foregroundColor(.secondary)

                ForEach(existingModels, id: \.self) { model in
                    Button(action: {
                        selectExistingModel(model)
                    }) {
                        HStack {
                            Image(systemName: "doc.fill")
                            Text(model)
                            Spacer()
                            Image(systemName: "checkmark.circle")
                                .foregroundColor(.green)
                        }
                        .padding()
                        .background(Color.gray.opacity(0.1))
                        .cornerRadius(8)
                    }
                    .buttonStyle(PlainButtonStyle())
                }
            }
        }
        .padding()
        .fileImporter(
            isPresented: $showingPicker,
            allowedContentTypes: [.data, .item],
            allowsMultipleSelection: false
        ) { result in
            handleFileSelection(result)
        }
    }

    private func handleFileSelection(_ result: Result<[URL], Error>) {
        switch result {
        case .success(let urls):
            guard let sourceURL = urls.first else { return }

            let fileName = sourceURL.lastPathComponent

            // Validate file extension
            guard fileName.hasSuffix(".bin") || fileName.hasSuffix(".task") else {
                errorMessage = "Please select a .bin or .task model file"
                return
            }

            isImporting = true
            errorMessage = nil

            // Copy file to app's Documents directory
            DispatchQueue.global(qos: .userInitiated).async {
                let documentsPath = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
                let destinationURL = documentsPath.appendingPathComponent(fileName)

                // Start accessing security-scoped resource
                let accessing = sourceURL.startAccessingSecurityScopedResource()
                defer {
                    if accessing {
                        sourceURL.stopAccessingSecurityScopedResource()
                    }
                }

                do {
                    // Remove existing file if present
                    if FileManager.default.fileExists(atPath: destinationURL.path) {
                        try FileManager.default.removeItem(at: destinationURL)
                    }

                    // Copy file
                    try FileManager.default.copyItem(at: sourceURL, to: destinationURL)

                    DispatchQueue.main.async {
                        isImporting = false
                        selectedFilePath = destinationURL.path
                        isPresented = false
                    }
                } catch {
                    DispatchQueue.main.async {
                        isImporting = false
                        errorMessage = "Failed to import: \(error.localizedDescription)"
                    }
                }
            }

        case .failure(let error):
            errorMessage = "Failed to select file: \(error.localizedDescription)"
        }
    }

    private func listExistingModels() -> [String] {
        let documentsPath = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!

        do {
            let contents = try FileManager.default.contentsOfDirectory(atPath: documentsPath.path)
            return contents.filter { $0.hasSuffix(".bin") || $0.hasSuffix(".task") }
        } catch {
            return []
        }
    }

    private func selectExistingModel(_ fileName: String) {
        let documentsPath = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
        let filePath = documentsPath.appendingPathComponent(fileName).path
        selectedFilePath = filePath
        isPresented = false
    }
}

/// UIKit-based document picker for use with Kotlin/Compose
@objc public class ModelFilePicker: NSObject {

    private var completion: ((String?) -> Void)?
    private var documentPicker: UIDocumentPickerViewController?

    @objc public func pickModelFile(from viewController: UIViewController, completion: @escaping (String?) -> Void) {
        self.completion = completion

        let supportedTypes: [UTType] = [.data, .item]
        let picker = UIDocumentPickerViewController(forOpeningContentTypes: supportedTypes)
        picker.delegate = self
        picker.allowsMultipleSelection = false

        self.documentPicker = picker
        viewController.present(picker, animated: true)
    }

    @objc public static func getDocumentsDirectory() -> String {
        FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first?.path ?? ""
    }

    @objc public static func listModelFiles() -> [String] {
        let documentsPath = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!

        do {
            let contents = try FileManager.default.contentsOfDirectory(atPath: documentsPath.path)
            return contents.filter { $0.hasSuffix(".bin") || $0.hasSuffix(".task") }
        } catch {
            return []
        }
    }

    @objc public static func copyFileToDocuments(from sourceURL: URL, fileName: String) -> String? {
        let documentsPath = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
        let destinationURL = documentsPath.appendingPathComponent(fileName)

        let accessing = sourceURL.startAccessingSecurityScopedResource()
        defer {
            if accessing {
                sourceURL.stopAccessingSecurityScopedResource()
            }
        }

        do {
            if FileManager.default.fileExists(atPath: destinationURL.path) {
                try FileManager.default.removeItem(at: destinationURL)
            }
            try FileManager.default.copyItem(at: sourceURL, to: destinationURL)
            return destinationURL.path
        } catch {
            print("Failed to copy file: \(error)")
            return nil
        }
    }
}

extension ModelFilePicker: UIDocumentPickerDelegate {
    public func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
        guard let sourceURL = urls.first else {
            completion?(nil)
            return
        }

        let fileName = sourceURL.lastPathComponent

        guard fileName.hasSuffix(".bin") || fileName.hasSuffix(".task") else {
            completion?(nil)
            return
        }

        // Copy to Documents
        if let copiedPath = ModelFilePicker.copyFileToDocuments(from: sourceURL, fileName: fileName) {
            completion?(copiedPath)
        } else {
            completion?(nil)
        }
    }

    public func documentPickerWasCancelled(_ controller: UIDocumentPickerViewController) {
        completion?(nil)
    }
}
