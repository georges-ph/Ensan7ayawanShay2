import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../models/users_model.dart';
import '../providers/auth_providers.dart';
import '../providers/firebase_providers.dart';
import '../providers/users_providers.dart';

/// Lets the current user pick friends to invite, then creates a room.
/// Equivalent to UsersActivity + UsersRecyclerAdapter.
class UsersScreen extends ConsumerStatefulWidget {
  const UsersScreen({super.key});

  @override
  ConsumerState<UsersScreen> createState() => _UsersScreenState();
}

class _UsersScreenState extends ConsumerState<UsersScreen> {
  final Set<String> _invitedIds = {};
  bool _creating = false;

  Future<void> _createRoom(String currentUserId) async {
    setState(() => _creating = true);

    final roomId = ref
        .read(gameServiceProvider)
        .currentTimestampMillis()
        .toString();

    final playerIds = [currentUserId, ..._invitedIds];

    try {
      await ref.read(gameServiceProvider).createRoom(
            roomId: roomId,
            createdBy: currentUserId,
            playerIds: playerIds,
          );

      if (mounted) context.pushReplacement('/game/$roomId');
    } finally {
      if (mounted) setState(() => _creating = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final currentUserId = ref.watch(authStateChangesProvider).value?.uid;
    final usersAsync = ref.watch(allUsersProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Create room')),
      body: usersAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stack) => Center(child: Text('Error getting users')),
        data: (users) {
          final others =
              users.where((u) => u.id != currentUserId).toList();

          if (others.isEmpty) {
            return const Center(child: Text('No other users registered yet'));
          }

          return ListView.builder(
            itemCount: others.length,
            itemBuilder: (context, index) {
              final UsersModel user = others[index];
              final selected = _invitedIds.contains(user.id);

              return ListTile(
                selected: selected,
                selectedTileColor: Theme.of(
                  context,
                ).colorScheme.primaryContainer,
                leading: CircleAvatar(
                  backgroundImage: user.image.isNotEmpty
                      ? CachedNetworkImageProvider(user.image)
                      : null,
                  child: user.image.isEmpty ? const Icon(Icons.person) : null,
                ),
                title: Text(user.name),
                onTap: () {
                  setState(() {
                    if (selected) {
                      _invitedIds.remove(user.id);
                    } else {
                      _invitedIds.add(user.id);
                    }
                  });
                },
              );
            },
          );
        },
      ),
      floatingActionButton: currentUserId == null
          ? null
          : FloatingActionButton(
              backgroundColor: Theme.of(context).colorScheme.primary,
              foregroundColor: Theme.of(context).colorScheme.onPrimary,
              onPressed: _creating ? null : () => _createRoom(currentUserId),
              child: _creating
                  ? const SizedBox(
                      width: 20,
                      height: 20,
                      child: CircularProgressIndicator(
                        strokeWidth: 2,
                        color: Colors.white,
                      ),
                    )
                  : const Icon(Icons.check),
            ),
    );
  }
}
