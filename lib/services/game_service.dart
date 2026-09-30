import 'dart:math';

import 'package:cloud_firestore/cloud_firestore.dart';

import 'firestore_paths.dart';
import '../models/entries_model.dart';
import '../models/game_model.dart';

class GameService {
  static const String _alphabet = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';

  // Excludes visually-ambiguous characters (0/O, 1/I/L) for the fallback
  // random code, so a spoken/handwritten code stays unambiguous.
  static const String _codeAlphabet = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
  static const int _codeLength = 6;

  final _random = Random();

  String chooseLetter() {
    return _alphabet[_random.nextInt(_alphabet.length)];
  }

  int currentTimestampMillis() => DateTime.now().millisecondsSinceEpoch;

  Stream<GameModel?> watchRoom(String roomId) {
    return FirestorePaths.roomDocument(roomId).snapshots().map((snapshot) {
      final data = snapshot.data();
      if (data == null) return null;
      return GameModel.fromJson(data);
    });
  }

  /// Looks up a room by its share code (case/space-insensitive). Returns
  /// the room id, or null if no room has that code.
  Future<String?> findRoomIdByCode(String code) async {
    final normalized = code.trim().toUpperCase();
    if (normalized.isEmpty) return null;

    final snapshot = await FirestorePaths.roomsCollection()
        .where('code', isEqualTo: normalized)
        .limit(1)
        .get();

    if (snapshot.docs.isEmpty) return null;
    return snapshot.docs.first.id;
  }

  /// Adds [userId] to an existing room's players, safe to call even if
  /// they're already in it.
  Future<void> joinRoom(String roomId, String userId) async {
    await FirestorePaths.roomDocument(roomId).update({
      'players': FieldValue.arrayUnion([userId]),
      'scores.$userId': FieldValue.increment(0),
    });
  }

  Future<void> createRoom({
    required String roomId,
    required String createdBy,
    required List<String> playerIds,
  }) async {
    final scores = {for (final id in playerIds) id: 0};
    final code = await _uniqueRoomCode();

    final gameModel = GameModel(
      firstStart: true,
      started: false,
      letter: 'A',
      createdBy: createdBy,
      players: playerIds,
      scores: scores,
      code: code,
      timestampMillis: int.parse(roomId),
    );

    await FirestorePaths.roomDocument(roomId).set(gameModel.toJson());
  }

  /// A short, easy-to-share room code. Always a full random draw from
  /// [_codeAlphabet] rather than anything derived from the room id, so
  /// codes don't repeat just because two rooms happen to be created
  /// around the same time.
  Future<String> _uniqueRoomCode() async {
    for (var attempt = 0; attempt < 5; attempt++) {
      final candidate = _randomCode();
      final existing = await findRoomIdByCode(candidate);
      if (existing == null) return candidate;
    }
    return _randomCode();
  }

  String _randomCode() {
    return List.generate(
      _codeLength,
      (_) => _codeAlphabet[_random.nextInt(_codeAlphabet.length)],
    ).join();
  }

  /// Starts a new round: picks a random letter and clears the current
  /// user's entries for it, matching GameRoomActivity.startGame/loadLetter.
  /// Also stamps the round's start time so every player's timer stays in
  /// sync, since it's derived from this shared value rather than each
  /// client's own clock the moment its listener fires.
  Future<void> startRound(String roomId) async {
    await FirestorePaths.roomDocument(roomId).update({
      'started': true,
      'first_start': false,
      'letter': chooseLetter(),
      'round_started_millis': currentTimestampMillis(),
    });
  }

  /// Stops the round, recording how long it ran and bumping the rounds
  /// counter - both persisted so they stay correct for anyone who reloads
  /// or joins after the round already ended.
  Future<void> stopRound(
    String roomId, {
    required int roundStartedMillis,
  }) async {
    final elapsedSeconds = roundStartedMillis == 0
        ? 0
        : ((currentTimestampMillis() - roundStartedMillis) / 1000).round();

    await FirestorePaths.roomDocument(roomId).update({
      'started': false,
      'last_round_seconds': elapsedSeconds,
      'rounds_played': FieldValue.increment(1),
    });
  }

  Future<void> clearOwnEntries(String roomId, String userId) async {
    await FirestorePaths.entriesDocument(
      roomId,
      userId,
    ).set(const EntriesModel().toJson());
  }

  Future<void> addToOwnScore(String roomId, String userId, int delta) async {
    if (delta == 0) return;
    await FirestorePaths.roomDocument(
      roomId,
    ).update({'scores.$userId': FieldValue.increment(delta)});
  }

  Future<void> saveOwnEntries(
    String roomId,
    String userId,
    EntriesModel entries,
  ) async {
    if (entries.ensan.isEmpty &&
        entries.hayawan.isEmpty &&
        entries.shay2.isEmpty) {
      return;
    }
    await FirestorePaths.entriesDocument(roomId, userId).set(entries.toJson());
  }

  Future<EntriesModel?> loadOwnEntries(String roomId, String userId) async {
    final snapshot = await FirestorePaths.entriesDocument(roomId, userId).get();
    final data = snapshot.data();
    if (data == null) return null;
    return EntriesModel.fromJson(data);
  }

  Future<Map<String, EntriesModel>> loadAllEntries(String roomId) async {
    final snapshot = await FirestorePaths.entriesCollection(roomId).get();
    return {
      for (final doc in snapshot.docs)
        doc.id: EntriesModel.fromJson(doc.data()),
    };
  }
}
