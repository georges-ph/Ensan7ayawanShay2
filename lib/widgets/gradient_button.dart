import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

import '../theme/app_colors.dart';

/// A pill-shaped, gradient-filled primary action button - the app's main
/// call-to-action style, used in place of a plain FilledButton wherever a
/// screen's single most important action lives.
class GradientButton extends StatelessWidget {
  const GradientButton({
    super.key,
    required this.label,
    required this.onPressed,
    this.icon,
    this.gradient = AppGradients.primary,
    this.loading = false,
  });

  final String label;
  final VoidCallback? onPressed;
  final IconData? icon;
  final LinearGradient gradient;
  final bool loading;

  /// Fixed height so this always matches a same-row/same-screen
  /// OutlinedButton exactly, regardless of icon/font metrics - relying on
  /// padding alone made two buttons with the same corner radius read as
  /// different shapes (one near-pill, one a plain rounded rect).
  static const double height = 56;
  static const double radius = 18;

  @override
  Widget build(BuildContext context) {
    final enabled = onPressed != null && !loading;
    final borderRadius = BorderRadius.circular(radius);

    return Opacity(
      opacity: enabled ? 1 : 0.55,
      // The shadow lives on this plain Container, separate from the
      // Material/InkWell below - painting it alongside the ink layer
      // could let it bleed out past the rounded corners as a faint
      // rectangle; splitting them keeps the shadow cleanly rounded.
      child: Container(
        height: height,
        decoration: BoxDecoration(
          borderRadius: borderRadius,
          boxShadow: enabled
              ? [
                  BoxShadow(
                    color: gradient.colors.first.withValues(alpha: 0.35),
                    blurRadius: 16,
                    offset: const Offset(0, 8),
                  ),
                ]
              : null,
        ),
        child: ClipRRect(
          borderRadius: borderRadius,
          child: Material(
            color: Colors.transparent,
            child: Ink(
              decoration: BoxDecoration(gradient: gradient),
              child: InkWell(
                onTap: enabled ? onPressed : null,
                child: Center(
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: loading
                        ? const [
                            SizedBox(
                              width: 20,
                              height: 20,
                              child: CircularProgressIndicator(
                                strokeWidth: 2,
                                color: Colors.white,
                              ),
                            ),
                          ]
                        : [
                            if (icon != null) ...[
                              Icon(icon, color: Colors.white, size: 20),
                              const SizedBox(width: 8),
                            ],
                            Text(
                              label,
                              style: GoogleFonts.fredoka(
                                color: Colors.white,
                                fontSize: 16,
                                fontWeight: FontWeight.w600,
                              ),
                            ),
                          ],
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
