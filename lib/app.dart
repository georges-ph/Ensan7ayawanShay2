import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:google_fonts/google_fonts.dart';

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

class _AppState extends ConsumerState<App> {
  String? _trackedUserId;

  @override
  Widget build(BuildContext context) {
    // Tracks presence for as long as someone's signed in, self-healing on
    // every reconnect (see PresenceService) instead of reacting to app
    // lifecycle events, which don't map reliably to connection state on
    // web (e.g. switching browser tabs).
    ref.listen(authStateChangesProvider, (previous, next) {
      final userId = next.value?.uid;
      final presence = ref.read(presenceServiceProvider);

      if (userId != null && userId != _trackedUserId) {
        _trackedUserId = userId;
        presence.startTracking(userId);
      } else if (userId == null && _trackedUserId != null) {
        presence.stopTracking(_trackedUserId!);
        _trackedUserId = null;
      }
    });

    final themeMode = ref.watch(themeModeProvider);

    return MaterialApp.router(
      title: 'Ensan 7ayawan Shay2',
      debugShowCheckedModeBanner: false,
      themeMode: themeMode,
      theme: _buildTheme(_lightColorScheme),
      darkTheme: _buildTheme(_darkColorScheme),
      routerConfig: appRouter,
    );
  }
}

const _lightColorScheme = ColorScheme.light(
  primary: AppColors.violet,
  onPrimary: Colors.white,
  primaryContainer: AppColors.lightSurfaceTint,
  onPrimaryContainer: AppColors.violetDeep,
  secondary: AppColors.coral,
  onSecondary: Colors.white,
  secondaryContainer: Color(0xFFFFE1EC),
  onSecondaryContainer: AppColors.coral,
  tertiary: AppColors.amber,
  error: AppColors.error,
  surface: AppColors.lightSurface,
  onSurface: Color(0xFF241D3D),
  surfaceContainerHighest: AppColors.lightSurfaceTint,
);

const _darkColorScheme = ColorScheme.dark(
  primary: AppColors.violetLight,
  onPrimary: Color(0xFF1B1333),
  primaryContainer: AppColors.darkSurfaceTint,
  onPrimaryContainer: AppColors.violetLight,
  secondary: AppColors.coral,
  onSecondary: Color(0xFF1B1333),
  secondaryContainer: Color(0xFF3A2337),
  onSecondaryContainer: AppColors.coral,
  tertiary: AppColors.amber,
  error: AppColors.error,
  surface: AppColors.darkSurface,
  onSurface: Color(0xFFF1ECFF),
  surfaceContainerHighest: AppColors.darkSurfaceTint,
);

ThemeData _buildTheme(ColorScheme scheme) {
  // GoogleFonts.nunitoTextTheme() with no base defaults to a light/black
  // text theme regardless of which scheme is being built - fine for the
  // few roles overridden below, but every other role (including what
  // TextField uses for input/hint text) stayed black-on-black in dark
  // mode. Basing it on the real brightness and forcing every role's color
  // from scheme.onSurface fixes that everywhere at once, without
  // hardcoding a fixed color that would break the other theme instead.
  final base = ThemeData(brightness: scheme.brightness).textTheme;
  final textTheme = GoogleFonts.nunitoTextTheme(base)
      .apply(bodyColor: scheme.onSurface, displayColor: scheme.onSurface)
      .copyWith(
        displayLarge: GoogleFonts.fredoka(
          fontSize: 40,
          fontWeight: FontWeight.w600,
          color: scheme.onSurface,
        ),
        headlineMedium: GoogleFonts.fredoka(
          fontSize: 28,
          fontWeight: FontWeight.w600,
          color: scheme.onSurface,
        ),
        titleLarge: GoogleFonts.fredoka(
          fontSize: 22,
          fontWeight: FontWeight.w600,
          color: scheme.onSurface,
        ),
        titleMedium: GoogleFonts.fredoka(
          fontSize: 18,
          fontWeight: FontWeight.w600,
          color: scheme.onSurface,
        ),
        labelLarge: GoogleFonts.nunito(
          fontWeight: FontWeight.w800,
          letterSpacing: 0.3,
          color: scheme.onSurface,
        ),
      );

  return ThemeData(
    colorScheme: scheme,
    scaffoldBackgroundColor: scheme.brightness == Brightness.light
        ? AppColors.lightBackground
        : AppColors.darkBackground,
    textTheme: textTheme,
    useMaterial3: true,
    appBarTheme: const AppBarTheme(
      backgroundColor: Colors.transparent,
      elevation: 0,
    ),
    cardTheme: CardThemeData(
      color: scheme.surface,
      elevation: 0,
      surfaceTintColor: Colors.transparent,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
    ),
    inputDecorationTheme: InputDecorationTheme(
      filled: true,
      fillColor: scheme.surfaceContainerHighest,
      labelStyle: TextStyle(color: scheme.onSurface.withValues(alpha: 0.7)),
      hintStyle: TextStyle(color: scheme.onSurface.withValues(alpha: 0.45)),
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(16),
        borderSide: BorderSide.none,
      ),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(16),
        borderSide: BorderSide.none,
      ),
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(16),
        borderSide: BorderSide(color: scheme.primary, width: 2),
      ),
      errorBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(16),
        borderSide: BorderSide(color: scheme.error, width: 1.5),
      ),
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
    ),
    filledButtonTheme: FilledButtonThemeData(
      style: FilledButton.styleFrom(
        backgroundColor: scheme.primary,
        foregroundColor: scheme.onPrimary,
        padding: const EdgeInsets.symmetric(vertical: 16, horizontal: 20),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(18)),
        textStyle: GoogleFonts.fredoka(
          fontSize: 16,
          fontWeight: FontWeight.w600,
        ),
      ),
    ),
    textButtonTheme: TextButtonThemeData(
      style: TextButton.styleFrom(
        foregroundColor: scheme.primary,
        textStyle: GoogleFonts.nunito(fontWeight: FontWeight.w800),
      ),
    ),
    switchTheme: SwitchThemeData(
      thumbColor: WidgetStateProperty.resolveWith(
        (states) =>
            states.contains(WidgetState.selected) ? scheme.primary : null,
      ),
      trackColor: WidgetStateProperty.resolveWith(
        (states) => states.contains(WidgetState.selected)
            ? scheme.primary.withValues(alpha: 0.5)
            : null,
      ),
    ),
    listTileTheme: ListTileThemeData(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
    ),
  );
}
