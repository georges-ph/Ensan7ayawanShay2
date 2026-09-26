import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';

/// A profile-picture avatar that always falls back to a plain person icon
/// -- on an empty url, and also on a load failure (broken/expired link,
/// network error), instead of showing Flutter's raw red error box. Framed
/// with a soft brand-tinted ring to match the rest of the app's look.
class UserAvatar extends StatelessWidget {
  const UserAvatar({super.key, this.imageUrl, this.radius});

  final String? imageUrl;
  final double? radius;

  @override
  Widget build(BuildContext context) {
    final url = imageUrl;
    final r = radius ?? 20;
    final scheme = Theme.of(context).colorScheme;

    final fallback = CircleAvatar(
      radius: r,
      backgroundColor: scheme.primaryContainer,
      child: Icon(Icons.person_rounded, color: scheme.onPrimaryContainer),
    );

    return Container(
      padding: const EdgeInsets.all(2),
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        border: Border.all(
          color: scheme.primary.withValues(alpha: 0.25),
          width: 1.5,
        ),
      ),
      child: (url == null || url.isEmpty)
          ? fallback
          : ClipOval(
              child: CachedNetworkImage(
                imageUrl: url,
                width: r * 2,
                height: r * 2,
                fit: BoxFit.cover,
                placeholder: (context, url) => fallback,
                errorWidget: (context, url, error) => fallback,
              ),
            ),
    );
  }
}
