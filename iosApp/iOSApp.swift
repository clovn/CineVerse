import SwiftUI

@main
struct iOSApp: App {
    init() {
        
        KoinHelperSwift.shared.start()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
