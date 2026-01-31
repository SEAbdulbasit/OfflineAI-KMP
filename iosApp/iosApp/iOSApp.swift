import SwiftUI

@main
struct iOSApp: App {

    init() {
        // Initialize the inference bridge to start listening for notifications
        _ = InferenceBridge.shared
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
