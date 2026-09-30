import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../models/users_model.dart';
import '../services/firestore_paths.dart';

/// Builds the canonical family key for [usersByIdProvider] from a list of
/// ids - a plain sorted, de-duplicated, comma-joined string so repeated
/// calls with the same ids (regardless of list identity/order) reuse the
/// same cached fetch instead of re-querying every rebuild.
String usersByIdKey(Iterable<String> ids) {
  final unique = ids.where((id) => id.isNotEmpty).toSet().toList()..sort();
  return unique.join(',');
}

/// Resolves just the given users' profiles by id (e.g. a visible list of
/// rooms' creators), instead of listing the entire Users collection - a
/// screen only ever needs to know about a handful of specific people, and
/// this keeps the Firestore rules from having to allow anyone to dump the
/// whole user directory (names, emails, photos) just to support that.
final usersByIdProvider =
    FutureProvider.family<Map<String, UsersModel>, String>((ref, idsKey) async {
      final ids = idsKey.isEmpty ? const <String>[] : idsKey.split(',');
      if (ids.isEmpty) return {};

      final snapshots = await Future.wait(
        ids.map((id) => FirestorePaths.userDocument(id).get()),
      );

      return {
        for (final snapshot in snapshots)
          if (snapshot.exists)
            snapshot.id: UsersModel.fromJson(snapshot.data()!),
      };
    });
