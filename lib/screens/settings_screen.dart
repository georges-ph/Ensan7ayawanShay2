import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../providers/auth_providers.dart';
import '../providers/firebase_providers.dart';
import '../providers/theme_provider.dart';
import '../services/firestore_paths.dart';
import '../widgets/gradient_app_bar.dart';
import '../widgets/user_avatar.dart';

/// Equivalent to SettingsActivity + SettingsFragment: name, theme and
/// notifications preferences, plus sign out.
class SettingsScreen extends ConsumerStatefulWidget {
  const SettingsScreen({super.key});

  @override
  ConsumerState<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends ConsumerState<SettingsScreen> {
  final _nameController = TextEditingController();
  bool _initialized = false;

  @override
  void dispose() {
    _nameController.dispose();
    super.dispose();
  }

  Future<void> _saveName(String userId, String name) async {
    await FirestorePaths.userDocument(userId).update({'name': name});
  }

  Future<void> _saveNotifications(String userId, bool value) async {
    await FirestorePaths.userDocument(userId).update({
      'notifications': value,
    });
  }

  @override
  Widget build(BuildContext context) {
    final userAsync = ref.watch(currentUserModelProvider);
    final themeMode = ref.watch(themeModeProvider);
    final userId = ref.watch(authStateChangesProvider).value?.uid;
    final scheme = Theme.of(context).colorScheme;

    return Scaffold(
      appBar: const GradientAppBar(title: 'Settings'),
      body: userAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stack) => Center(child: Text('$error')),
        data: (user) {
          if (user != null && !_initialized) {
            _nameController.text = user.name;
            _initialized = true;
          }

          return ListView(
            padding: const EdgeInsets.all(16),
            children: [
              if (user != null)
                _SettingsCard(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      UserAvatar(imageUrl: user.image, radius: 32),
                      const SizedBox(height: 16),
                      TextField(
                        controller: _nameController,
                        textAlign: TextAlign.center,
                        decoration: const InputDecoration(
                          labelText: 'Your name',
                        ),
                        onSubmitted: (value) => _saveName(userId!, value),
                        onEditingComplete: () =>
                            _saveName(userId!, _nameController.text),
                      ),
                    ],
                  ),
                ),
              const SizedBox(height: 16),
              _SettingsCard(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Theme',
                      style: Theme.of(context).textTheme.titleMedium,
                    ),
                    const SizedBox(height: 12),
                    SegmentedButton<ThemeMode>(
                      segments: const [
                        ButtonSegment(
                          value: ThemeMode.light,
                          icon: Icon(Icons.light_mode_rounded),
                          label: Text('Light'),
                        ),
                        ButtonSegment(
                          value: ThemeMode.dark,
                          icon: Icon(Icons.dark_mode_rounded),
                          label: Text('Dark'),
                        ),
                        ButtonSegment(
                          value: ThemeMode.system,
                          icon: Icon(Icons.settings_suggest_rounded),
                          label: Text('Auto'),
                        ),
                      ],
                      selected: {themeMode},
                      onSelectionChanged: (selection) {
                        ref
                            .read(themeModeProvider.notifier)
                            .setThemeMode(selection.first);
                      },
                    ),
                    if (user != null) ...[
                      const Divider(height: 32),
                      SwitchListTile(
                        contentPadding: EdgeInsets.zero,
                        title: const Text('Notifications'),
                        value: user.notifications,
                        onChanged: (value) =>
                            _saveNotifications(userId!, value),
                      ),
                    ],
                  ],
                ),
              ),
              const SizedBox(height: 16),
              _SettingsCard(
                child: ListTile(
                  contentPadding: EdgeInsets.zero,
                  leading: Icon(Icons.logout_rounded, color: scheme.error),
                  title: Text(
                    'Sign out',
                    style: TextStyle(
                      color: scheme.error,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  onTap: () => ref.read(authServiceProvider).signOut(),
                ),
              ),
            ],
          );
        },
      ),
    );
  }
}

class _SettingsCard extends StatelessWidget {
  const _SettingsCard({required this.child});

  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surface,
        borderRadius: BorderRadius.circular(20),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.04),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: child,
    );
  }
}
