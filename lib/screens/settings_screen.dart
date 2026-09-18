import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../providers/auth_providers.dart';
import '../providers/firebase_providers.dart';
import '../providers/theme_provider.dart';
import '../services/firestore_paths.dart';

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

    return Scaffold(
      appBar: AppBar(title: const Text('Settings')),
      body: userAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stack) => Center(child: Text('$error')),
        data: (user) {
          if (user != null && !_initialized) {
            _nameController.text = user.name;
            _initialized = true;
          }

          return ListView(
            children: [
              if (user != null)
                Padding(
                  padding: const EdgeInsets.all(16),
                  child: TextField(
                    controller: _nameController,
                    decoration: const InputDecoration(labelText: 'Your name'),
                    onSubmitted: (value) => _saveName(userId!, value),
                    onEditingComplete: () =>
                        _saveName(userId!, _nameController.text),
                  ),
                ),
              ListTile(
                title: const Text('Theme'),
                trailing: DropdownButton<ThemeMode>(
                  value: themeMode,
                  items: const [
                    DropdownMenuItem(
                      value: ThemeMode.light,
                      child: Text('Light'),
                    ),
                    DropdownMenuItem(
                      value: ThemeMode.dark,
                      child: Text('Dark'),
                    ),
                    DropdownMenuItem(
                      value: ThemeMode.system,
                      child: Text('System'),
                    ),
                  ],
                  onChanged: (mode) {
                    if (mode != null) {
                      ref.read(themeModeProvider.notifier).setThemeMode(mode);
                    }
                  },
                ),
              ),
              if (user != null)
                SwitchListTile(
                  title: const Text('Notifications'),
                  value: user.notifications,
                  onChanged: (value) => _saveNotifications(userId!, value),
                ),
              const Divider(),
              ListTile(
                leading: const Icon(Icons.logout),
                title: const Text('Sign out'),
                onTap: () => ref.read(authServiceProvider).signOut(),
              ),
            ],
          );
        },
      ),
    );
  }
}
