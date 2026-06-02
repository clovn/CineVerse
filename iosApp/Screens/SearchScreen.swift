import SwiftUI
import shared

struct SearchScreen: View {
    let onNavigateToDetails: (Int32) -> Void
    
    @State private var viewModel = KoinHelperSwift.shared.getSearchViewModel()
    @State private var state = SearchState(query: "", results: [], isSearching: false, selectedGenre: nil, availableGenres: [])
    @State private var showFilters: Bool = false
    
    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Header inputs section
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
                        Button(action: { showFilters.toggle() }) {
                            HStack {
                                Image(systemName: "line.horizontal.3.decrease.circle")
                                Text("Filter by Genre")
                            }
                            .font(AppTypography.labelSmall)
                            .foregroundColor(state.selectedGenre != nil || showFilters ? AppColors.primary : .gray)
                        }
                        
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
                    
                    // Collapse Genre filter chips row
                    if showFilters {
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 8) {
                                Button(action: { viewModel.sendIntent(intent: SearchIntent.ApplyFilter(genre: nil)) }) {
                                    Text("All")
                                        .font(AppTypography.labelSmall)
                                        .padding(.horizontal, 12)
                                        .padding(.vertical, 6)
                                        .background(state.selectedGenre == nil ? AppColors.primary : Color.gray.opacity(0.1))
                                        .foregroundColor(state.selectedGenre == nil ? .white : .primary)
                                        .cornerRadius(8)
                                }
                                
                                ForEach(state.availableGenres, id: \.id) { genre in
                                    Button(action: { viewModel.sendIntent(intent: SearchIntent.ApplyFilter(genre: genre)) }) {
                                        Text(genre.name)
                                            .font(AppTypography.labelSmall)
                                            .padding(.horizontal, 12)
                                            .padding(.vertical, 6)
                                            .background(state.selectedGenre?.id == genre.id ? AppColors.primary : Color.gray.opacity(0.1))
                                            .foregroundColor(state.selectedGenre?.id == genre.id ? .white : .primary)
                                            .cornerRadius(8)
                                    }
                                }
                            }
                        }
                        .transition(.slide)
                    }
                }
                .padding()
                .background(Color(uiColor: .secondarySystemBackground))
                
                // Result scroller
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
                                Button(action: { onNavigateToDetails(movie.id) }) {
                                    HStack(spacing: 12) {
                                        // Poster left
                                        ZStack {
                                            if let path = movie.posterPath, let url = URL(string: path) {
                                                AsyncImage(url: url) { image in
                                                    image.resizable().aspectRatio(contentMode: .fill)
                                                } placeholder: {
                                                    ProgressView()
                                                }
                                            }
                                        }
                                        .frame(width: 60, height: 90)
                                        .cornerRadius(8)
                                        .clipped()
                                        
                                        // Details right
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
                                    .background(Color(uiColor: .systemBackground))
                                    .cornerRadius(12)
                                    .shadow(color: Color.black.opacity(0.05), radius: 5)
                                }
                            }
                        }
                        .padding()
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
