import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../models/rooms_model.dart';
import '../services/firestore_paths.dart';
import 'auth_providers.dart';

/// Rooms the current user is a player in, newest first, same query as
/// RoomsActivity.loadRooms (players array-contains uid).
final myRoomsProvider = StreamProvider<List<RoomsModel>>((ref) {
  final user = ref.watch(authStateChangesProvider).value;
  if (user == null) return Stream.value(const []);

  return FirestorePaths.roomsCollection()
      .where('players', arrayContains: user.uid)
      .snapshots()
      .map((snapshot) {
        final rooms = snapshot.docs
            .map((doc) => RoomsModel.fromJson(doc.data()))
            .toList();
        rooms.sort((a, b) => b.timestampMillis.compareTo(a.timestampMillis));
        return rooms;
      });
});
