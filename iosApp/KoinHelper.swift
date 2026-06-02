import shared

class KoinHelperSwift {
    static let shared = KoinHelperSwift()
    
    private var koinHelper: KoinHelper?
    
    func start() {
        // doInitKoin() is the Kotlin/Native export name for initKoin()
        let _ = KoinKt.doInitKoin()
        koinHelper = KoinHelper()
    }
    
    func getHomeViewModel() -> HomeViewModel {
        return koinHelper!.getHomeViewModel()
    }
    
    func getSearchViewModel() -> SearchViewModel {
        return koinHelper!.getSearchViewModel()
    }
    
    func getDetailsViewModel() -> DetailsViewModel {
        return koinHelper!.getDetailsViewModel()
    }
    
    func getProfileViewModel() -> ProfileViewModel {
        return koinHelper!.getProfileViewModel()
    }
    
    func getWatchlistViewModel() -> WatchlistViewModel {
        return koinHelper!.getWatchlistViewModel()
    }
    
    func getDiceViewModel() -> DiceViewModel {
        return koinHelper!.getDiceViewModel()
    }
    
    func getMainViewModel() -> MainViewModel {
        return koinHelper!.getMainViewModel()
    }
}
