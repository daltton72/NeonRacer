# NeonRacer Multiplayer Firebase Setup

This repository now includes a Firebase Realtime Database lobby client and starter database rules.

## What the current build implements

- Anonymous Firebase Authentication for guest usernames (no email required).
- Persistent guest display name stored locally and a Firebase profile record.
- Quick matchmaking for circuit or elimination rooms.
- Private room creation with a six-digit invite code and room joining.
- Up to 8 players per standard room; head-to-head rooms are limited to 2.
- Ready state, player roster, room chat, host start, and host-selected random track.
- The host chooses a random track from the game's current `TRACKS` catalog when the race starts; all clients in that room receive the same track index.
- Elimination status updates: the host marks the lowest-progress active driver eliminated at 30-second stages.

**Important:** this is the online lobby and race-start foundation, not a finished real-time multiplayer racing simulation. The current game still runs its local race/AI simulation; remote player car transforms, authoritative collision/physics, anti-cheat validation, reconnect recovery, and robust race-result settlement are not yet implemented. Do not advertise the game as full live online racing until those systems are added and tested.

## 1. Create a Firebase project

1. Open https://console.firebase.google.com/ and create a project.
2. Add a Web App to the project and copy its Firebase configuration.
3. In **Authentication → Sign-in method**, enable **Anonymous**.
4. In **Build → Realtime Database**, create a database.
5. Copy the database URL shown by Firebase.

## 2. Add the Firebase config to the HTML

Open `NeonRacer_v34.02_Firebase_8_Player_Circuit_Elimination.html` and find the `FIREBASE_CONFIG` object in the `nr-multiplayer-online-v3402` script. Replace:

- `YOUR_FIREBASE_WEB_API_KEY`
- `YOUR_PROJECT_ID`
- `YOUR_FIREBASE_APP_ID`

Use the exact `authDomain`, `databaseURL`, `projectId`, and `appId` values from the Firebase Web App configuration. Do not publish any service-account private key or admin SDK credential in the game.

## 3. Install the Realtime Database rules

In Firebase Console → Realtime Database → Rules, paste the contents of `firebase-database.rules.json`, then publish.

These are starter rules, not a production security audit. Before public release, move room creation, matchmaking, host authority, race progression, elimination decisions, and result settlement into trusted Cloud Functions or another server-authoritative service. Client-side rules alone cannot prevent a modified game client from cheating.

## 4. Test with two devices

1. Load the same hosted game build on two devices.
2. Open Multiplayer on both.
3. On device one, create a private room or use Find Match.
4. On device two, join the displayed six-digit code (or use Find Match).
5. Both players mark Ready; the host presses Host Start.
6. Confirm both clients switch to the same randomly assigned track.

For an 8-player circuit or elimination room, invite up to seven additional drivers. Head-to-head rooms cap at two.

## Planned before full public launch

- Stream remote car positions and interpolation at a safe update rate.
- Add server-authoritative race clock, checkpoints, laps, finishing order, and elimination.
- Remove eliminated drivers from active play on every client.
- Add disconnect/rejoin handling and host migration.
- Add moderation, chat rate limits, abuse reporting, and account recovery options.
- Test latency, mobile data use, Firebase costs, and database security.
