# Ensan 7ayawan Shay2 (انسان حيوان شيء)

A real-time multiplayer word game: pick a random letter, then race to name a
person/character, an animal, and a thing that all start with it. Players
self-score each round and compare totals live.

This is a Flutter rewrite of the original native Android app
(`Ensan7ayawanShay2`), targeting **Android and Web only**.

## Stack

- Flutter + Riverpod + go_router
- Firebase: Auth (Google Sign-In), Firestore, Realtime Database (presence)

Firestore/Realtime Database layout intentionally matches the original app
so both read/write the same data:

- `Ensan7ayawanShay2/AppCollections/Users/{uid}`
- `Ensan7ayawanShay2/AppCollections/Rooms/{roomId}` (+ `Entries` subcollection)
- Realtime DB: `users_online/{uid}`

## Setup

Firebase config isn't committed (see `.gitignore`) — regenerate it locally:

```
dart pub global activate flutterfire_cli
flutterfire configure --project=project-1-b5db1 --platforms=android,web
```

## Status

Phase 1 (this branch): core gameplay — auth, create/join rooms, live rounds,
settings. Not yet ported: AdMob, Crashlytics, Sentry, Remote Config,
Analytics, and push notifications for room invitations (planned via a
self-hosted backend).
