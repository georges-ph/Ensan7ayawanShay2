import 'dart:math';

import 'package:cloud_firestore/cloud_firestore.dart';

import 'firestore_paths.dart';
import '../models/entries_model.dart';
import '../models/game_model.dart';

class GameService {
  static const String _alphabet = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';

  String chooseLetter() {
    final random = Random();
    return _alphabet[random.nextInt(_alphabet.length)];
  }

  int currentTimestampMillis() => DateTime.now().millisecondsSinceEpoch;

  Stream<GameModel?> watchRoom(String roomId) {
    return FirestorePaths.roomDocument(roomId).snapshots().map((snapshot) {
      final data = snapshot.data();
      if (data == null) return null;
      return GameModel.fromJson(data);
    });
  }

  Future<void> createRoom({
    required String roomId,
    required String createdBy,
    required List<String> playerIds,
  }) async {
    final scores = {for (final id in playerIds) id: 0};

    final gameModel = GameModel(
      firstStart: true,
      started: false,
      letter: 'A',
      createdBy: createdBy,
      players: playerIds,
      scores: scores,
      timestampMillis: int.parse(roomId),
    );

    await FirestorePaths.roomDocument(roomId).set(gameModel.toJson());
  }

  /// Starts a new round: picks a random letter and clears the current
  /// user's entries for it, matching GameRoomActivity.startGame/loadLetter.
  Future<void> startRound(String roomId) async {
    await FirestorePaths.roomDocument(roomId).update({
      'started': true,
      'first_start': false,
      'letter': chooseLetter(),
    });
  }

  Future<void> stopRound(String roomId) async {
    await FirestorePaths.roomDocument(roomId).update({'started': false});
  }

  Future<void> clearOwnEntries(String roomId, String userId) async {
    await FirestorePaths.entriesDocument(
      roomId,
      userId,
    ).set(const EntriesModel().toJson());
  }

  Future<void> addToOwnScore(String roomId, String userId, int delta) async {
    if (delta == 0) return;
    await FirestorePaths.roomDocument(roomId).update({
      'scores.$userId': FieldValue.increment(delta),
    });
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

  Future<Map<String, EntriesModel>> loadAllEntries(String roomId) async {
    final snapshot = await FirestorePaths.entriesCollection(roomId).get();
    return {
      for (final doc in snapshot.docs) doc.id: EntriesModel.fromJson(doc.data()),
    };
  }
}
