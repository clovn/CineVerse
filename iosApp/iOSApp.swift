import SwiftUI
import FirebaseCore
import shared

@main
struct IOSApp: App {
    init() {
        FirebaseApp.configure()
        let tracker = IOSFirebaseAnalyticsTracker()
        KoinHelperSwift.shared.start(analyticsTracker: tracker)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
