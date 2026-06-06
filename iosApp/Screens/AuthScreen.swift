import SwiftUI
import shared

struct AuthScreen: View {
    @State private var viewModel = KoinHelperSwift.shared.getProfileViewModel()
    @State private var state = ProfileState(user: nil, isAuthorized: false, stats: nil, usernameInput: "", passwordInput: "", isLoading: false, error: nil)
    @State private var isLoginTab = true
    
    @State private var alertMessage: String?
    @State private var showAlert: Bool = false
    
    var body: some View {
        NavigationView {
            ZStack {
                AppColors.background.edgesIgnoringSafeArea(.all)
                
                VStack(spacing: 20) {
                    Spacer().frame(height: 20)

                    ZStack {
                        Circle()
                            .fill(AppColors.primary.opacity(0.1))
                            .frame(width: 80, height: 80)
                        
                        Image(systemName: "film")
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                            .frame(width: 44, height: 44)
                            .foregroundColor(AppColors.primary)
                    }
                    
                    Text("CineVerse")
                        .font(AppTypography.headingLarge)
                        .foregroundColor(AppColors.textPrimary)
                        .fontWeight(.bold)
                    
                    Text("Your Ultimate Movie Companion")
                        .font(AppTypography.bodyMedium)
                        .foregroundColor(AppColors.textSecondary)
                        .padding(.top, -10)
                        .padding(.bottom, 20)

                    HStack(spacing: 4) {
                        Button(action: { isLoginTab = true }) {
                            Text("Log In")
                                .font(.system(size: 16, weight: .bold))
                                .foregroundColor(isLoginTab ? .white : AppColors.textSecondary)
                                .frame(maxWidth: .infinity)
                                .frame(height: 44)
                                .background(isLoginTab ? AppColors.primary : Color.clear)
                                .cornerRadius(8)
                        }
                        
                        Button(action: { isLoginTab = false }) {
                            Text("Register")
                                .font(.system(size: 16, weight: .bold))
                                .foregroundColor(!isLoginTab ? .white : AppColors.textSecondary)
                                .frame(maxWidth: .infinity)
                                .frame(height: 44)
                                .background(!isLoginTab ? AppColors.primary : Color.clear)
                                .cornerRadius(8)
                        }
                    }
                    .padding(4)
                    .background(AppColors.surface)
                    .cornerRadius(12)
                    .padding(.horizontal)
                    
                    Spacer().frame(height: 10)
                    
                    CineVerseTextField(
                        placeholder: "Username",
                        text: Binding(
                            get: { state.usernameInput },
                            set: { viewModel.sendIntent(intent: ProfileIntent.TypeUsername(username: $0)) }
                        ),
                        leadingIcon: "person.fill"
                    )
                    .padding(.horizontal)
                    
                    CineVerseTextField(
                        placeholder: isLoginTab ? "Password" : "Password (min 4 characters)",
                        text: Binding(
                            get: { state.passwordInput },
                            set: { viewModel.sendIntent(intent: ProfileIntent.TypePassword(password: $0)) }
                        ),
                        isSecure: true,
                        leadingIcon: "lock.fill"
                    )
                    .padding(.horizontal)
                    
                    Spacer().frame(height: 10)
                    
                    CineVerseButton(
                        text: isLoginTab ? "Log In" : "Create Account",
                        onClick: {
                            if isLoginTab {
                                viewModel.sendIntent(intent: ProfileIntent.Login())
                            } else {
                                viewModel.sendIntent(intent: ProfileIntent.Register())
                            }
                        },
                        isLoading: state.isLoading
                    )
                    .padding(.horizontal)
                    
                    Spacer()
                }
            }
            .navigationBarHidden(true)
            .alert(isPresented: $showAlert) {
                Alert(title: Text("Auth Status"), message: Text(alertMessage ?? ""), dismissButton: .default(Text("OK")))
            }
        }
        .task {
            for await currentState in viewModel.state {
                if let currentState = currentState {
                    self.state = currentState
                }
            }
        }
        .task {
            for await currentEffect in viewModel.effect {
                switch currentEffect {
                case _ as ProfileEffect.AuthSuccess:
                    break
                case let errorEffect as ProfileEffect.AuthError:
                    self.alertMessage = errorEffect.message
                    self.showAlert = true
                default:
                    break
                }
            }
        }
    }
}
