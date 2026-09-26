import 'package:flutter/material.dart';

/// The app's palette: a deliberate departure from the original app's
/// plain Material colors, built for a fun, energetic party-game feel
/// (violet/coral brand gradient, warm amber accent for the drawn letter,
/// a distinct color per answer category).
class AppColors {
  AppColors._();

  // Brand
  static const violet = Color(0xFF6C4CF1);
  static const violetDeep = Color(0xFF4B2FD1);
  static const violetLight = Color(0xFF9B87FF);
  static const coral = Color(0xFFFF6B9E);
  static const amber = Color(0xFFFFB020);

  // Light theme surfaces
  static const lightBackground = Color(0xFFF7F4FF);
  static const lightSurface = Color(0xFFFFFFFF);
  static const lightSurfaceTint = Color(0xFFF0EBFF);

  // Dark theme surfaces
  static const darkBackground = Color(0xFF140F22);
  static const darkSurface = Color(0xFF1E1733);
  static const darkSurfaceTint = Color(0xFF271F42);

  // Category colors (Ensan / 7ayawan / Shay2), same in both themes -
  // chosen to stay legible against both light and dark surfaces.
  static const ensan = Color(0xFF3DA9FC);
  static const hayawan = Color(0xFF2ED573);
  static const shay2 = Color(0xFFFF9F43);

  static const success = Color(0xFF2ED573);
  static const error = Color(0xFFFF5A6E);
}

/// Reusable gradients for hero surfaces (app bars, primary buttons, the
/// letter badge) so the same brand feel shows up consistently everywhere.
class AppGradients {
  AppGradients._();

  static const primary = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [AppColors.violet, AppColors.coral],
  );

  static const warm = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [AppColors.amber, AppColors.coral],
  );

  static const cool = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [Color(0xFF3DA9FC), AppColors.violet],
  );

  // A darker take on [cool] for large hero surfaces (e.g. a full-width
  // card) - the same bright colors that read fine on a small 52px icon
  // badge are too glaring stretched across a whole card.
  static const coolDeep = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [Color(0xFF2F80ED), AppColors.violetDeep],
  );
}
