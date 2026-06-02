import SwiftUI
import shared
import UserNotifications

struct DetailsScreen: View {
    let movieId: Int32
    let onNavigateBack: () -> Void
    
    @State private var viewModel = KoinHelperSwift.shared.getDetailsViewModel()
    @State private var state = DetailsState(movieDetails: nil, cast: [], isLoading: false, isFavorite: false, isWatchLater: false, error: nil)
    
    @State private var showDatePicker = false
    @State private var selectedDate = Date()
    @State private var alertMessage: String?
    @State private var showAlert = false
    
    var body: some View {
        ZStack {
            Color(uiColor: .systemBackground).edgesIgnoringSafeArea(.all)
            
            if state.isLoading && state.movieDetails == nil {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: AppColors.primary))
            } else if let details = state.movieDetails {
                BoxDetailsContent(details: details)
            }
        }
        .navigationBarHidden(true)
        .alert(isPresented: $showAlert) {
            Alert(title: Text("Alert"), message: Text(alertMessage ?? ""), dismissButton: .default(Text("OK")))
        }
        .sheet(isPresented: $showDatePicker) {
            VStack(spacing: 20) {
                Text("Select Reminder Time")
                    .font(AppTypography.headingLarge)
                    .padding(.top)
                
                DatePicker("", selection: $selectedDate, in: Date()..., displayedComponents: [.date, .hourAndMinute])
                    .datePickerStyle(WheelDatePickerStyle())
                    .labelsHidden()
                
                CineVerseButton(text: "Schedule Reminder") {
                    let millis = Int64(selectedDate.timeIntervalSince1970 * 1000)
                    viewModel.sendIntent(intent: DetailsIntent.ScheduleReminder(dateTime: String(millis)))
                    showDatePicker = false
                }
                .padding(.horizontal)
                
                Button("Cancel") {
                    showDatePicker = false
                }
                .foregroundColor(.red)
                .padding(.bottom)
            }
            .padding()
            .presentationDetents([.medium])
        }
        .task {
            viewModel.sendIntent(intent: DetailsIntent.LoadDetails(id: movieId))
            
            // Collect State Flow
            for await currentState in viewModel.state {
                if let currentState = currentState { self.state = currentState }
            }
        }
        .task {
            // Collect Effects
            for await currentEffect in viewModel.effect {
                switch currentEffect {
                case let messageEffect as DetailsEffect.ShowMessage:
                    self.alertMessage = messageEffect.message
                    self.showAlert = true
                case let scheduleEffect as DetailsEffect.ScheduleNotification:
                    if let millis = Int64(scheduleEffect.releaseDate), let title = detailsTitle() {
                        let date = Date(timeIntervalSince1970: TimeInterval(millis / 1000))
                        scheduleLocalNotification(movieTitle: title, date: date)
                        self.alertMessage = "Scheduled alarm for \(title)"
                        self.showAlert = true
                    }
                default:
                    break
                }
            }
        }
    }
    
    private func detailsTitle() -> String? {
        return state.movieDetails?.title
    }
    
    @ViewBuilder
    private func BoxDetailsContent(details: MovieDetails) -> some View {
        ZStack(alignment: .bottom) {
            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    
                    // Backdrop header
                    ZStack(alignment: .bottom) {
                        if let path = details.backdropPath ?? details.posterPath, let url = URL(string: path) {
                            AsyncImage(url: url) { image in
                                image.resizable().aspectRatio(contentMode: .fill)
                            } placeholder: {
                                ProgressView()
                            }
                        }
                        
                        LinearGradient(
                            gradient: Gradient(colors: [.clear, Color(uiColor: .systemBackground)]),
                            startPoint: .top,
                            endPoint: .bottom
                        )
                    }
                    .frame(height: 280)
                    .clipped()
                    
                    // Poster & Description info
                    HStack(alignment: .bottom, spacing: 16) {
                        ZStack {
                            if let path = details.posterPath, let url = URL(string: path) {
                                AsyncImage(url: url) { image in
                                    image.resizable().aspectRatio(contentMode: .fill)
                                } placeholder: {
                                    ProgressView()
                                }
                            }
                        }
                        .frame(width: 110, height: 165)
                        .cornerRadius(12)
                        .clipped()
                        
                        VStack(alignment: .leading, spacing: 6) {
                            Text(details.title)
                                .font(.title3)
                                .fontWeight(.bold)
                                .lineLimit(2)
                            
                            Text("Release: \(details.releaseDate)")
                                .font(AppTypography.labelSmall)
                                .foregroundColor(.gray)
                            
                            Text("Rating: ★ \(String(format: "%.1f", details.voteAverage)) (\(details.runtime) min)")
                                .font(AppTypography.labelSmall)
                                .foregroundColor(AppColors.primary)
                                .fontWeight(.bold)
                        }
                        Spacer()
                    }
                    .padding(.horizontal)
                    .padding(.top, -40)
                    
                    // Genre chips list
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(details.genres, id: \.id) { genre in
                                Text(genre.name)
                                    .font(AppTypography.labelSmall)
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 6)
                                    .background(Color.gray.opacity(0.1))
                                    .cornerRadius(8)
                            }
                        }
                        .padding(.horizontal)
                        .padding(.vertical, 12)
                    }
                    
                    // Synopsis text
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Synopsis")
                            .font(AppTypography.headingLarge)
                            .fontWeight(.bold)
                        
                        Text(details.overview)
                            .font(AppTypography.bodyMedium)
                            .foregroundColor(.gray)
                    }
                    .padding(.horizontal)
                    .padding(.vertical, 8)
                    
                    // Cast list scroller
                    if !state.cast.isEmpty {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Top Cast")
                                .font(AppTypography.headingLarge)
                                .fontWeight(.bold)
                                .padding(.horizontal)
                            
                            ScrollView(.horizontal, showsIndicators: false) {
                                HStack(spacing: 16) {
                                    ForEach(state.cast, id: \.id) { actor in
                                        VStack(alignment: .center, spacing: 4) {
                                            ZStack {
                                                if let path = actor.profilePath, let url = URL(string: path) {
                                                    AsyncImage(url: url) { image in
                                                        image.resizable().aspectRatio(contentMode: .fill)
                                                    } placeholder: {
                                                        ProgressView()
                                                    }
                                                }
                                            }
                                            .frame(width: 56, height: 56)
                                            .clipShape(Circle())
                                            
                                            Text(actor.name)
                                                .font(.system(size: 10, weight: .bold))
                                                .lineLimit(1)
                                                .frame(width: 70)
                                            
                                            Text(actor.character)
                                                .font(.system(size: 9, weight: .regular))
                                                .foregroundColor(.gray)
                                                .lineLimit(1)
                                                .frame(width: 70)
                                        }
                                    }
                                }
                                .padding(.horizontal)
                            }
                        }
                        .padding(.vertical, 8)
                    }
                    
                    Spacer().frame(height: 100) // Bottom spacing
                }
            }
            .edgesIgnoringSafeArea(.top)
            
            // Header buttons
            VStack {
                HStack {
                    Button(action: onNavigateBack) {
                        Image(systemName: "chevron.backward")
                            .foregroundColor(.white)
                            .padding(10)
                            .background(Color.black.opacity(0.5))
                            .clipShape(Circle())
                    }
                    
                    Spacer()
                    
                    HStack(spacing: 12) {
                        Button(action: { showDatePicker = true }) {
                            Image(systemName: "bell.fill")
                                .foregroundColor(.white)
                                .padding(10)
                                .background(Color.black.opacity(0.5))
                                .clipShape(Circle())
                        }
                        
                        Button(action: { viewModel.sendIntent(intent: DetailsIntent.ToggleFavorite()) }) {
                            Image(systemName: state.isFavorite ? "heart.fill" : "heart")
                                .foregroundColor(state.isFavorite ? AppColors.primary : .white)
                                .padding(10)
                                .background(Color.black.opacity(0.5))
                                .clipShape(Circle())
                        }
                    }
                }
                .padding()
                Spacer()
            }
            
            // Floating sticky Watch Later button
            CineVerseButton(
                text: state.isWatchLater ? "Remove from Watch Later" : "Add to Watch Later",
                onClick: { viewModel.sendIntent(intent: DetailsIntent.ToggleWatchLater()) }
            )
            .padding()
            .background(LinearGradient(gradient: Gradient(colors: [.clear, Color(uiColor: .systemBackground)]), startPoint: .top, endPoint: .bottom))
        }
    }
    
    private func scheduleLocalNotification(movieTitle: String, date: Date) {
        let center = UNUserNotificationCenter.current()
        
        center.requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
            if granted {
                let content = UNMutableNotificationContent()
                content.title = "Movie Night Reminder!"
                content.body = "Don't forget to watch: \(movieTitle)!"
                content.sound = .default
                
                let triggerDate = Calendar.current.dateComponents([.year, .month, .day, .hour, .minute], from: date)
                let trigger = UNCalendarNotificationTrigger(dateMatching: triggerDate, repeats: false)
                
                let request = UNNotificationRequest(identifier: movieTitle, content: content, trigger: trigger)
                center.add(request) { error in
                    if let error = error {
                        print("Error adding notification request: \(error)")
                    }
                }
            }
        }
    }
}
