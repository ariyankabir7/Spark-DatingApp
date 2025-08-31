Spark ✨ - A Modern Android Dating App
Welcome to Spark, a modern dating application designed to connect people based on shared interests. Built with a clean architecture and the latest Android development technologies, Spark offers a seamless and engaging user experience.


🚀 About The Project
Spark is a single-activity Android application built using Kotlin and the MVVM architecture. It leverages the power of Firebase for its backend, providing real-time database capabilities, authentication, and file storage to create a dynamic and responsive experience. The primary goal is to help users find meaningful connections by matching them with others who share similar hobbies and passions.

📸 Screenshots
(Here you can add screenshots of your app. These are placeholders.)

Login / Sign Up

Browse Screen

Match List

**

**

**

✅ Features Implemented
Splash Screen: Intelligent routing based on authentication status and profile completeness.

Firebase Authentication: Secure email & password login and sign-up.

Profile Creation: Users can create a detailed profile with a name, age, gender, and photo.

Interest Selection: A dynamic and user-friendly interface for selecting interests from various categories.

Bottom Navigation: Easy navigation between Profile, Browse, and Match screens.

Real-time Database: Firestore integration for instant data synchronization.

Cloud Storage: Profile photos are securely uploaded and stored using Firebase Storage.

Clean Architecture: Follows MVVM and Repository patterns for a scalable and maintainable codebase.

🎯 Future Features
[ ] Advanced Profile Editing

[ ] Smart Filtering in Browse Screen (by interests, age, etc.)

[ ] Real-time Chat Functionality

[ ] Push Notifications for New Matches and Messages

[ ] Lottie Animations for a more fluid UI

[ ] Image Compression for faster uploads

🛠 Tech Stack & Architecture
This project is built with a modern tech stack, focusing on best practices for Android development.

Language: Kotlin

Architecture: MVVM (Model-View-ViewModel) + Repository Pattern

UI: XML with Material Design 3 Components

Asynchronous Programming: Kotlin Coroutines

Dependency Injection: Hilt (recommended for future implementation)

Image Loading: Glide

🔥 Firebase
Authentication: For secure user management.

Firestore: As the primary real-time, NoSQL database.

Storage: For hosting user-uploaded profile images.

आर्किटेक्चर (Jetpack Components)
Navigation Component: For handling all in-app navigation.

ViewModel: To store and manage UI-related data in a lifecycle-conscious way.

LiveData: As a lifecycle-aware observable data holder.

🌊 App Flow
The application follows a logical flow to ensure a smooth user journey from launch to the main home screen.

SplashFragment
      |
      |-- User Not Logged In -> AuthFragment
      |
      '-- User Logged In
            |
            |-- Profile Doc Missing -> AuthFragment
            |
            '-- Profile Exists
                  |
                  |-- Interests Empty -> InterestsFragment
                  |
                  '-- Interests Exist -> HomeFragment

🗂️ Firestore Database Structure
The database is structured to be simple and scalable. The primary collection is users.

Collection: users

Document ID: uid (from Firebase Auth)

Example Document:

{
  "name": "Alice",
  "age": 24,
  "gender": "female",
  "photoUrl": "https://firebasestorage/.../uid.jpg",
  "interests": ["Sports", "Bollywood"],
  "likes": ["uid1", "uid2"],
  "dislikes": ["uid3"],
  "lastSeen": <timestamp>,
  "createdAt": <timestamp>
}

📦 Package Structure
The project is organized into a clean, feature-based package structure.

com.ariyan.spark
 ├── MainActivity.kt
 ├── model/
 │     └── User.kt
 ├── repository/
 │     └── UserRepository.kt
 ├── ui/
 │     ├── splash/
 │     ├── auth/
 │     ├── interests/
 │     └── home/
 │           ├── profile/
 │           ├── browse/
 │           └── match/
 ├── viewmodel/
 │     ├── SplashViewModel.kt
 │     ├── AuthViewModel.kt
 │     └── ...
 └── util/
       └── (TimeUtils.kt, SingleLiveEvent.kt)

⚙️ How To Get Started
To get a local copy up and running, follow these simple steps.

Clone the repository:

git clone [https://github.com/your-username/SparkDatingApp.git](https://github.com/your-username/SparkDatingApp.git)

Connect to Firebase:

Open the project in Android Studio.

Go to Tools -> Firebase and connect your project to your own Firebase instance.

Download the google-services.json file and place it in the app/ directory.

Run the app:

Build and run the project on an Android emulator or a physical device.

Made with ❤️ by Ariyan.
