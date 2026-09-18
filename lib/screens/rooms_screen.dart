import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../models/rooms_model.dart';
import '../models/users_model.dart';
import '../providers/auth_providers.dart';
import '../providers/rooms_providers.dart';
import '../services/firestore_paths.dart';

/// Rooms the current user belongs to. Equivalent to RoomsActivity +
/// RoomsRecyclerAdapter.
class RoomsScreen extends ConsumerWidget {
  const RoomsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final roomsAsync = ref.watch(myRoomsProvider);
    final currentUserId = ref.watch(authStateChangesProvider).value?.uid;

    return Scaffold(
      appBar: AppBar(title: const Text('Rooms')),
      body: roomsAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stack) => const Center(child: Text('Error loading rooms')),
        data: (rooms) {
          if (rooms.isEmpty) {
            return const Center(child: Text('No rooms yet'));
          }

          return ListView.builder(
            itemCount: rooms.length,
            itemBuilder: (context, index) {
              final RoomsModel room = rooms[index];
              return _RoomTile(room: room, currentUserId: currentUserId);
            },
          );
        },
      ),
    );
  }
}

class _RoomTile extends StatelessWidget {
  const _RoomTile({required this.room, required this.currentUserId});

  final RoomsModel room;
  final String? currentUserId;

  @override
  Widget build(BuildContext context) {
    // Some rooms may have a missing/blank created_by (bad or legacy data) -
    // a blank id would otherwise crash FirestorePaths.userDocument(), which
    // rejects empty document paths.
    final hasCreator = room.createdBy.isNotEmpty;

    return FutureBuilder(
      future: hasCreator
          ? FirestorePaths.userDocument(room.createdBy).get()
          : null,
      builder: (context, snapshot) {
        final data = snapshot.data?.data();
        final creator = data != null ? UsersModel.fromJson(data) : null;

        final isMe = hasCreator && room.createdBy == currentUserId;
        final label = isMe
            ? 'You created a room'
            : '${creator?.name ?? 'Someone'} added you to a room';

        return ListTile(
          leading: CircleAvatar(
            backgroundImage: (creator?.image.isNotEmpty ?? false)
                ? CachedNetworkImageProvider(creator!.image)
                : null,
            child: (creator?.image.isEmpty ?? true)
                ? const Icon(Icons.person)
                : null,
          ),
          title: Text(label),
          onTap: () =>
              context.push('/game/${room.timestampMillis}'),
        );
      },
    );
  }
}
