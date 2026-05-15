import SwiftUI
import ComposeApp

/// SwiftUI wrapper for the Compose Multiplatform UI
struct ComposeView: UIViewControllerRepresentable {

    func makeUIViewController(context: Context) -> UIViewController {
        // Create the Compose UI view controller
        // InferenceBridge will be initialized lazily when needed
        let viewController = MainViewControllerKt.MainViewController()
        return viewController
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // No updates needed - Compose handles its own state
    }
}