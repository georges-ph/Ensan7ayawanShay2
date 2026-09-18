import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../models/users_model.dart';
import '../services/firestore_paths.dart';

/// All registered users, ordered by name, same query as UsersActivity.loadUsers.
final allUsersProvider = FutureProvider<List<UsersModel>>((ref) async {
  final snapshot =
      await FirestorePaths.usersCollection().orderBy('name').get();

  return snapshot.docs.map((doc) => UsersModel.fromJson(doc.data())).toList();
});
