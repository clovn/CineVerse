import Foundation
import FirebaseAnalytics
import shared

class IOSFirebaseAnalyticsTracker: NSObject, AnalyticsTracker {
    func trackEvent(name: String, params: [String : Any]) {
        Analytics.logEvent(name, parameters: params)
    }
}
