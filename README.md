# DemoiLost

DemoiLost is an Android application project configured with Gradle using Kotlin DSL. This README provides instructions for building and running the app.

---

## Prerequisites

- **Java Development Kit (JDK):** Ensure you have JDK 11 or later installed.
- **Android Studio:** Recommended for development, debugging, and running the emulator. [Download Android Studio](https://developer.android.com/studio).
- **Android SDK:** Installed via Android Studio.
- **Gradle:** The project is configured with Gradle, which comes bundled with Android Studio.

---

## Project Setup

1. **Clone the Repository**

   ```bash
   git clone https://your-repository-url.git
   cd DemoiLost
   ```

2. **Open the Project in Android Studio**

    - Launch Android Studio.
    - Select **File > Open** and navigate to the project directory.
    - Allow Android Studio to sync the project and download necessary dependencies.

3. **Verify SDK and Build Tools**

    - Ensure that your Android SDK is up-to-date.
    - Confirm that the correct SDK platforms and build tools are installed via **SDK Manager** in Android Studio.

---

## Building the App

The project uses Gradle for dependency management and build automation. The settings defined in `settings.gradle.kts` configure the repositories used for plugins and dependencies.

1. **Using Android Studio**

    - Open the project.
    - Click on the **Build** menu and select **Make Project** (or press <kbd>Ctrl + F9</kbd> on Windows/Linux or <kbd>Cmd + F9</kbd> on macOS).

2. **Using Command Line**

    - Navigate to the project directory and run:

      ```bash
      ./gradlew build
      ```

    - This command will compile the project and run any unit tests.

---

## Running the App

### On an Emulator

1. **Create an Emulator**

    - Open **AVD Manager** in Android Studio.
    - Create a new virtual device and select a system image.
    - Launch the emulator.

2. **Run the App**

    - Click the **Run** button (green play icon) in Android Studio.
    - Select the running emulator as the deployment target.

### On a Physical Device

1. **Enable Developer Options**

    - On your Android device, go to **Settings > About phone** and tap the **Build number** several times until developer mode is enabled.
    - Enable **USB Debugging** in the **Developer options** menu.

2. **Connect the Device**

    - Connect your device via USB.
    - Allow USB debugging access when prompted on your device.

3. **Deploy the App**

    - Click the **Run** button in Android Studio and select your connected device.
    - Alternatively, install the debug build via command line:

      ```bash
      ./gradlew installDebug
      ```

---

## Troubleshooting

- **Gradle Sync Issues:**  
  If you encounter problems with dependency resolution, try:
    - Checking your internet connection.
    - Invalidating caches in Android Studio via **File > Invalidate Caches / Restart**.

- **SDK Path Problems:**  
  Ensure that your Android SDK path is correctly set in **Project Structure** or via environment variables.

- **Build Failures:**  
  Review the error logs in the **Build** window for specific issues. Often, missing SDK components or misconfigured Gradle settings are the cause.

---

## License

Include license information here (e.g., MIT, Apache, etc.) if applicable.

---

## Contact

For further questions or support, please contact the project maintainer at [your-email@example.com].

