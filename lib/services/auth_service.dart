import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/foundation.dart';

import 'firestore_paths.dart';
import '../models/users_model.dart';

class AuthService {
  AuthService(this._auth);

  final FirebaseAuth _auth;

  User? get currentUser => _auth.currentUser;

  Stream<User?> authStateChanges() => _auth.authStateChanges();

  /// Signs in with Google and ensures a matching Users document exists,
  /// mirroring the original app's registerWithFirebase step.
  Future<UsersModel> signInWithGoogle() async {
    final provider = GoogleAuthProvider();

    final credential = kIsWeb
        ? await _auth.signInWithPopup(provider)
        : await _auth.signInWithProvider(provider);

    return _ensureUserDocument(credential.user!);
  }

  Future<UsersModel> _ensureUserDocument(User user) async {
    final userDoc = FirestorePaths.userDocument(user.uid);
    final snapshot = await userDoc.get();

    if (snapshot.exists) {
      return UsersModel.fromJson(snapshot.data()!);
    }

    final usersModel = UsersModel(
      id: user.uid,
      name: user.displayName ?? '',
      email: user.email ?? '',
      image: user.photoURL ?? '',
      fcmToken: '',
      notifications: true,
    );

    await userDoc.set(usersModel.toJson());
    return usersModel;
  }

  Future<void> signOut() => _auth.signOut();
}
