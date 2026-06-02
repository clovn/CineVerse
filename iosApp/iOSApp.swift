import SwiftUI

@main
struct iOSApp: App {
    init() {
        // Bootstrap Koin container before loading views
        KoinHelperSwift.shared.start()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
