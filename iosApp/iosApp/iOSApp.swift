import SwiftUI

@main
struct iOSApp: App {
    init() {
        print("iOSApp: Initializing")
        // Postpone everything. Even bridge creation.
        print("iOSApp: Initialization complete")
    }

    var body: some Scene {
        WindowGroup {
            ComposeView()
                .onAppear {
                    print("iOSApp: ComposeView appeared - starting bridge")
                    InferenceBridge.shared.start()
                }
        }
    }
}
