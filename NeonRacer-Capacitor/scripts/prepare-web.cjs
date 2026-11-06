const fs = require('node:fs');
const path = require('node:path');

const repoRoot = path.resolve(__dirname, '..', '..');
const source = path.join(repoRoot, 'NeonRacer_v31.91_APK_Preparation_AutoTilt_Immersive.html');
const webDir = path.join(__dirname, '..', 'www');
const destination = path.join(webDir, 'index.html');

if (!fs.existsSync(source)) {
  console.error('NeonRacer source HTML was not found:', source);
  process.exit(1);
}

fs.mkdirSync(webDir, { recursive: true });
fs.copyFileSync(source, destination);
console.log('Prepared Capacitor web asset from:', path.basename(source));
