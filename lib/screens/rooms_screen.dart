import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:google_fonts/google_fonts.dart';

import '../models/rooms_model.dart';
import '../models/users_model.dart';
import '../providers/auth_providers.dart';
import '../providers/firebase_providers.dart';
import '../providers/rooms_providers.dart';
import '../providers/users_providers.dart';
import '../theme/app_colors.dart';
import '../widgets/gradient_app_bar.dart';
import '../widgets/user_avatar.dart';

/// Join a room by its share code, or pick one you already belong to.
/// Equivalent to RoomsActivity + RoomsRecyclerAdapter, plus the new
/// code-entry flow that replaced the all-users invite picker.
class RoomsScreen extends ConsumerStatefulWidget {
  const RoomsScreen({super.key});

  @override
  ConsumerState<RoomsScreen> createState() => _RoomsScreenState();
}

class _RoomsScreenState extends ConsumerState<RoomsScreen> {
  final _codeController = TextEditingController();
  bool _joining = false;
  String? _error;

  @override
  void dispose() {
    _codeController.dispose();
    super.dispose();
  }

  Future<void> _joinByCode() async {
    final userId = ref.read(authStateChangesProvider).value?.uid;
    final code = _codeController.text.trim();
    if (userId == null || code.isEmpty) return;

    setState(() {
      _joining = true;
      _error = null;
    });

    try {
      final gameService = ref.read(gameServiceProvider);
      final roomId = await gameService.findRoomIdByCode(code);

      if (roomId == null) {
        if (mounted) {
          setState(() => _error = "That code doesn't match a room");
        }
        return;
      }

      await gameService.joinRoom(roomId, userId);
      if (mounted) context.push('/game/$roomId');
    } catch (_) {
      if (mounted) setState(() => _error = 'Could not join, please try again');
    } finally {
      if (mounted) setState(() => _joining = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final roomsAsync = ref.watch(myRoomsProvider);
    final currentUserId = ref.watch(authStateChangesProvider).value?.uid;
    final usersById = ref.watch(usersByIdProvider).valueOrNull ?? const {};
    final scheme = Theme.of(context).colorScheme;

    return Scaffold(
      appBar: const GradientAppBar(title: 'Join room'),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Container(
            padding: const EdgeInsets.all(20),
            decoration: BoxDecoration(
              gradient: AppGradients.coolDeep,
              borderRadius: BorderRadius.circular(24),
              boxShadow: [
                BoxShadow(
                  color: AppColors.violet.withValues(alpha: 0.3),
                  blurRadius: 20,
                  offset: const Offset(0, 10),
                ),
              ],
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Got a code?',
                  style: GoogleFonts.fredoka(
                    color: Colors.white,
                    fontSize: 18,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  "Enter it below to jump into a friend's room",
                  style: TextStyle(
                    color: Colors.white.withValues(alpha: 0.85),
                    fontSize: 12,
                  ),
                ),
                const SizedBox(height: 16),
                TextField(
                  controller: _codeController,
                  enabled: !_joining,
                  textCapitalization: TextCapitalization.characters,
                  textAlign: TextAlign.center,
                  onSubmitted: (_) => _joining ? null : _joinByCode(),
                  inputFormatters: [
                    FilteringTextInputFormatter.deny(RegExp(r'\s')),
                  ],
                  style: GoogleFonts.fredoka(
                    fontSize: 20,
                    fontWeight: FontWeight.w600,
                    letterSpacing: 6,
                    color: AppColors.violetDeep,
                  ),
                  decoration: InputDecoration(
                    hintText: 'CODE',
                    // The fill is pinned to white (for contrast against
                    // the gradient card) regardless of theme, so the hint
                    // must be too - it would otherwise inherit the app's
                    // dark-mode hint color, which is near-white on white.
                    hintStyle: TextStyle(
                      color: AppColors.violetDeep.withValues(alpha: 0.35),
                      letterSpacing: 6,
                    ),
                    filled: true,
                    fillColor: Colors.white,
                    errorText: _error,
                    errorStyle: const TextStyle(color: Colors.yellowAccent),
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(16),
                      borderSide: BorderSide.none,
                    ),
                    contentPadding: const EdgeInsets.symmetric(vertical: 14),
                  ),
                ),
                const SizedBox(height: 12),
                SizedBox(
                  width: double.infinity,
                  child: FilledButton(
                    onPressed: _joining ? null : _joinByCode,
                    style: FilledButton.styleFrom(
                      backgroundColor: Colors.white,
                      foregroundColor: AppColors.violetDeep,
                      padding: const EdgeInsets.symmetric(vertical: 14),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(14),
                      ),
                    ),
                    child: _joining
                        ? const SizedBox(
                            width: 20,
                            height: 20,
                            child: CircularProgressIndicator(
                              strokeWidth: 2,
                              color: AppColors.violetDeep,
                            ),
                          )
                        : const Text('Join room'),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 24),
          Text(
            'Your rooms',
            style: TextStyle(
              color: scheme.onSurface.withValues(alpha: 0.6),
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 8),
          roomsAsync.when(
            loading: () => const Padding(
              padding: EdgeInsets.only(top: 24),
              child: Center(child: CircularProgressIndicator()),
            ),
            error: (error, stack) => const Padding(
              padding: EdgeInsets.only(top: 24),
              child: Center(child: Text('Error loading rooms')),
            ),
            data: (rooms) {
              if (rooms.isEmpty) {
                return const Padding(
                  padding: EdgeInsets.only(top: 24),
                  child: Center(child: Text('No rooms yet')),
                );
              }

              return Column(
                children: rooms
                    .map(
                      (room) => Padding(
                        padding: const EdgeInsets.only(bottom: 10),
                        child: _RoomTile(
                          room: room,
                          currentUserId: currentUserId,
                          creator: usersById[room.createdBy],
                        ),
                      ),
                    )
                    .toList(),
              );
            },
          ),
        ],
      ),
    );
  }
}

class _RoomTile extends StatelessWidget {
  const _RoomTile({
    required this.room,
    required this.currentUserId,
    required this.creator,
  });

  final RoomsModel room;
  final String? currentUserId;
  final UsersModel? creator;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    final isMe = room.createdBy.isNotEmpty && room.createdBy == currentUserId;
    final label = isMe
        ? 'You created a room'
        : '${creator?.name ?? 'Someone'} added you to a room';

    return Material(
      color: scheme.surface,
      borderRadius: BorderRadius.circular(18),
      child: InkWell(
        borderRadius: BorderRadius.circular(18),
        onTap: () => context.push('/game/${room.timestampMillis}'),
        child: Container(
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(18),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withValues(alpha: 0.04),
                blurRadius: 10,
                offset: const Offset(0, 4),
              ),
            ],
          ),
          child: Row(
            children: [
              UserAvatar(imageUrl: creator?.image),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      label,
                      style: const TextStyle(fontWeight: FontWeight.w600),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      _formatRoomTime(room.timestampMillis),
                      style: TextStyle(
                        fontSize: 12,
                        color: scheme.onSurface.withValues(alpha: 0.5),
                      ),
                    ),
                  ],
                ),
              ),
              Icon(
                Icons.chevron_right_rounded,
                color: scheme.onSurface.withValues(alpha: 0.3),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

const _months = [
  'Jan',
  'Feb',
  'Mar',
  'Apr',
  'May',
  'Jun',
  'Jul',
  'Aug',
  'Sep',
  'Oct',
  'Nov',
  'Dec',
];

/// A short, human-friendly time for when a room was created: relative for
/// anything recent, a plain date once it's old enough that "3d ago" stops
/// being useful.
String _formatRoomTime(int millis) {
  final date = DateTime.fromMillisecondsSinceEpoch(millis);
  final now = DateTime.now();
  final diff = now.difference(date);

  if (diff.inSeconds < 60) return 'Just now';
  if (diff.inMinutes < 60) return '${diff.inMinutes}m ago';

  final isToday =
      date.year == now.year && date.month == now.month && date.day == now.day;
  if (isToday) return '${diff.inHours}h ago';

  final yesterday = now.subtract(const Duration(days: 1));
  final isYesterday =
      date.year == yesterday.year &&
      date.month == yesterday.month &&
      date.day == yesterday.day;
  if (isYesterday) return 'Yesterday';

  if (diff.inDays < 7) return '${diff.inDays}d ago';

  final monthDay = '${_months[date.month - 1]} ${date.day}';
  return date.year == now.year ? monthDay : '$monthDay, ${date.year}';
}
