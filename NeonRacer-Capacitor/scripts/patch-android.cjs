const fs = require('node:fs');
const path = require('node:path');

const project = path.resolve(__dirname, '..');
const manifestPath = path.join(project, 'android', 'app', 'src', 'main', 'AndroidManifest.xml');
if (!fs.existsSync(manifestPath)) {
  console.error('Capacitor Android manifest not found:', manifestPath);
  process.exit(1);
}

let manifest = fs.readFileSync(manifestPath, 'utf8');

if (!manifest.includes('android.hardware.sensor.accelerometer')) {
  manifest = manifest.replace(
    /<application\b/,
    '    <uses-feature android:name="android.hardware.sensor.accelerometer" android:required="true" />\n\n    <application'
  );
}

if (!manifest.includes('android:screenOrientation="landscape"')) {
  manifest = manifest.replace(
    /(<activity\b[^>]*android:name="\.MainActivity"[^>]*)(>)/,
    '$1 android:screenOrientation="landscape"$2'
  );
}

fs.writeFileSync(manifestPath, manifest);

const activityDir = path.join(
  project,
  'android',
  'app',
  'src',
  'main',
  'java',
  'com',
  'neonracer',
  'game',
  'capacitor'
);
fs.mkdirSync(activityDir, { recursive: true });

const activitySource = `package com.neonracer.game.capacitor;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        applyImmersiveMode();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            applyImmersiveMode();
        }
    }

    private void applyImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }
    }
}
`;

fs.writeFileSync(path.join(activityDir, 'MainActivity.java'), activitySource);
console.log('Patched Capacitor Android for landscape and immersive fullscreen.');
