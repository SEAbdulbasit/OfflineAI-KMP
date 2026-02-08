import SwiftUI

@main
struct iOSApp: App {
    init() {
        _ = InferenceBridge.shared
    }

    var body: some Scene {
        WindowGroup {
            MainTabView()
        }
    }
}
