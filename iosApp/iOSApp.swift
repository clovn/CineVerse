import SwiftUI
import FirebaseCore
import shared

@main
struct iOSApp: App {
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
