# NeonRacer Android project (v31.91)

This is a native Android WebView wrapper for the existing NeonRacer v31.91 HTML game.

## Included

- Full game HTML bundled at app/src/main/assets/index.html.
- Native Android SensorManager tilt bridge, automatically started when the Activity resumes; steering is sent from -9 (left) to +9 (right).
- Landscape-only launch and immersive fullscreen. Status and navigation bars stay hidden, with a swipe able to reveal them temporarily.
- AndroidX WebView Asset Loader serves the bundled game from an HTTPS-like local origin instead of file://.
- Internet permission for the game's existing Cloudflare R2 and CDN-hosted models, textures, and audio.

## Build on Windows 10

1. Install stable Android Studio and Android SDK Platform 35.
2. Extract this entire NeonRacer-Android folder.
3. In Android Studio choose Open and select the NeonRacer-Android folder (the one containing settings.gradle.kts).
4. Wait for Gradle sync. Approve downloads for Gradle, Android Gradle Plugin, Kotlin, and SDK components if prompted.
5. Choose Build > Build Bundle(s) / APK(s) > Build APK(s).
6. Click locate in the build notification. The debug APK is app/build/outputs/apk/debug/app-debug.apk.
7. Copy the APK to the Galaxy S20 FE and install it. Sideloading does not require a Play Store account.

The project uses JDK 17, Gradle 8.10.2, Android Gradle Plugin 8.8.2, Kotlin 2.0.21, and compile SDK 35. Android Studio's bundled JDK 17 is recommended.

## Game asset/network behavior

The game shell is bundled locally, so it does not need to download at launch. Existing models, textures, and music still come from Cloudflare R2 and other HTTPS CDNs and therefore need internet access. Since the local page is served from https://appassets.androidplatform.net, remote hosts must allow cross-origin requests from that origin for JavaScript/WebGL fetches. If R2 resources fail with CORS errors, configure the neonracer R2 bucket CORS policy to allow origin https://appassets.androidplatform.net, methods GET and HEAD, and headers such as Range and Content-Type; expose Accept-Ranges, Content-Length, Content-Range, and Content-Type. Mixed HTTP resources are blocked.

## Tilt behavior

Tilt starts automatically without a browser permission prompt or in-game enable button. The bridge uses display-rotated gravity, a 3-degree dead zone, 23-degree full-steer angle, and smoothing. A real phone test is still needed to verify the direction and sensitivity. If steering is reversed, change steeringSign from -1f to +1f in app/src/main/java/com/neonracer/game/MainActivity.kt, rebuild, and reinstall.

## App identity and release

Application ID: com.neonracer.game. Debug builds use com.neonracer.game.debug. Release builds are not signed here; use Build > Generate Signed Bundle / APK in Android Studio to create a signed release APK.

This is the Android Studio source project, not a prebuilt APK. Android Studio compiles it locally; first build requires internet to download dependencies and Android build tools.
