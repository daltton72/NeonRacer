# NeonRacer Multiplayer Setup — v34.03

## Implemented in this build

- Firebase Realtime Database room/lobby connection.
- Firebase Anonymous Authentication for guest play (no email required).
- Local guest display name and Firebase guest profile.
- Quick matchmaking for 8-player circuit or elimination rooms.
- Private six-digit room codes and joining by code.
- Two-player head-to-head room capacity.
- Up to eight drivers in circuit and elimination rooms.
- Ready status, roster, lobby chat streaming, host start and random track assignment.
- The host selects one random entry from the game's current track catalog at race start; every connected room client receives that same track index.
- Elimination stages: the host periodically marks the lowest-progress active driver as eliminated; eliminated clients are returned to the home screen.

## Important limits

This is an online lobby, room/chat, and synchronized race-start foundation. It is **not yet complete live online multiplayer racing**. Cars are not yet streamed between clients, remote car positions are not rendered as network opponents, race physics/checkpoints/finishing order are not server-authoritative, and elimination is currently a coarse periodic progress comparison. The game still runs its existing local race/AI simulation after the shared start signal.

Guest sign-in is implemented. A persistent username-and-password account without email is **not** implemented yet. Do not store or compare passwords in the client or directly in Realtime Database. A safe password-based username account requires a trusted backend (for example Firebase Cloud Functions) to validate credentials and issue Firebase custom tokens, plus password hashing/rate limiting and account recovery design.

The supplied Realtime Database rules are starter rules for development, not a production security audit. Before public launch, move room creation, matchmaking, host authority, race progression, elimination decisions, and results into trusted server functions. Add moderation, abuse controls, reconnect handling and rate limits.

## Setup

1. Create a project at https://console.firebase.google.com/.
2. Add a Web App and copy its Firebase config.
3. In Firebase Console → Authentication → Sign-in method, enable **Anonymous**.
4. In Build → Realtime Database, create a database and copy its database URL.
5. Open `NeonRacer_v34.03_Multiplayer_Chat_Ready_Elimination_Fix.html`. Find `FIREBASE_CONFIG` inside script `nr-multiplayer-online-v3402` and replace the placeholder values with your project's exact `apiKey`, `authDomain`, `databaseURL`, `projectId`, and `appId`.
6. In Realtime Database → Rules, paste `firebase-database.rules.json` and publish.
7. Host the updated HTML over HTTPS. Anonymous sign-in and Realtime Database calls need a valid hosted origin and the correct Firebase project configuration.
8. Test on two devices: create a room, join by code, mark both ready, then have the host press Host Start. Both devices should receive the same randomly selected track.

## Modes

- **8-player circuit:** standard three-lap circuit format on one randomly selected track.
- **Elimination:** up to eight drivers; the host removes the lowest-progress active driver at timed stages.
- **Head-to-head:** private room capped at two drivers.
- **Private room:** share the generated six-digit code. Select the race mode before creating the room.
