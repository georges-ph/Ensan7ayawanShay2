import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:url_launcher/url_launcher.dart';

import '../providers/auth_providers.dart';
import '../providers/firebase_providers.dart';
import '../theme/app_colors.dart';
import '../widgets/gradient_app_bar.dart';

/// Equivalent to StartActivity: create/join room menu, reached after
/// signing in from HomeScreen. Creating a room no longer means picking
/// from every registered user - it drops you straight into the new room,
/// which shows a share code other players can join with.
class StartScreen extends ConsumerStatefulWidget {
  const StartScreen({super.key});

  @override
  ConsumerState<StartScreen> createState() => _StartScreenState();
}

class _StartScreenState extends ConsumerState<StartScreen> {
  static const _feedbackUrl = 'http://bit.ly/3s5wvyI';

  bool _creating = false;

  Future<void> _sendFeedback() async {
    await launchUrl(Uri.parse(_feedbackUrl));
  }

  Future<void> _createRoom() async {
    final userId = ref.read(authStateChangesProvider).value?.uid;
    if (userId == null) return;

    setState(() => _creating = true);

    try {
      final gameService = ref.read(gameServiceProvider);
      final roomId = gameService.currentTimestampMillis().toString();

      await gameService.createRoom(
        roomId: roomId,
        createdBy: userId,
        playerIds: [userId],
      );

      if (mounted) context.push('/game/$roomId');
    } finally {
      if (mounted) setState(() => _creating = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: GradientAppBar(
        title: 'Ensan 7ayawan Shay2',
        actions: [
          PopupMenuButton<void>(
            itemBuilder: (context) => [
              PopupMenuItem(
                onTap: _sendFeedback,
                child: const Text('Feedback'),
              ),
            ],
          ),
        ],
      ),
      body: Center(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'What are we playing?',
                style: Theme.of(context).textTheme.titleLarge,
              ),
              const SizedBox(height: 16),
              _ActionCard(
                title: 'Create room',
                subtitle: 'Start a new game and share the code',
                icon: Icons.add_circle_rounded,
                gradient: AppGradients.primary,
                loading: _creating,
                onTap: _creating ? null : _createRoom,
              ),
              const SizedBox(height: 12),
              _ActionCard(
                title: 'Join room',
                subtitle: 'Enter a friend\'s room code',
                icon: Icons.meeting_room_rounded,
                gradient: AppGradients.cool,
                onTap: () => context.push('/rooms'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _ActionCard extends StatelessWidget {
  const _ActionCard({
    required this.title,
    required this.subtitle,
    required this.icon,
    required this.gradient,
    required this.onTap,
    this.loading = false,
  });

  final String title;
  final String subtitle;
  final IconData icon;
  final LinearGradient gradient;
  final VoidCallback? onTap;
  final bool loading;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;

    return Material(
      color: scheme.surface,
      borderRadius: BorderRadius.circular(22),
      child: InkWell(
        borderRadius: BorderRadius.circular(22),
        onTap: onTap,
        child: Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(22),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withValues(alpha: 0.05),
                blurRadius: 14,
                offset: const Offset(0, 6),
              ),
            ],
          ),
          child: Row(
            children: [
              Container(
                width: 52,
                height: 52,
                decoration: BoxDecoration(
                  gradient: gradient,
                  borderRadius: BorderRadius.circular(16),
                ),
                alignment: Alignment.center,
                child: loading
                    ? const SizedBox(
                        width: 22,
                        height: 22,
                        child: CircularProgressIndicator(
                          strokeWidth: 2,
                          color: Colors.white,
                        ),
                      )
                    : Icon(icon, color: Colors.white, size: 26),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      title,
                      style: GoogleFonts.fredoka(
                        fontSize: 18,
                        fontWeight: FontWeight.w600,
                        color: scheme.onSurface,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      subtitle,
                      style: TextStyle(
                        fontSize: 12,
                        color: scheme.onSurface.withValues(alpha: 0.6),
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
