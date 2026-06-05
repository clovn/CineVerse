import SwiftUI
import shared

struct ProfileScreen: View {
    @State private var viewModel = KoinHelperSwift.shared.getProfileViewModel()
    @State private var state = ProfileState(user: nil, isAuthorized: false, stats: nil, usernameInput: "", passwordInput: "", isLoading: false, error: nil)
    
    @State private var alertMessage: String?
    @State private var showAlert: Bool = false
    
    var body: some View {
        NavigationView {
            ZStack {
                AppColors.darkBackground.edgesIgnoringSafeArea(.all)
                ProfileDetailsLayout()
            }
            .navigationTitle("My Profile")
        }
        .task {
            for await currentState in viewModel.state {
                if let currentState = currentState {
                    self.state = currentState
                }
            }
        }
    }
    
    @ViewBuilder
    private func ProfileDetailsLayout() -> some View {
        VStack(spacing: 24) {
            Spacer().frame(height: 10)

            ZStack {
                Circle()
                    .fill(AppColors.primary.opacity(0.1))
                    .frame(width: 100, height: 100)
                
                Image(systemName: "person.crop.circle.fill")
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(width: 80, height: 80)
                    .foregroundColor(AppColors.primary)
            }
            
            if let user = state.user {
                VStack(spacing: 4) {
                    Text(user.username)
                        .font(AppTypography.headingLarge)
                    Text("Premium CineVerse Member")
                        .font(AppTypography.labelSmall)
                        .foregroundColor(AppColors.primary)
                }
            }

            if let stats = state.stats {
                HStack(spacing: 16) {
                    
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Movies Watched")
                            .font(AppTypography.labelSmall)
                            .foregroundColor(.gray)
                        
                        Text("\(stats.moviesWatched)")
                            .font(.system(size: 32, weight: .bold))
                            .foregroundColor(AppColors.primary)
                    }
                    .padding()
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(AppColors.darkSurface)
                    .cornerRadius(16)

                    VStack(alignment: .leading, spacing: 8) {
                        Text("Favorite Genres")
                            .font(AppTypography.labelSmall)
                            .foregroundColor(.gray)
                        
                        Text(stats.favoriteGenres.joined(separator: "\n"))
                            .font(AppTypography.bodyMedium)
                            .fontWeight(.bold)
                            .foregroundColor(.primary)
                            .lineLimit(2)
                    }
                    .padding()
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(AppColors.darkSurface)
                    .cornerRadius(16)
                }
                .padding(.horizontal)
            }

            HStack {
                Text("Dark Theme")
                    .font(AppTypography.bodyMedium)
                    .fontWeight(.bold)
                    .foregroundColor(.primary)
                Spacer()
                Toggle("", isOn: Binding<Bool>(
                    get: { ThemeSettings.shared.isDarkTheme() },
                    set: { ThemeSettings.shared.setDarkTheme(enabled: $0) }
                ))
                .labelsHidden()
            }
            .padding()
            .background(AppColors.darkSurface)
            .cornerRadius(16)
            .padding(.horizontal)
            
            Spacer()
            
            CineVerseButton(text: "Log Out") {
                viewModel.sendIntent(intent: ProfileIntent.Logout())
            }
            .padding(.horizontal)
            .padding(.bottom, 24)
        }
    }
}
