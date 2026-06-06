import SwiftUI
import shared
import UserNotifications

struct DetailsScreen: View {
    let movieId: Int32
    let onNavigateBack: () -> Void

    @State private var viewModel = KoinHelperSwift.shared.getDetailsViewModel()
    @State private var state = DetailsState(movieDetails: nil, cast: [], isLoading: false, isFavorite: false, isWatchLater: false, error: nil, noteText: nil, currentUsername: nil)
    
    @State private var showDatePicker = false
    @State private var selectedDate = Date()
    @State private var alertMessage: String?
    @State private var showAlert = false
    
    @State private var noteInputText = ""
    @State private var isEditingNote = false
    
    var body: some View {
        ZStack {
            AppColors.background.edgesIgnoringSafeArea(.all)

            if state.isLoading && state.movieDetails == nil {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: AppColors.primary))
            } else if let details = state.movieDetails {
                DetailsContent(
                    details: details,
                    cast: state.cast,
                    isFavorite: state.isFavorite,
                    isWatchLater: state.isWatchLater,
                    onNavigateBack: onNavigateBack,
                    onShowDatePicker: { showDatePicker = true },
                    onToggleFavorite: { viewModel.sendIntent(intent: DetailsIntent.ToggleFavorite()) },
                    onToggleWatchLater: { viewModel.sendIntent(intent: DetailsIntent.ToggleWatchLater()) }
                )
            }
        }
        .navigationBarHidden(true)
        .alert(isPresented: $showAlert) {
            Alert(
                title: Text("Alert"),
                message: Text(alertMessage ?? ""),
                dismissButton: .default(Text("OK"))
            )
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

            for await currentState in viewModel.state {
                if let currentState = currentState { self.state = currentState }
            }
        }
        .task {
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
        .onChange(of: state.noteText) { newNote in
            if let note = newNote as String? {
                self.noteInputText = note
                self.isEditingNote = false
            } else {
                self.noteInputText = ""
                self.isEditingNote = true
            }
        }
    }

    private func detailsTitle() -> String? {
        return state.movieDetails?.title
    }

    private func scheduleLocalNotification(movieTitle: String, date: Date) {
        let center = UNUserNotificationCenter.current()

        center.requestAuthorization(options: [.alert, .sound, .badge]) { granted, _ in
            if granted {
                let content = UNMutableNotificationContent()
                content.title = "Movie Night Reminder!"
                content.body = "Don't forget to watch: \(movieTitle)!"
                content.sound = .default

                let triggerDate = Calendar.current.dateComponents(
                    [.year, .month, .day, .hour, .minute],
                    from: date
                )
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

struct DetailsContent: View {
    let details: MovieDetails
    let cast: [CastMember]
    let isFavorite: Bool
    let isWatchLater: Bool
    let onNavigateBack: () -> Void
    let onShowDatePicker: () -> Void
    let onToggleFavorite: () -> Void
    let onToggleWatchLater: () -> Void

    var body: some View {
        ZStack(alignment: .bottom) {
            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    BackdropView(path: details.backdropPath ?? details.posterPath)

                    HeaderView(details: details)

                    GenresView(genres: details.genres)

                    SynopsisView(overview: details.overview)

                    if !cast.isEmpty {
                        CastView(cast: cast)
                    }

                    Spacer().frame(height: 100)
                }
            }
            .edgesIgnoringSafeArea(.top)

            NavigationOverlay(
                isFavorite: isFavorite,
                onNavigateBack: onNavigateBack,
                onShowDatePicker: onShowDatePicker,
                onToggleFavorite: onToggleFavorite
            )

            CineVerseButton(
                text: isWatchLater ? "Remove from Watch Later" : "Add to Watch Later",
                onClick: onToggleWatchLater
            )
            .padding()
            .background(
                LinearGradient(
                    gradient: Gradient(colors: [.clear, AppColors.background]),
                    startPoint: .top,
                    endPoint: .bottom
                )
            )
        }
    }
}

struct BackdropView: View {
    let path: String?

    var body: some View {
        ZStack(alignment: .bottom) {
            GeometryReader { geo in
                if let path = path, let url = URL(string: path) {
                    AsyncImage(url: url) { phase in
                        switch phase {
                        case .success(let image):
                            image
                                .resizable()
                                .scaledToFill()
                                .frame(width: geo.size.width, height: geo.size.height)
                        case .failure:
                            ImageFallbackView(systemIconName: "photo")
                                .frame(width: geo.size.width, height: geo.size.height)
                        case .empty:
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: AppColors.primary))
                                .frame(width: geo.size.width, height: geo.size.height)
                        @unknown default:
                            ImageFallbackView(systemIconName: "photo")
                                .frame(width: geo.size.width, height: geo.size.height)
                        }
                    }
                } else {
                    ImageFallbackView(systemIconName: "photo")
                        .frame(width: geo.size.width, height: geo.size.height)
                }
            }

            LinearGradient(
                gradient: Gradient(colors: [.clear, AppColors.background]),
                startPoint: .top,
                endPoint: .bottom
            )
        }
        .frame(height: 280)
        .clipped()
    }
}

struct HeaderView: View {
    let details: MovieDetails

    var body: some View {
        HStack(alignment: .bottom, spacing: 16) {
            ZStack {
                if let path = details.posterPath, let url = URL(string: path) {
                    AsyncImage(url: url) { phase in
                        switch phase {
                        case .success(let image):
                            image
                                .resizable()
                                .aspectRatio(contentMode: .fill)
                                .frame(width: 110, height: 165)
                                .clipped()
                        case .failure:
                            ImageFallbackView(systemIconName: "film")
                                .frame(width: 110, height: 165)
                        case .empty:
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: AppColors.primary))
                                .frame(width: 110, height: 165)
                        @unknown default:
                            ImageFallbackView(systemIconName: "film")
                                .frame(width: 110, height: 165)
                        }
                    }
                    .frame(width: 110, height: 165)
                    .clipped()
                } else {
                    ImageFallbackView(systemIconName: "film")
                        .frame(width: 110, height: 165)
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
    }
}

struct GenresView: View {
    let genres: [Genre]

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(genres, id: \.id) { genre in
                    Text(genre.name)
                        .font(AppTypography.labelSmall)
                        .foregroundColor(AppColors.textPrimary)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6)
                        .background(AppColors.surface)
                        .cornerRadius(8)
                }
            }
            .padding(.horizontal)
            .padding(.vertical, 12)
        }
    }
}

struct SynopsisView: View {
    let overview: String

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Synopsis")
                .font(AppTypography.headingLarge)
                .fontWeight(.bold)
                .foregroundColor(AppColors.textPrimary)

            Text(overview)
                .font(AppTypography.bodyMedium)
                .foregroundColor(AppColors.textSecondary)
        }
        .padding(.horizontal)
        .padding(.vertical, 8)
    }
}

struct CastView: View {
    let cast: [CastMember]

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Top Cast")
                .font(AppTypography.headingLarge)
                .fontWeight(.bold)
                .padding(.horizontal)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 16) {
                    ForEach(cast, id: \.id) { actor in
                        VStack(alignment: .center, spacing: 4) {
                            ZStack {
                                    if let path = actor.profilePath, let url = URL(string: path) {
                                        AsyncImage(url: url) { phase in
                                            switch phase {
                                            case .success(let image):
                                                image
                                                    .resizable()
                                                    .aspectRatio(contentMode: .fill)
                                                    .frame(width: 56, height: 56)
                                                    .clipped()
                                            case .failure:
                                                ImageFallbackView(systemIconName: "person.fill")
                                                    .frame(width: 56, height: 56)
                                            case .empty:
                                                ProgressView()
                                                    .progressViewStyle(
                                                        CircularProgressViewStyle(tint: AppColors.primary)
                                                    )
                                                    .frame(width: 56, height: 56)
                                            @unknown default:
                                                ImageFallbackView(systemIconName: "person.fill")
                                                    .frame(width: 56, height: 56)
                                            }
                                        }
                                        .frame(width: 56, height: 56)
                                        .clipped()
                                    } else {
                                        ImageFallbackView(systemIconName: "person.fill")
                                            .frame(width: 56, height: 56)
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

                    VStack(alignment: .leading, spacing: 12) {
                        Text("My Notes")
                            .font(AppTypography.headingLarge)
                            .fontWeight(.bold)
                            .foregroundColor(AppColors.textPrimary)
                        
                        if isEditingNote {
                            VStack(alignment: .trailing, spacing: 8) {
                                TextEditor(text: $noteInputText)
                                    .frame(height: 100)
                                    .padding(8)
                                    .background(AppColors.surface)
                                    .cornerRadius(12)
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 12)
                                            .stroke(Color.gray.opacity(0.3), lineWidth: 1)
                                    )
                                
                                HStack(spacing: 8) {
                                    if state.noteText != nil {
                                        Button("Cancel") {
                                            noteInputText = state.noteText as String? ?? ""
                                            isEditingNote = false
                                        }
                                        .foregroundColor(.gray)
                                        .padding(.horizontal)
                                    }
                                    
                                    CineVerseButton(text: "Save Note") {
                                        viewModel.sendIntent(intent: DetailsIntent.SaveNote(text: noteInputText))
                                        isEditingNote = false
                                    }
                                    .frame(width: 120)
                                }
                            }
                        } else {
                            VStack(alignment: .leading, spacing: 12) {
                                Text(state.noteText as String? ?? "")
                                    .font(AppTypography.bodyMedium)
                                    .foregroundColor(AppColors.textSecondary)
                                    .padding()
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                    .background(AppColors.surface)
                                    .cornerRadius(12)
                                
                                HStack {
                                    Spacer()
                                    Button("Delete") {
                                        viewModel.sendIntent(intent: DetailsIntent.DeleteNote())
                                    }
                                    .foregroundColor(.red)
                                    .padding(.horizontal)
                                    
                                    CineVerseButton(text: "Edit") {
                                        isEditingNote = true
                                    }
                                    .frame(width: 100)
                                }
                            }
                        }
                    }
                    .padding(.horizontal)
                    .padding(.vertical, 8)
                    
                    Spacer().frame(height: 100) 
                }
                .padding(.horizontal)
            }
        }
        .padding(.vertical, 8)
    }
}

struct NavigationOverlay: View {
    let isFavorite: Bool
    let onNavigateBack: () -> Void
    let onShowDatePicker: () -> Void
    let onToggleFavorite: () -> Void

    var body: some View {
        VStack {
            HStack {
                Button(action: onNavigateBack, label: {
                    Image(systemName: "chevron.backward")
                        .foregroundColor(.white)
                        .padding(10)
                        .background(Color.black.opacity(0.5))
                        .clipShape(Circle())
                })

                Spacer()

                HStack(spacing: 12) {
                    Button(action: onShowDatePicker, label: {
                        Image(systemName: "bell.fill")
                            .foregroundColor(.white)
                            .padding(10)
                            .background(Color.black.opacity(0.5))
                            .clipShape(Circle())
                    })

                    Button(action: onToggleFavorite, label: {
                        Image(systemName: isFavorite ? "heart.fill" : "heart")
                            .foregroundColor(isFavorite ? AppColors.primary : .white)
                            .padding(10)
                            .background(Color.black.opacity(0.5))
                            .clipShape(Circle())
                    })
                }
            }
            .padding()
            Spacer()
        }
    }
}
