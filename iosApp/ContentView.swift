import SwiftUI
import shared

struct ContentView: View {
    @State private var activeTab = 0
    @State private var selectedMovieId: Int32? = nil
    
    @State private var mainViewModel = KoinHelperSwift.shared.getMainViewModel()
    @State private var mainState = MainState(isOnboardingCompleted: false, isAuthorized: false, isLoading: true)
    
    var body: some View {
        ZStack {
            if mainState.isLoading {
                ZStack {
                    AppColors.darkBackground.edgesIgnoringSafeArea(.all)
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: AppColors.primary))
                }
            } else if !mainState.isOnboardingCompleted {
                OnboardingScreen(onComplete: {
                    mainViewModel.sendIntent(intent: MainIntent.CompleteOnboarding())
                })
            } else if !mainState.isAuthorized {
                AuthScreen()
            } else {
                TabView(selection: $activeTab) {
                    HomeScreen(onNavigateToDetails: { id in
                        withAnimation(.spring()) {
                            selectedMovieId = id
                        }
                    })
                    .tabItem {
                        Label("Home", systemImage: "house.fill")
                    }
                    .tag(0)
                    
                    SearchScreen(onNavigateToDetails: { id in
                        withAnimation(.spring()) {
                            selectedMovieId = id
                        }
                    })
                    .tabItem {
                        Label("Search", systemImage: "magnifyingglass")
                    }
                    .tag(1)
                    
                    DiceScreen(onNavigateToDetails: { id in
                        withAnimation(.spring()) {
                            selectedMovieId = id
                        }
                    })
                    .tabItem {
                        Label("Dice", systemImage: "dice.fill")
                    }
                    .tag(2)
                    
                    WatchlistScreen(onNavigateToDetails: { id in
                        withAnimation(.spring()) {
                            selectedMovieId = id
                        }
                    })
                    .tabItem {
                        Label("Watchlist", systemImage: "list.bullet")
                    }
                    .tag(3)
                    
                    ProfileScreen()
                    .tabItem {
                        Label("Profile", systemImage: "person.fill")
                    }
                    .tag(4)
                }
                .accentColor(AppColors.primary)
                
                // Present Details screen as a fluid overlay sliding from bottom
                if let movieId = selectedMovieId {
                    DetailsScreen(movieId: movieId, onNavigateBack: {
                        withAnimation(.spring()) {
                            selectedMovieId = nil
                        }
                    })
                    .transition(.move(edge: .bottom))
                    .zIndex(10)
                }
            }
        }
        .task {
            for await currentState in mainViewModel.state {
                if let currentState = currentState {
                    self.mainState = currentState
                }
            }
        }
    }
}
