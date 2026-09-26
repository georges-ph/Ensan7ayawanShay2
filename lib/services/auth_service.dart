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

  /// Single email/password entry point: creates the Firebase account if the
  /// email is new, or signs in with it if it's already in the (shared)
  /// project, then ensures this app's Users document exists either way.
  /// Never reveals which of the two happened.
  Future<UsersModel> continueWithEmail(String email, String password) async {
    User user;

    try {
      final credential = await _auth.createUserWithEmailAndPassword(
        email: email,
        password: password,
      );
      user = credential.user!;
    } on FirebaseAuthException catch (e) {
      if (e.code != 'email-already-in-use') rethrow;

      final credential = await _auth.signInWithEmailAndPassword(
        email: email,
        password: password,
      );
      user = credential.user!;
    }

    return _ensureUserDocument(user);
  }

  Future<UsersModel> _ensureUserDocument(User user) async {
    final userDoc = FirestorePaths.userDocument(user.uid);
    final snapshot = await userDoc.get();

    if (snapshot.exists) {
      return UsersModel.fromJson(snapshot.data()!);
    }

    final usersModel = UsersModel(
      id: user.uid,
      name: user.displayName ?? _nameFromEmail(user.email) ?? '',
      email: user.email ?? '',
      image: user.photoURL ?? '',
      fcmToken: '',
      notifications: true,
    );

    await userDoc.set(usersModel.toJson());
    return usersModel;
  }

  /// Falls back to the part before '@' as a starter name for new
  /// email/password accounts, since there's no name field to ask for one.
  String? _nameFromEmail(String? email) {
    if (email == null || !email.contains('@')) return null;
    return email.split('@').first;
  }

  Future<void> signOut() => _auth.signOut();
}
