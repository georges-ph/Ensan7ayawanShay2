import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

import '../theme/app_colors.dart';

/// The app's signature header: brand-gradient background with a rounded
/// bottom edge, used on every screen in place of a flat Material app bar.
class GradientAppBar extends StatelessWidget implements PreferredSizeWidget {
  const GradientAppBar({
    super.key,
    required this.title,
    this.actions,
    this.gradient = AppGradients.primary,
  });

  final String title;
  final List<Widget>? actions;
  final LinearGradient gradient;

  @override
  Size get preferredSize => const Size.fromHeight(kToolbarHeight);

  @override
  Widget build(BuildContext context) {
    return AppBar(
      title: Text(title),
      titleTextStyle: GoogleFonts.fredoka(
        color: Colors.white,
        fontSize: 22,
        fontWeight: FontWeight.w600,
      ),
      actions: actions,
      iconTheme: const IconThemeData(color: Colors.white),
      actionsIconTheme: const IconThemeData(color: Colors.white),
      backgroundColor: Colors.transparent,
      elevation: 0,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(bottom: Radius.circular(28)),
      ),
      flexibleSpace: Container(decoration: BoxDecoration(gradient: gradient)),
    );
  }
}
