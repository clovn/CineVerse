import SwiftUI
import shared

struct OnboardingSlide {
    let title: String
    let description: String
    let iconName: String
    let iconColor: Color
}

struct OnboardingScreen: View {
    let onComplete: () -> Void
    
    @State private var currentPage = 0
    
    private let slides = [
        OnboardingSlide(
            title: "Welcome to CineVerse",
            description: "Discover your next favorite movie, track releases, and build your ultimate watchlist in one clean application.",
            iconName: "film",
            iconColor: AppColors.primary
        ),
        OnboardingSlide(
            title: "Dynamic Movie Roll",
            description: "Can't decide what to watch? Roll the interactive 3D dice to randomly select a top-rated movie from our database.",
            iconName: "dice.fill",
            iconColor: .blue
        ),
        OnboardingSlide(
            title: "Smart Watchlist Reminders",
            description: "Add movie releases to your watchlist and schedule custom notifications so you never miss a premiere.",
            iconName: "bell.fill.badge.play.fill",
            iconColor: .green
        )
    ]
    
    var body: some View {
        VStack(spacing: 20) {
            
            HStack {
                Spacer()
                if currentPage < slides.count - 1 {
                    Button(action: onComplete) {
                        Text("Skip")
                            .font(.system(size: 16, weight: .medium))
                            .foregroundColor(AppColors.textSecondary)
                    }
                    .padding(.trailing, 24)
                    .padding(.top, 16)
                } else {
                    Spacer().frame(height: 40)
                }
            }

            TabView(selection: $currentPage) {
                ForEach(0..<slides.count, id: \.self) { index in
                    let slide = slides[index]
                    VStack(spacing: 30) {
                        ZStack {
                            Circle()
                                .fill(slide.iconColor.opacity(0.1))
                                .frame(width: 150, height: 150)
                            
                            Image(systemName: slide.iconName)
                                .resizable()
                                .aspectRatio(contentMode: .fit)
                                .frame(width: 80, height: 80)
                                .foregroundColor(slide.iconColor)
                        }
                        
                        Text(slide.title)
                            .font(AppTypography.headingLarge)
                            .foregroundColor(AppColors.textPrimary)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 24)
                        
                        Text(slide.description)
                            .font(AppTypography.bodyMedium)
                            .foregroundColor(AppColors.textSecondary)
                            .multilineTextAlignment(.center)
                            .lineSpacing(4)
                            .padding(.horizontal, 32)
                    }
                    .tag(index)
                }
            }
            .tabViewStyle(PageTabViewStyle(indexDisplayMode: .never))

            HStack(spacing: 8) {
                ForEach(0..<slides.count, id: \.self) { index in
                    Capsule()
                        .fill(currentPage == index ? AppColors.primary : AppColors.textSecondary.opacity(0.4))
                        .frame(width: currentPage == index ? 24 : 8, height: 8)
                }
            }
            .padding(.bottom, 24)

            let isLastPage = currentPage == slides.count - 1
            CineVerseButton(
                text: isLastPage ? "Get Started" : "Next",
                onClick: {
                    if isLastPage {
                        onComplete()
                    } else {
                        withAnimation {
                            currentPage += 1
                        }
                    }
                }
            )
            .padding(.horizontal, 24)
            .padding(.bottom, 32)
        }
        .background(AppColors.background.edgesIgnoringSafeArea(.all))
    }
}
