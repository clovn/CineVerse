import SwiftUI
import shared

struct HomeScreen: View {
    let onNavigateToDetails: (Int32) -> Void

    @State private var viewModel = KoinHelperSwift.shared.getHomeViewModel()
    @State private var state = HomeState(
        movies: [],
        nowPlaying: [],
        isLoading: false,
        error: nil,
        currentTab: .trending
    )

    var body: some View {
        NavigationView {
            ZStack {
                AppColors.background.edgesIgnoringSafeArea(.all)

                if state.isLoading && state.movies.isEmpty {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: AppColors.primary))
                } else {
                    ScrollView {
                        VStack(alignment: .leading, spacing: 16) {

                            if !state.nowPlaying.isEmpty {
                                TabView {
                                    ForEach(state.nowPlaying, id: \.id) { movie in
                                        Button(action: { onNavigateToDetails(movie.id) }, label: {
                                            ZStack(alignment: .bottomLeading) {
                                                if let path = movie.posterPath, let url = URL(string: path) {
                                                    AsyncImage(url: url) { phase in
                                                        switch phase {
                                                        case .success(let image):
                                                            image
                                                                .resizable()
                                                                .aspectRatio(contentMode: .fill)
                                                                .frame(height: 220)
                                                                .clipped()
                                                        case .failure:
                                                            ImageFallbackView(systemIconName: "popcorn.fill")
                                                                .frame(height: 220)
                                                        case .empty:
                                                            ProgressView()
                                                                .progressViewStyle(
                                                                    CircularProgressViewStyle(tint: AppColors.primary)
                                                                )
                                                                .frame(height: 220)
                                                        @unknown default:
                                                            ImageFallbackView(systemIconName: "popcorn.fill")
                                                                .frame(height: 220)
                                                        }
                                                    }
                                                    .frame(height: 220)
                                                    .clipped()
                                                } else {
                                                    ImageFallbackView(systemIconName: "popcorn.fill")
                                                        .frame(height: 220)
                                                }

                                                LinearGradient(
                                                    gradient: Gradient(colors: [.clear, .black.opacity(0.85)]),
                                                    startPoint: .top,
                                                    endPoint: .bottom
                                                )

                                                VStack(alignment: .leading, spacing: 4) {
                                                    Text(movie.title)
                                                        .font(.title3)
                                                        .fontWeight(.bold)
                                                        .foregroundColor(.white)

                                                    HStack {
                                                        Text("★ \(String(format: "%.1f", movie.voteAverage))")
                                                            .font(AppTypography.labelSmall)
                                                            .foregroundColor(AppColors.primary)
                                                        Text("•")
                                                            .foregroundColor(.gray)
                                                        Text(movie.releaseDate)
                                                            .font(AppTypography.labelSmall)
                                                            .foregroundColor(.white)
                                                    }
                                                }
                                                .padding()
                                            }
                                        })
                                    }
                                }
                                .tabViewStyle(PageTabViewStyle())
                                .frame(height: 250)
                                .cornerRadius(16)
                                .padding(.horizontal)
                            }

                            Picker("", selection: Binding(
                                get: { state.currentTab },
                                set: { viewModel.sendIntent(intent: HomeIntent.ChangeTab(tab: $0)) }
                            )) {
                                Text("Trending").tag(HomeTab.trending)
                                Text("Now Playing").tag(HomeTab.nowPlaying)
                            }
                            .pickerStyle(SegmentedPickerStyle())
                            .padding(.horizontal)

                            Text(state.currentTab == .trending ? "Trending This Week" : "Now Playing in Cinemas")
                                .font(AppTypography.headingLarge)
                                .padding(.horizontal)

                            let listToDisplay = state.currentTab == .trending ? state.movies : state.nowPlaying
                            let columns = [
                                GridItem(.flexible(), spacing: 16),
                                GridItem(.flexible(), spacing: 16)
                            ]
                            LazyVGrid(columns: columns, spacing: 16) {
                                ForEach(listToDisplay, id: \.id) { movie in
                                    VStack(alignment: .leading, spacing: 6) {
                                        MovieCard(imageUrl: movie.posterPath) {
                                            onNavigateToDetails(movie.id)
                                        }

                                        Text(movie.title)
                                            .font(AppTypography.bodyMedium)
                                            .fontWeight(.bold)
                                            .lineLimit(1)

                                        Text("★ \(String(format: "%.1f", movie.voteAverage))")
                                            .font(AppTypography.labelSmall)
                                            .foregroundColor(AppColors.primary)
                                    }
                                }
                            }
                            .padding(.horizontal)
                        }
                        .padding(.bottom, 24)
                    }
                }
            }
            .navigationTitle("CineVerse")
        }
        .task {

            for await currentState in viewModel.state {
                if let currentState = currentState { self.state = currentState }
            }
        }
    }
}
