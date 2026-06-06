import SwiftUI
import shared

struct SearchScreen: View {
    let onNavigateToDetails: (Int32) -> Void

    @State private var viewModel = KoinHelperSwift.shared.getSearchViewModel()
    @State private var state = SearchState(
        query: "",
        results: [],
        isSearching: false,
        selectedGenre: nil,
        availableGenres: []
    )
    @State private var showFilters: Bool = false

    var body: some View {
        NavigationView {
            ZStack {
                AppColors.background.edgesIgnoringSafeArea(.all)

                VStack(spacing: 0) {

                    VStack(alignment: .leading, spacing: 10) {
                        CineVerseTextField(
                            placeholder: "Search movies by title...",
                            text: Binding(
                                get: { state.query },
                                set: { viewModel.sendIntent(intent: SearchIntent.UpdateQuery(query: $0)) }
                            ),
                            leadingIcon: "magnifyingglass"
                        )

                        HStack {
                            Button(action: { showFilters.toggle() }, label: {
                                HStack {
                                    Image(systemName: "line.horizontal.3.decrease.circle")
                                    Text("Filter by Genre")
                                }
                                .font(AppTypography.labelSmall)
                                .foregroundColor(state.selectedGenre != nil || showFilters ? AppColors.primary : .gray)
                            })

                            Spacer()

                            if let selected = state.selectedGenre {
                                Text("Filtered by: \(selected.name)")
                                    .font(AppTypography.labelSmall)
                                    .foregroundColor(AppColors.primary)
                                    .onTapGesture {
                                        viewModel.sendIntent(intent: SearchIntent.ApplyFilter(genre: nil))
                                    }
                            }
                        }
                        .padding(.top, 4)

                        if showFilters {
                            ScrollView(.horizontal, showsIndicators: false) {
                                HStack(spacing: 8) {
                                    Button(
                                        action: {
                                            viewModel.sendIntent(intent: SearchIntent.ApplyFilter(genre: nil))
                                        },
                                        label: {
                                            Text("All")
                                                .font(AppTypography.labelSmall)
                                                .padding(.horizontal, 12)
                                                .padding(.vertical, 6)
                                                .background(
                                                    state.selectedGenre == nil
                                                        ? AppColors.primary
                                                        : Color.gray.opacity(0.1)
                                                )
                                                .foregroundColor(state.selectedGenre == nil ? .white : .primary)
                                                .cornerRadius(8)
                                        }
                                    )

                                    ForEach(state.availableGenres, id: \.id) { genre in
                                        Button(
                                            action: {
                                                viewModel.sendIntent(intent: SearchIntent.ApplyFilter(genre: genre))
                                            },
                                            label: {
                                                Text(genre.name)
                                                    .font(AppTypography.labelSmall)
                                                    .padding(.horizontal, 12)
                                                    .padding(.vertical, 6)
                                                    .background(
                                                        state.selectedGenre?.id == genre.id
                                                            ? AppColors.primary
                                                            : Color.gray.opacity(0.1)
                                                    )
                                                    .foregroundColor(
                                                        state.selectedGenre?.id == genre.id ? .white : .primary
                                                    )
                                                    .cornerRadius(8)
                                            }
                                        )
                                    }
                                }
                            }
                            .transition(.slide)
                        }
                    }
                    .padding()
                    .background(AppColors.surface)

                    if state.isSearching {
                        Spacer()
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: AppColors.primary))
                        Spacer()
                    } else if state.results.isEmpty {
                        Spacer()
                        Text(state.query.isEmpty ? "Search for your favorite movies" : "No results found")
                            .foregroundColor(.gray)
                            .font(AppTypography.bodyMedium)
                        Spacer()
                    } else {
                        ScrollView {
                            LazyVStack(spacing: 12) {
                                ForEach(state.results, id: \.id) { movie in
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
                                                                .frame(width: 60, height: 90)
                                                                .clipped()
                                                        case .failure:
                                                            ImageFallbackView(systemIconName: "film")
                                                                .frame(width: 60, height: 90)
                                                        case .empty:
                                                            ProgressView()
                                                                .progressViewStyle(
                                                                    CircularProgressViewStyle(tint: AppColors.primary)
                                                                )
                                                                .frame(width: 60, height: 90)
                                                        @unknown default:
                                                            ImageFallbackView(systemIconName: "film")
                                                                .frame(width: 60, height: 90)
                                                        }
                                                    }
                                                    .frame(width: 60, height: 90)
                                                    .clipped()
                                                } else {
                                                    ImageFallbackView(systemIconName: "film")
                                                        .frame(width: 60, height: 90)
                                                }
                                            }
                                            .frame(width: 60, height: 90)
                                            .cornerRadius(8)
                                            .clipped()

                                            VStack(alignment: .leading, spacing: 6) {
                                                Text(movie.title)
                                                    .font(AppTypography.bodyMedium)
                                                    .fontWeight(.bold)
                                                    .foregroundColor(.primary)
                                                    .multilineTextAlignment(.leading)
                                                    .lineLimit(2)

                                                Text("Year: \(String(movie.releaseDate.prefix(4)))")
                                                    .font(AppTypography.labelSmall)
                                                    .foregroundColor(.gray)

                                                Text("★ \(String(format: "%.1f", movie.voteAverage))")
                                                    .font(AppTypography.labelSmall)
                                                    .foregroundColor(AppColors.primary)
                                                    .fontWeight(.bold)
                                            }
                                            Spacer()
                                        }
                                        .padding(8)
                                        .background(AppColors.surface)
                                        .cornerRadius(12)
                                        .shadow(color: Color.black.opacity(0.2), radius: 5)
                                    })
                                }
                            }
                            .padding()
                        }
                    }
                }
            }
            .navigationTitle("Smart Search")
        }
        .task {
            for await currentState in viewModel.state {
                if let currentState = currentState { self.state = currentState }
            }
        }
    }
}
