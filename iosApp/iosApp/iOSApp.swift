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
                .ignoresSafeArea(.all)
                .onAppear {
                    InferenceBridge.shared.start()
                }
        }
    }
}
