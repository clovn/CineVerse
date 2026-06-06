import SwiftUI
import shared

struct WatchlistScreen: View {
    let onNavigateToDetails: (Int32) -> Void

    @State private var viewModel = KoinHelperSwift.shared.getWatchlistViewModel()
    @State private var state = WatchlistState(favorites: [], watchLater: [], selectedTab: .favorites, isLoading: false)

    @State private var toastMessage: String?
    @State private var showToast = false

    var body: some View {
        NavigationView {
            VStack(spacing: 0) {

                Picker("", selection: Binding(
                    get: { state.selectedTab },
                    set: { viewModel.sendIntent(intent: WatchlistIntent.ChangeTab(tab: $0)) }
                )) {
                    Text("Favorites").tag(WatchlistTab.favorites)
                    Text("Watch Later").tag(WatchlistTab.watchLater)
                }
                .pickerStyle(SegmentedPickerStyle())
                .padding()

                if state.isLoading {
                    Spacer()
                    ProgressView()
                    Spacer()
                } else {
                    let listToDisplay = state.selectedTab == .favorites ? state.favorites : state.watchLater

                    if listToDisplay.isEmpty {
                        Spacer()
                        let emptyMsg = state.selectedTab == .favorites
                            ? "No favorite movies added."
                            : "Watch Later list is empty."
                        Text(emptyMsg)
                            .foregroundColor(.gray)
                            .font(AppTypography.bodyMedium)
                        Spacer()
                    } else {
                        List {
                            ForEach(listToDisplay, id: \.id) { movie in
                                Button(action: { onNavigateToDetails(movie.id) }, label: {
                                    HStack(spacing: 12) {
                                         ZStack {
                                             if let path = movie.posterPath, let url = URL(string: path) {
                                                 AsyncImage(url: url) { phase in
                                                     switch phase {
                                                     case .success(let image):
                                                         image
                                                             .resizable()
                                                             .aspectRatio(contentMode: .fill)
                                                             .frame(width: 50, height: 75)
                                                             .clipped()
                                                     case .failure:
                                                         ImageFallbackView(systemIconName: "film")
                                                             .frame(width: 50, height: 75)
                                                     case .empty:
                                                         ProgressView()
                                                             .progressViewStyle(
                                                                 CircularProgressViewStyle(tint: AppColors.primary)
                                                             )
                                                             .frame(width: 50, height: 75)
                                                     @unknown default:
                                                         ImageFallbackView(systemIconName: "film")
                                                             .frame(width: 50, height: 75)
                                                     }
                                                 }
                                                 .frame(width: 50, height: 75)
                                                 .clipped()
                                             } else {
                                                 ImageFallbackView(systemIconName: "film")
                                                     .frame(width: 50, height: 75)
                                             }
                                         }
                                        .frame(width: 50, height: 75)
                                        .cornerRadius(8)
                                        .clipped()

                                        VStack(alignment: .leading, spacing: 4) {
                                            Text(movie.title)
                                                .font(AppTypography.bodyMedium)
                                                .fontWeight(.bold)
                                                .foregroundColor(.primary)
                                                .lineLimit(1)

                                            Text("Year: \(String(movie.releaseDate.prefix(4)))")
                                                .font(AppTypography.labelSmall)
                                                .foregroundColor(.gray)

                                            Text("★ \(String(format: "%.1f", movie.voteAverage))")
                                                .font(AppTypography.labelSmall)
                                                .foregroundColor(AppColors.primary)
                                                .fontWeight(.bold)
                                        }
                                    }
                                })
                            }
                            .onDelete { indexSet in
                                for index in indexSet {
                                    let movie = listToDisplay[index]
                                    viewModel.sendIntent(
                                        intent: WatchlistIntent.RemoveMovie(
                                            movieId: movie.id,
                                            tab: state.selectedTab
                                        )
                                    )
                                }
                            }
                        }
                        .listStyle(PlainListStyle())
                    }
                }
            }
            .navigationTitle("My Watchlist")
            .alert(isPresented: $showToast) {
                Alert(
                    title: Text("Info"),
                    message: Text(toastMessage ?? ""),
                    dismissButton: .default(Text("OK"))
                )
            }
        }
        .task {
            for await currentState in viewModel.state {
                if let currentState = currentState { self.state = currentState }
            }
        }
        .task {
            for await currentEffect in viewModel.effect {
                switch currentEffect {
                case let toastEffect as WatchlistEffect.ShowMessage:
                    self.toastMessage = toastEffect.message
                    self.showToast = true
                default:
                    break
                }
            }
        }
    }
}
