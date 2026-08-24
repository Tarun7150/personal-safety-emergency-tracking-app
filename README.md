# Hybrid Personal Safety & Emergency Tracking Mobile Application

## Overview

A hybrid Android-based personal safety and emergency tracking application designed to provide quick emergency assistance, real-time location tracking, and secure communication with trusted contacts.

The application combines GPS-based location tracking, SOS alerts, SMS communication, Firebase real-time data synchronization, and security features to support users during emergency situations.

## Key Features

- 🚨 **Emergency SOS** – Quickly trigger an emergency alert.
- 📍 **GPS Location Tracking** – Obtain and share the user's current location.
- 🗺️ **Live Location Tracking** – Track the user's location in real time using Google Maps and Firebase.
- 📱 **SMS Emergency Alerts** – Send emergency information and location details to trusted contacts.
- 👥 **Trusted Contacts** – Store and manage emergency contacts.
- ⏱️ **Emergency Timer** – Set a timer for safety monitoring.
- 🔐 **PIN-Based Security** – Includes a PIN mechanism designed to provide additional protection during emergencies.
- ☁️ **Firebase Integration** – Synchronizes emergency and location information in real time.
- 🌐 **Hybrid Emergency Communication** – Supports emergency communication using both online and offline-capable mechanisms.
- 📶 **Network Monitoring** – Detects network availability and supports appropriate emergency communication behavior.

## Technology Stack

- **Language:** Java
- **Platform:** Android
- **IDE:** Android Studio
- **Database / Backend:** Firebase Realtime Database
- **Location:** Google Fused Location Provider
- **Maps:** Google Maps SDK
- **Communication:** SMS
- **Build System:** Gradle
- **UI:** XML-based Android layouts

## Main Modules

### 1. User Authentication
Provides the login and authentication flow for the application.

### 2. Emergency SOS
Allows the user to initiate an emergency response and share important information with trusted contacts.

### 3. Location Tracking
Uses GPS and the Fused Location Provider to obtain the user's latitude and longitude.

### 4. Live Tracking
Displays the user's location on a Google Map and updates the location using Firebase.

### 5. Emergency Contacts
Allows users to manage trusted contacts who can receive emergency alerts.

### 6. Safety Timer
Provides a timer-based safety mechanism for monitoring the user's situation.

### 7. Security
Provides PIN-based protection and a coercion-resistant emergency interaction mechanism.

## Project Structure

```text
SafetyApp/
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/safetyapp/
│   │   │   └── res/
│   │   └── AndroidManifest.xml
│   │
│   ├── build.gradle.kts
│   └── proguard-rules.pro
│
├── gradle/
├── build.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── settings.gradle.kts
└── .gitignore
