import 'package:firebase_database/firebase_database.dart';

/// Tracks the current user's online status under /users_online/{uid},
/// same Realtime Database path the original app used. Adds onDisconnect()
/// so presence still clears itself if the app/tab closes without a clean
/// pause event (important on web, where lifecycle callbacks are unreliable).
class PresenceService {
  PresenceService(this._database);

  final FirebaseDatabase _database;

  DatabaseReference _userRef(String userId) {
    return _database.ref('users_online/$userId');
  }

  Future<void> setOnline(String userId) async {
    final ref = _userRef(userId);
    await ref.onDisconnect().set(false);
    await ref.set(true);
  }

  Future<void> setOffline(String userId) async {
    await _userRef(userId).set(false);
  }

  Stream<bool> onlineStatus(String userId) {
    return _userRef(userId).onValue.map((event) {
      return event.snapshot.value as bool? ?? false;
    });
  }
}
