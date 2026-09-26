import 'package:go_router/go_router.dart';

import '../screens/game_room_screen.dart';
import '../screens/home_screen.dart';
import '../screens/rooms_screen.dart';
import '../screens/settings_screen.dart';
import '../screens/sign_in_screen.dart';
import '../screens/start_screen.dart';

final appRouter = GoRouter(
  routes: [
    GoRoute(path: '/', builder: (context, state) => const HomeScreen()),
    GoRoute(
      path: '/sign-in',
      builder: (context, state) => const SignInScreen(),
    ),
    GoRoute(path: '/start', builder: (context, state) => const StartScreen()),
    GoRoute(
      path: '/rooms',
      builder: (context, state) => const RoomsScreen(),
    ),
    GoRoute(
      path: '/game/:roomId',
      builder: (context, state) =>
          GameRoomScreen(roomId: state.pathParameters['roomId']!),
    ),
    GoRoute(
      path: '/settings',
      builder: (context, state) => const SettingsScreen(),
    ),
  ],
);
