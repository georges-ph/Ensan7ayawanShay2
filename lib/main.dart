import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:google_sign_in/google_sign_in.dart';

import 'app.dart';
import 'firebase_options_dev.dart' as dev;
import 'firebase_options_prod.dart' as prod;

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Debug/profile builds (local `flutter run`) talk to the dev project
  // (project-1-b5db1); release builds talk to production (jundbits-27eab,
  // under the separate jundbits@gmail.com account) - there's no build
  // flavor set up to split this at the native Android level, so this one
  // switch is what keeps everyday testing from touching production data.
  final options = kReleaseMode
      ? prod.DefaultFirebaseOptions.currentPlatform
      : dev.DefaultFirebaseOptions.currentPlatform;

  await Firebase.initializeApp(options: options);

  if (!kIsWeb) {
    // No serverClientId needed here: it's read automatically from
    // whichever google-services.json was baked into this build (dev or
    // prod, picked by android/app/build.gradle.kts), which always matches
    // the FirebaseOptions selected above.
    await GoogleSignIn.instance.initialize();
  }

  runApp(const ProviderScope(child: App()));
}
