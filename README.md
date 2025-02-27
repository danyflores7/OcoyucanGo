# OcoyucanGo - Android Application

OcoyucanGo is a mobile application developed to promote ecotourism and biodiversity conservation in the Ocoyucan region. This Android version, which I was responsible for, allows users to:

- **Authenticate and manage their profile** using Firebase Authentication.
- **Track routes:** Users can start and stop journeys, with the app automatically recording the distance, duration, and points earned (stored in Firestore).
- **Identify plant species:** Captures images using the device camera and utilizes the PlantNet API to identify species.
- **Real-time data synchronization:** Connects to Firebase (Firestore, Storage, and Cloud Functions) to ensure up-to-date information.
- **View achievements and completed routes,** so users can monitor their progress.

> **Note:** This Android version has not been published on the Play Store; it was presented as a demo to the training partner.

---

## Main Features

- **Authentication and Registration:** Allows users to sign up and log in.
- **Route Tracking:** Users can start and stop a journey; the app automatically logs details such as distance, duration, and points earned.
- **Species Identification:** Captures images via the camera and uses the PlantNet API to identify plant species.
- **Profile Management:** Users can update their profile picture and edit personal information.
- **Real-time Synchronization:** The app connects to Firebase to update and store user data, routes, and identifications in real time.

---

## Technologies Used

- **Languages:** Kotlin and Java
- **IDE:** Android Studio
- **Backend:** Firebase (Authentication, Firestore, Storage, Cloud Functions)
- **APIs:** PlantNet API for species identification; Google Maps for route mapping
- **Architecture:** MVVM (Model-View-ViewModel)
