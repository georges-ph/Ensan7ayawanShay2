import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../models/users_model.dart';
import '../services/firestore_paths.dart';

/// All registered users, ordered by name, same query as UsersActivity.loadUsers.
final allUsersProvider = FutureProvider<List<UsersModel>>((ref) async {
  final snapshot =
      await FirestorePaths.usersCollection().orderBy('name').get();

  return snapshot.docs.map((doc) => UsersModel.fromJson(doc.data())).toList();
});

/// [allUsersProvider] keyed by id, for screens that need to look up
/// another user's profile (e.g. a room's creator) -- reuses the same list
/// read instead of an extra per-user document fetch.
final usersByIdProvider = Provider<AsyncValue<Map<String, UsersModel>>>((
  ref,
) {
  return ref
      .watch(allUsersProvider)
      .whenData((users) => {for (final u in users) u.id: u});
});
