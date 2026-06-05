import SwiftUI

struct AppColors {
    static let primary = Color(red: 225/255, green: 29/255, blue: 72/255) 

    static let darkBackground = Color(red: 15/255, green: 23/255, blue: 42/255) 
    static let darkSurface = Color(red: 30/255, green: 41/255, blue: 59/255) 
    static let darkTextPrimary = Color(red: 248/255, green: 250/255, blue: 252/255) 
    static let darkTextSecondary = Color(red: 148/255, green: 163/255, blue: 184/255) 

    static let lightBackground = Color(red: 248/255, green: 250/255, blue: 252/255) 
    static let lightSurface = Color(red: 255/255, green: 255/255, blue: 255/255) 
    static let lightTextPrimary = Color(red: 15/255, green: 23/255, blue: 42/255) 
    static let lightTextSecondary = Color(red: 100/255, green: 116/255, blue: 139/255) 
}

struct AppTypography {
    static let headingLarge = Font.system(size: 24, weight: .bold)
    static let bodyMedium = Font.system(size: 16, weight: .regular)
    static let labelSmall = Font.system(size: 12, weight: .medium)
}

struct CineVerseButton: View {
    let text: String
    let onClick: () -> Void
    var isLoading: Bool = false
    var enabled: Bool = true
    
    var body: some View {
        Button(action: onClick) {
            ZStack {
                if isLoading {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                } else {
                    Text(text)
                        .font(AppTypography.bodyMedium)
                        .fontWeight(.bold)
                        .foregroundColor(.white)
                }
            }
            .frame(maxWidth: .infinity)
            .frame(height: 48)
            .background(AppColors.primary.opacity(enabled && !isLoading ? 1.0 : 0.5))
            .cornerRadius(12)
        }
        .disabled(!enabled || isLoading)
    }
}

struct CineVerseTextField: View {
    let placeholder: String
    @Binding var text: String
    var isSecure: Bool = false
    var leadingIcon: String? = nil
    
    @Environment(\.colorScheme) var colorScheme
    
    var body: some View {
        HStack {
            if let icon = leadingIcon {
                Image(systemName: icon)
                    .foregroundColor(.gray)
                    .padding(.leading, 12)
            }
            
            if isSecure {
                SecureField(placeholder, text: $text)
                    .font(AppTypography.bodyMedium)
                    .padding(12)
            } else {
                TextField(placeholder, text: $text)
                    .font(AppTypography.bodyMedium)
                    .padding(12)
            }
            
            if !text.isEmpty {
                Button(action: { text = "" }) {
                    Image(systemName: "xmark.circle.fill")
                        .foregroundColor(.gray)
                        .padding(.trailing, 12)
                }
            }
        }
        .background(colorScheme == .dark ? AppColors.darkSurface : AppColors.lightSurface)
        .cornerRadius(12)
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(Color.gray.opacity(0.3), lineWidth: 1)
        )
    }
}

struct MovieCard: View {
    let imageUrl: String?
    let onClick: () -> Void
    
    @Environment(\.colorScheme) var colorScheme
    
    var body: some View {
        Button(action: onClick) {
            ZStack {
                if let urlString = imageUrl, !urlString.isEmpty, let url = URL(string: urlString) {
                    AsyncImage(url: url) { phase in
                        switch phase {
                        case .success(let image):
                            image
                                .resizable()
                                .aspectRatio(contentMode: .fill)
                                .frame(maxWidth: .infinity, maxHeight: .infinity)
                                .clipped()
                        case .failure(_):
                            ImageFallbackView(systemIconName: "film")
                                .frame(maxWidth: .infinity, maxHeight: .infinity)
                                .clipped()
                        case .empty:
                            BoxPlaceholder()
                                .frame(maxWidth: .infinity, maxHeight: .infinity)
                                .clipped()
                        @unknown default:
                            ImageFallbackView(systemIconName: "film")
                                .frame(maxWidth: .infinity, maxHeight: .infinity)
                                .clipped()
                        }
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .clipped()
                } else {
                    ImageFallbackView(systemIconName: "film")
                }
            }
            .frame(maxWidth: .infinity)
            .aspectRatio(2/3, contentMode: .fit)
            .cornerRadius(16)
            .shadow(color: colorScheme == .dark ? Color.black.opacity(0.4) : Color.clear, radius: 8)
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(colorScheme == .light ? Color.gray.opacity(0.2) : Color.clear, lineWidth: 1)
            )
        }
    }
}

struct ImageFallbackView: View {
    var systemIconName: String = "film"
    
    var body: some View {
        ZStack {
            LinearGradient(
                gradient: Gradient(colors: [
                    Color(red: 30/255, green: 41/255, blue: 59/255), 
                    Color(red: 15/255, green: 23/255, blue: 42/255)  
                ]),
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            Image(systemName: systemIconName)
                .font(.system(size: 28, weight: .medium))
                .foregroundColor(AppColors.primary.opacity(0.7))
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

struct BoxPlaceholder: View {
    var body: some View {
        ZStack {
            Color.gray.opacity(0.1)
            ProgressView()
                .progressViewStyle(CircularProgressViewStyle(tint: AppColors.primary))
        }
    }
}
