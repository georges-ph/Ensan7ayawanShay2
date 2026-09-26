import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:google_sign_in/google_sign_in.dart';

import '../providers/firebase_providers.dart';
import '../widgets/gradient_app_bar.dart';
import '../widgets/gradient_button.dart';

/// Single email/password entry point (creates an account if the email is
/// new, signs in if it's already registered) plus Google sign in.
class SignInScreen extends ConsumerStatefulWidget {
  const SignInScreen({super.key});

  @override
  ConsumerState<SignInScreen> createState() => _SignInScreenState();
}

class _SignInScreenState extends ConsumerState<SignInScreen> {
  final _formKey = GlobalKey<FormState>();
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();

  bool _busy = false;
  bool _obscurePassword = true;
  String? _error;

  @override
  void dispose() {
    _emailController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  Future<void> _run(Future<void> Function() action) async {
    setState(() {
      _busy = true;
      _error = null;
    });

    try {
      await action();
      if (mounted) context.pushReplacement('/start');
    } on FirebaseAuthException catch (e) {
      if (mounted) setState(() => _error = _messageFor(e));
    } on GoogleSignInException catch (e) {
      // The user backing out of the native account picker isn't an
      // error worth showing - just quietly return to the form.
      if (e.code != GoogleSignInExceptionCode.canceled && mounted) {
        setState(() => _error = 'Sign in error, please try again');
      }
    } catch (_) {
      if (mounted) setState(() => _error = 'Sign in error, please try again');
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  String _messageFor(FirebaseAuthException e) {
    switch (e.code) {
      case 'invalid-email':
        return 'Invalid email address';
      case 'user-not-found':
      case 'wrong-password':
      case 'invalid-credential':
        return 'Wrong email or password';
      case 'weak-password':
        return 'Password must be at least 6 characters';
      case 'too-many-requests':
        return 'Too many attempts, try again later';
      case 'network-request-failed':
        return 'Check your internet connection';
      default:
        return 'Sign in error, please try again';
    }
  }

  void _submit() {
    if (!_formKey.currentState!.validate()) return;

    final email = _emailController.text.trim();
    final password = _passwordController.text;

    _run(
      () => ref.read(authServiceProvider).continueWithEmail(email, password),
    );
  }

  void _signInWithGoogle() {
    _run(() => ref.read(authServiceProvider).signInWithGoogle());
  }

  InputDecoration _decoration(String label, IconData icon) {
    return InputDecoration(labelText: label, prefixIcon: Icon(icon));
  }

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;

    return Scaffold(
      appBar: const GradientAppBar(title: 'Sign in'),
      body: Center(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24),
          child: ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 400),
            child: Container(
              padding: const EdgeInsets.all(24),
              decoration: BoxDecoration(
                color: scheme.surface,
                borderRadius: BorderRadius.circular(24),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withValues(alpha: 0.06),
                    blurRadius: 24,
                    offset: const Offset(0, 12),
                  ),
                ],
              ),
              child: Form(
                key: _formKey,
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Center(
                      child: Image.asset(
                        'assets/images/logo.png',
                        width: 88,
                      ),
                    ),
                    const SizedBox(height: 16),
                    Text(
                      'Let\'s play',
                      textAlign: TextAlign.center,
                      style: Theme.of(context).textTheme.titleLarge,
                    ),
                    const SizedBox(height: 20),
                    TextFormField(
                      controller: _emailController,
                      enabled: !_busy,
                      keyboardType: TextInputType.emailAddress,
                      textInputAction: TextInputAction.next,
                      autofillHints: const [AutofillHints.email],
                      decoration: _decoration('Email', Icons.email_outlined),
                      validator: (value) {
                        final text = (value ?? '').trim();
                        if (text.isEmpty) return 'Enter your email';
                        if (!text.contains('@')) {
                          return 'Invalid email address';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 12),
                    TextFormField(
                      controller: _passwordController,
                      enabled: !_busy,
                      obscureText: _obscurePassword,
                      textInputAction: TextInputAction.done,
                      autofillHints: const [AutofillHints.password],
                      onFieldSubmitted: (_) => _busy ? null : _submit(),
                      decoration: _decoration('Password', Icons.lock_outline)
                          .copyWith(
                            suffixIcon: IconButton(
                              icon: Icon(
                                _obscurePassword
                                    ? Icons.visibility_outlined
                                    : Icons.visibility_off_outlined,
                              ),
                              onPressed: () => setState(
                                () => _obscurePassword = !_obscurePassword,
                              ),
                            ),
                          ),
                      validator: (value) {
                        final text = value ?? '';
                        if (text.isEmpty) return 'Enter your password';
                        if (text.length < 6) {
                          return 'Password must be at least 6 characters';
                        }
                        return null;
                      },
                    ),
                    if (_error != null) ...[
                      const SizedBox(height: 12),
                      Text(
                        _error!,
                        textAlign: TextAlign.center,
                        style: TextStyle(color: scheme.error),
                      ),
                    ],
                    const SizedBox(height: 24),
                    if (_busy)
                      const Center(child: CircularProgressIndicator())
                    else ...[
                      GradientButton(label: 'Continue', onPressed: _submit),
                      const SizedBox(height: 16),
                      Row(
                        children: [
                          Expanded(child: Divider(color: scheme.outlineVariant)),
                          Padding(
                            padding: const EdgeInsets.symmetric(
                              horizontal: 10,
                            ),
                            child: Text(
                              'or',
                              style: TextStyle(
                                color: scheme.onSurface.withValues(
                                  alpha: 0.5,
                                ),
                              ),
                            ),
                          ),
                          Expanded(child: Divider(color: scheme.outlineVariant)),
                        ],
                      ),
                      const SizedBox(height: 16),
                      SizedBox(
                        width: double.infinity,
                        height: GradientButton.height,
                        child: OutlinedButton.icon(
                          onPressed: _signInWithGoogle,
                          icon: Image.asset(
                            'assets/images/google_logo.png',
                            width: 20,
                            height: 20,
                          ),
                          label: const Text(
                            'Continue with Google',
                            overflow: TextOverflow.ellipsis,
                            maxLines: 1,
                          ),
                          style: OutlinedButton.styleFrom(
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(
                                GradientButton.radius,
                              ),
                            ),
                          ),
                        ),
                      ),
                    ],
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
