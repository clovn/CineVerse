import SwiftUI
import shared

struct DiceScreen: View {
    let onNavigateToDetails: (Int32) -> Void
    
    @State private var viewModel = KoinHelperSwift.shared.getDiceViewModel()
    @State private var state = DiceState(randomMovie: nil, isRolling: false)
    
    @State private var rotation: Double = 0.0
    
    var body: some View {
        NavigationView {
            ZStack {
                AppColors.background.edgesIgnoringSafeArea(.all)
                
                VStack {
                VStack(spacing: 8) {
                    Text("Movie Dice")
                        .font(AppTypography.headingLarge)
                        .padding(.top, 16)
                    
                    Text("Can't decide what to watch? Let the dice choose!")
                        .font(AppTypography.bodyMedium)
                        .foregroundColor(.gray)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 32)
                }
                
                Spacer()

                ZStack {
                    RoundedRectangle(cornerRadius: 24)
                        .fill(AppColors.primary)
                        .frame(width: 120, height: 120)
                        .overlay(
                            DiceDotsView()
                        )
                }
                .rotation3DEffect(
                    .degrees(rotation),
                    axis: (x: 1.0, y: 1.0, z: 0.5)
                )
                .onTapGesture {
                    if !state.isRolling {
                        viewModel.sendIntent(intent: DiceIntent.RollDice())
                    }
                }
                
                Spacer()

                BoxResultsView()
                
                Spacer()
                
                CineVerseButton(
                    text: state.isRolling ? "Rolling..." : "Roll the Dice",
                    onClick: { viewModel.sendIntent(intent: DiceIntent.RollDice()) },
                    enabled: !state.isRolling
                )
                .padding(.horizontal)
                .padding(.bottom, 24)
                }
            }
            .navigationTitle("Random Picker")
        }
        .task {
            for await currentState in viewModel.state {
                if let currentState = currentState {
                    self.state = currentState
                }
            }
        }
        .onChange(of: state.isRolling) { isRolling in
            if isRolling {
                withAnimation(Animation.linear(duration: 0.6).repeatForever(autoreverses: false)) {
                    rotation = 720.0
                }
            } else {
                withAnimation(.spring()) {
                    rotation = 0.0
                }
            }
        }
    }
    
    @ViewBuilder
    private func DiceDotsView() -> some View {
        GeometryReader { geo in
            let r = geo.size.width * 0.08
            let w = geo.size.width
            let h = geo.size.height
            
            ZStack {
                
                Circle().fill(Color.white).frame(width: r * 2).position(x: w / 2, y: h / 2)
                
                Circle().fill(Color.white).frame(width: r * 2).position(x: w / 4, y: h / 4)
                
                Circle().fill(Color.white).frame(width: r * 2).position(x: w * 3 / 4, y: h * 3 / 4)
                
                Circle().fill(Color.white).frame(width: r * 2).position(x: w * 3 / 4, y: h / 4)
                
                Circle().fill(Color.white).frame(width: r * 2).position(x: w / 4, y: h * 3 / 4)
            }
        }
    }
    
    @ViewBuilder
    private func BoxResultsView() -> some View {
        VStack {
            if state.isRolling {
                Text("Selecting a masterpiece...")
                    .font(AppTypography.bodyMedium)
                    .fontWeight(.bold)
                    .foregroundColor(AppColors.primary)
            } else if let movie = state.randomMovie {
                VStack(spacing: 12) {
                    Text("Your Match!")
                        .font(AppTypography.labelSmall)
                        .fontWeight(.bold)
                        .foregroundColor(AppColors.primary)

                    MovieCard(imageUrl: movie.posterPath) {
                        onNavigateToDetails(movie.id)
                    }
                    .frame(width: 100, height: 150)
                    
                    Text(movie.title)
                        .font(AppTypography.bodyMedium)
                        .fontWeight(.bold)
                        .lineLimit(1)
                    
                    Text("Rating: ★ \(String(format: "%.1f", movie.voteAverage))")
                        .font(AppTypography.labelSmall)
                        .foregroundColor(.gray)
                }
                .padding()
                .background(AppColors.surface)
                .cornerRadius(16)
                .shadow(color: Color.black.opacity(0.3), radius: 5)
                .transition(.asymmetric(insertion: .scale.combined(with: .opacity), removal: .opacity))
            } else {
                Spacer().frame(height: 200)
            }
        }
        .frame(height: 240)
    }
}
