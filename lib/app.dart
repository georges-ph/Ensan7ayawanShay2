import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'providers/auth_providers.dart';
import 'providers/firebase_providers.dart';
import 'providers/theme_provider.dart';
import 'router/app_router.dart';
import 'theme/app_colors.dart';

class App extends ConsumerStatefulWidget {
  const App({super.key});

  @override
  ConsumerState<App> createState() => _AppState();
}

class _AppState extends ConsumerState<App> with WidgetsBindingObserver {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    final userId = ref.read(authStateChangesProvider).value?.uid;
    if (userId == null) return;

    final presence = ref.read(presenceServiceProvider);
    if (state == AppLifecycleState.resumed) {
      presence.setOnline(userId);
    } else if (state == AppLifecycleState.paused ||
        state == AppLifecycleState.detached) {
      presence.setOffline(userId);
    }
  }

  @override
  Widget build(BuildContext context) {
    ref.listen(authStateChangesProvider, (previous, next) {
      final userId = next.value?.uid;
      if (userId != null) {
        ref.read(presenceServiceProvider).setOnline(userId);
      }
    });

    final themeMode = ref.watch(themeModeProvider);

    return MaterialApp.router(
      title: 'Ensan 7ayawan Shay2',
      themeMode: themeMode,
      theme: _buildTheme(_lightColorScheme),
      darkTheme: _buildTheme(_darkColorScheme),
      routerConfig: appRouter,
    );
  }
}

// Mirrors themes.xml: colorPrimary=dark_sienna_2, colorOnPrimary=white,
// colorSecondary=twilight_lavender_1, background/surface=white, text=black.
const _lightColorScheme = ColorScheme.light(
  primary: AppColors.darkSienna2,
  onPrimary: Colors.white,
  primaryContainer: AppColors.darkSienna3,
  secondary: AppColors.twilightLavender1,
  onSecondary: Colors.black,
  secondaryContainer: AppColors.twilightLavender3,
  surface: Colors.white,
  onSurface: Colors.black,
);

// Mirrors values-night/themes.xml: colorPrimary=dark_sienna_1,
// colorOnPrimary=black, background/surface=dark_black, text=white.
const _darkColorScheme = ColorScheme.dark(
  primary: AppColors.darkSienna1,
  onPrimary: Colors.black,
  primaryContainer: AppColors.darkSienna3,
  secondary: AppColors.twilightLavender1,
  onSecondary: Colors.black,
  secondaryContainer: AppColors.twilightLavender1,
  surface: AppColors.darkBlack,
  onSurface: Colors.white,
);

ThemeData _buildTheme(ColorScheme scheme) {
  return ThemeData(
    colorScheme: scheme,
    scaffoldBackgroundColor: scheme.surface,
    appBarTheme: AppBarTheme(
      backgroundColor: scheme.primary,
      foregroundColor: scheme.onPrimary,
    ),
    useMaterial3: true,
  );
}
