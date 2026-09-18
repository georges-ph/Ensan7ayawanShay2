import 'package:cloud_firestore/cloud_firestore.dart';

/// Matches the original Android app's Firestore layout exactly, so the
/// rewrite reads/writes the same data as the app it replaces:
/// Ensan7ayawanShay2/AppCollections/{Users,Rooms}, Rooms/{roomId}/Entries.
class FirestorePaths {
  FirestorePaths._();

  static const String appName = 'Ensan7ayawanShay2';

  static DocumentReference<Map<String, dynamic>> appDocument() {
    return FirebaseFirestore.instance
        .collection(appName)
        .doc('AppCollections');
  }

  static CollectionReference<Map<String, dynamic>> usersCollection() {
    return appDocument().collection('Users');
  }

  static DocumentReference<Map<String, dynamic>> userDocument(String userId) {
    return usersCollection().doc(userId);
  }

  static CollectionReference<Map<String, dynamic>> roomsCollection() {
    return appDocument().collection('Rooms');
  }

  static DocumentReference<Map<String, dynamic>> roomDocument(String roomId) {
    return roomsCollection().doc(roomId);
  }

  static CollectionReference<Map<String, dynamic>> entriesCollection(
    String roomId,
  ) {
    return roomDocument(roomId).collection('Entries');
  }

  static DocumentReference<Map<String, dynamic>> entriesDocument(
    String roomId,
    String userId,
  ) {
    return entriesCollection(roomId).doc(userId);
  }
}
