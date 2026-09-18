import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../models/users_model.dart';
import '../services/firestore_paths.dart';
import 'firebase_providers.dart';

final authStateChangesProvider = StreamProvider<User?>((ref) {
  return ref.watch(firebaseAuthProvider).authStateChanges();
});

/// Live view of the signed-in user's Firestore profile document, replacing
/// the original app's static HelperMethods.currentUserModel field.
final currentUserModelProvider = StreamProvider<UsersModel?>((ref) {
  final authState = ref.watch(authStateChangesProvider).value;
  if (authState == null) {
    return Stream.value(null);
  }

  return FirestorePaths.userDocument(authState.uid).snapshots().map((
    snapshot,
  ) {
    final data = snapshot.data();
    if (data == null) return null;
    return UsersModel.fromJson(data);
  });
});
