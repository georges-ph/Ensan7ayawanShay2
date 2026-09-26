import 'dart:async';

import 'package:firebase_database/firebase_database.dart';

/// Tracks the current user's online status under /users_online/{uid},
/// same Realtime Database path the original app used.
///
/// Follows Firebase's documented presence pattern: re-registers
/// onDisconnect() and writes true every time the client (re)connects, via
/// .info/connected, rather than tying online/offline to app lifecycle
/// events. A single one-off setOnline() call can otherwise race with its
/// own onDisconnect hook across a dropped/replaced connection (page
/// reload, network blip, hot restart) and leave the value stuck on false
/// even while the user is actively using the app - lifecycle pause/resume
/// isn't a reliable proxy for connection state on web anyway.
class PresenceService {
  PresenceService(this._database);

  final FirebaseDatabase _database;
  StreamSubscription<DatabaseEvent>? _connectionSub;

  DatabaseReference _userRef(String userId) {
    return _database.ref('users_online/$userId');
  }

  /// Starts tracking [userId] as online for as long as the connection to
  /// Realtime Database stays up, self-healing on every reconnect. Call
  /// once after sign in; call [stopTracking] on sign out.
  void startTracking(String userId) {
    _connectionSub?.cancel();

    final userRef = _userRef(userId);
    _connectionSub = _database.ref('.info/connected').onValue.listen((
      event,
    ) async {
      final connected = event.snapshot.value as bool? ?? false;
      if (!connected) return;

      await userRef.onDisconnect().set(false);
      await userRef.set(true);
    });
  }

  Future<void> stopTracking(String userId) async {
    await _connectionSub?.cancel();
    _connectionSub = null;
    await _userRef(userId).onDisconnect().cancel();
    await setOffline(userId);
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
