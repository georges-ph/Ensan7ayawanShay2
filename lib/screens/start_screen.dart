import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:url_launcher/url_launcher.dart';

/// Equivalent to StartActivity: create/join room menu, reached after
/// signing in from HomeScreen.
class StartScreen extends StatelessWidget {
  const StartScreen({super.key});

  static const _feedbackUrl = 'http://bit.ly/3s5wvyI';

  Future<void> _sendFeedback() async {
    await launchUrl(Uri.parse(_feedbackUrl));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Ensan 7ayawan Shay2'),
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
          padding: const EdgeInsets.symmetric(horizontal: 80),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              SizedBox(
                width: double.infinity,
                child: FilledButton(
                  onPressed: () => context.push('/users'),
                  child: const Text('Create room'),
                ),
              ),
              const SizedBox(height: 8),
              SizedBox(
                width: double.infinity,
                child: FilledButton(
                  onPressed: () => context.push('/rooms'),
                  child: const Text('Join room'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
