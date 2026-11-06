package com.neonracer.game

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.Surface
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.WindowCompat
import androidx.webkit.WebViewAssetLoader
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class MainActivity : Activity(), SensorEventListener {
    private lateinit var webView: WebView
    private lateinit var sensorManager: SensorManager
    private var gravitySensor: Sensor? = null
    private var accelerometer: Sensor? = null
    private var pageReady = false
    private var sensorStarted = false
    private var filteredY = 0f
    private var filteredZ = 9.81f
    private var smoothCommand = 0f
    private var lastCommand = 999f

    // Physical-device test may show that this needs to be +1f instead.
    private val steeringSign = -1f
    private val deadZoneDegrees = 3.0
    private val fullLockDegrees = 23.0

    private val assetLoader by lazy {
        WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()
    }

    inner class AndroidBridge {
        @JavascriptInterface fun isAndroidApp(): Boolean = true
        @JavascriptInterface fun enterImmersiveMode() { runOnUiThread { applyImmersiveMode() } }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        applyImmersiveMode()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        webView = WebView(this)
        webView.setBackgroundColor(android.graphics.Color.rgb(2, 9, 14))
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false
        webView.overScrollMode = View.OVER_SCROLL_NEVER
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.databaseEnabled = true
        webView.settings.allowFileAccess = false
        webView.settings.allowContentAccess = false
        webView.settings.loadsImagesAutomatically = true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
        webView.addJavascriptInterface(AndroidBridge(), "NeonRacerAndroid")
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: android.webkit.WebResourceRequest): android.webkit.WebResourceResponse? {
                return assetLoader.shouldInterceptRequest(request.url)
            }
            override fun onPageFinished(view: WebView, url: String) {
                pageReady = true
                notifyTiltReady(true)
                super.onPageFinished(view, url)
            }
        }
        setContentView(webView)
        webView.loadUrl("https://appassets.androidplatform.net/assets/index.html")
    }

    private fun applyImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun currentDisplayRotation(): Int =
        if (android.os.Build.VERSION.SDK_INT >= 30) display?.rotation ?: Surface.ROTATION_0
        else windowManager.defaultDisplay.rotation

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) applyImmersiveMode()
    }

    override fun onResume() {
        super.onResume()
        applyImmersiveMode()
        startNativeTilt()
    }

    override fun onPause() {
        stopNativeTilt()
        super.onPause()
    }

    private fun startNativeTilt() {
        if (sensorStarted) return
        val sensor = gravitySensor ?: accelerometer
        if (sensor == null) {
            notifyTiltReady(false)
            return
        }
        filteredY = 0f
        filteredZ = 9.81f
        smoothCommand = 0f
        lastCommand = 999f
        sensorStarted = sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
        if (!sensorStarted) notifyTiltReady(false)
    }

    private fun stopNativeTilt() {
        if (!sensorStarted) return
        sensorManager.unregisterListener(this)
        sensorStarted = false
        if (pageReady) runOnUiThread {
            if (::webView.isInitialized) webView.evaluateJavascript("window.__neonAndroidSetTiltReady && window.__neonAndroidSetTiltReady(false)", null)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        val remapped = FloatArray(3)
        val axes = when (currentDisplayRotation()) {
            Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
            Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
            Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
            else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
        }
        SensorManager.remapCoordinateSystem(event.values, axes.first, axes.second, remapped)
        val alpha = 0.22f
        filteredY += alpha * (remapped[1] - filteredY)
        filteredZ += alpha * (remapped[2] - filteredZ)
        val angle = Math.toDegrees(atan2(filteredY.toDouble(), max(0.1, abs(filteredZ).toDouble())))
        val magnitude = abs(angle)
        val normalized = if (magnitude <= deadZoneDegrees) 0.0 else {
            val t = min(1.0, (magnitude - deadZoneDegrees) / (fullLockDegrees - deadZoneDegrees))
            t.pow(0.82) * 9.0 * if (angle < 0) -1.0 else 1.0
        }
        val target = (normalized * steeringSign).toFloat()
        smoothCommand += 0.30f * (target - smoothCommand)
        val command = (smoothCommand * 10f).toInt() / 10f
        if (abs(command - lastCommand) >= 0.1f) {
            lastCommand = command
            sendTilt(command)
        }
    }

    private fun sendTilt(command: Float) {
        if (!pageReady || !::webView.isInitialized) return
        val safe = command.coerceIn(-9f, 9f)
        runOnUiThread { webView.evaluateJavascript("window.__neonAndroidSetTilt && window.__neonAndroidSetTilt($safe)", null) }
    }

    private fun notifyTiltReady(ready: Boolean) {
        if (!pageReady || !::webView.isInitialized) return
        runOnUiThread { webView.evaluateJavascript("window.__neonAndroidSetTiltReady && window.__neonAndroidSetTiltReady($ready)", null) }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onDestroy() {
        stopNativeTilt()
        if (::webView.isInitialized) {
            webView.removeJavascriptInterface("NeonRacerAndroid")
            webView.destroy()
        }
        super.onDestroy()
    }
}
