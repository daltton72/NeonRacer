# NeonRacer Capacitor test build

This is a separate Capacitor 8 packaging path for the existing NeonRacer v31.91 HTML game. It does not replace the existing `NeonRacer-Android` WebView project or its build workflow.

## Build with GitHub Actions

1. Open https://github.com/daltton72/NeonRacer/actions
2. Select **Build NeonRacer Capacitor APK**.
3. Click **Run workflow**, then click the green **Run workflow** button.
4. Wait for the workflow to finish.
5. Open the completed run and download **NeonRacer-Capacitor-APK** under Artifacts.
6. Extract the ZIP and install `app-debug.apk`.

The workflow uses Node 22, Capacitor 8, Java 21, and Android SDK 36.

## Test app identity

The Capacitor test build uses application ID `com.neonracer.game.capacitor`. This is intentionally different from the existing Android wrapper's ID, so both test apps can be installed side by side. It is a separate test installation; saved progress is not shared between the two apps.

## What is included

- The exact v31.91 APK-preparation HTML file copied into the APK as a local web asset.
- Capacitor Android runtime and HTTPS-style local origin.
- Landscape orientation and immersive fullscreen.
- Existing game JavaScript and its browser motion-sensor fallback. A real-device test is required to confirm tilt steering in the Capacitor WebView.
- Remote R2/CDN-hosted car models, textures, and audio still require internet access.

## Important

This first Capacitor build is a packaging experiment, not a claim that tilt, CORS, remote assets, or gameplay have already been verified on a phone. If the game reports missing remote resources, check the R2 CORS policy for the Capacitor local origin. The previous native WebView APK workflow remains available separately.
