import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:google_fonts/google_fonts.dart';

import '../models/entries_model.dart';
import '../models/game_model.dart';
import '../providers/auth_providers.dart';
import '../providers/firebase_providers.dart';
import '../services/firestore_paths.dart';
import '../theme/app_colors.dart';
import '../widgets/gradient_app_bar.dart';
import '../widgets/gradient_button.dart';

/// The live game screen: letter draw, per-round answers, self-scoring and
/// a running scoreboard. Equivalent to GameRoomActivity plus its two
/// RecyclerView adapters.
class GameRoomScreen extends ConsumerStatefulWidget {
  const GameRoomScreen({super.key, required this.roomId});

  final String roomId;

  @override
  ConsumerState<GameRoomScreen> createState() => _GameRoomScreenState();
}

class _GameRoomScreenState extends ConsumerState<GameRoomScreen> {
  final _ensanController = TextEditingController();
  final _hayawanController = TextEditingController();
  final _shay2Controller = TextEditingController();
  final _scrollController = ScrollController();

  StreamSubscription<GameModel?>? _gameSub;
  StreamSubscription<QuerySnapshotEntries>? _entriesSub;
  Timer? _roundTicker;

  GameModel? _game;
  Map<String, String> _playerNames = {};
  Map<String, EntriesModel> _allEntries = {};

  int? _ensanSelected;
  int? _hayawanSelected;
  int? _shay2Selected;

  // Round-tracking keys so round-start/round-stop side effects run exactly
  // once per round instead of on every Firestore snapshot, unlike the
  // original activity (which re-ran them on every event).
  String? _appliedStartForLetter;
  String? _appliedStopForLetter;

  int get _totalScore =>
      (_ensanSelected ?? 0) + (_hayawanSelected ?? 0) + (_shay2Selected ?? 0);

  String get _userId => ref.read(authStateChangesProvider).value!.uid;

  @override
  void initState() {
    super.initState();
    _gameSub = ref
        .read(gameServiceProvider)
        .watchRoom(widget.roomId)
        .listen(_onGameUpdate);
  }

  @override
  void dispose() {
    _gameSub?.cancel();
    _entriesSub?.cancel();
    _roundTicker?.cancel();
    _ensanController.dispose();
    _hayawanController.dispose();
    _shay2Controller.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  void _scrollToTop() {
    if (!_scrollController.hasClients) return;
    _scrollController.animateTo(
      0,
      duration: const Duration(milliseconds: 400),
      curve: Curves.easeOut,
    );
  }

  Future<void> _onGameUpdate(GameModel? game) async {
    if (game == null) return;

    setState(() => _game = game);
    await _loadPlayerNames(game.players);

    if (game.started) {
      _entriesSub?.cancel();
      if (_appliedStartForLetter != game.letter) {
        _appliedStartForLetter = game.letter;
        _appliedStopForLetter = null;
        _scrollToTop();

        final scoreToCommit = _totalScore;
        _ensanController.clear();
        _hayawanController.clear();
        _shay2Controller.clear();

        final gameService = ref.read(gameServiceProvider);
        await gameService.addToOwnScore(widget.roomId, _userId, scoreToCommit);
        await gameService.clearOwnEntries(widget.roomId, _userId);

        setState(() {
          _ensanSelected = null;
          _hayawanSelected = null;
          _shay2Selected = null;
          _allEntries = {};
        });
      }

      // Ticks once a second just to trigger a rebuild so the live timer
      // (derived from game.roundStartedMillis, not local state) stays
      // current - every player's clock reads the same shared start time.
      _roundTicker ??= Timer.periodic(
        const Duration(seconds: 1),
        (_) => mounted ? setState(() {}) : null,
      );
    } else {
      _roundTicker?.cancel();
      _roundTicker = null;

      if (!game.firstStart && _appliedStopForLetter != game.letter) {
        _appliedStopForLetter = game.letter;
        _appliedStartForLetter = null;
        _scrollToTop();

        final typed = EntriesModel(
          ensan: _ensanController.text,
          hayawan: _hayawanController.text,
          shay2: _shay2Controller.text,
        );

        final gameService = ref.read(gameServiceProvider);
        await gameService.saveOwnEntries(widget.roomId, _userId, typed);

        if (typed.ensan.isEmpty &&
            typed.hayawan.isEmpty &&
            typed.shay2.isEmpty) {
          // Reopening an already-stopped room starts with empty
          // controllers (fresh widget state) even though this player
          // already has saved answers - pull them back in.
          final saved = await gameService.loadOwnEntries(
            widget.roomId,
            _userId,
          );
          if (mounted && saved != null) {
            _ensanController.text = saved.ensan;
            _hayawanController.text = saved.hayawan;
            _shay2Controller.text = saved.shay2;
          }
        }

        _watchEntries();
      }
    }
  }

  Future<void> _loadPlayerNames(List<String> playerIds) async {
    final missing = playerIds.where(
      (id) => id.isNotEmpty && !_playerNames.containsKey(id),
    );
    if (missing.isEmpty) return;

    final entries = await Future.wait(
      missing.map((id) async {
        final snapshot = await FirestorePaths.userDocument(id).get();
        final name = snapshot.data()?['name'] as String? ?? id;
        return MapEntry(id, name);
      }),
    );

    if (!mounted) return;
    setState(() {
      _playerNames = {..._playerNames, ...Map.fromEntries(entries)};
    });
  }

  void _watchEntries() {
    _entriesSub?.cancel();
    _entriesSub = FirestorePaths.entriesCollection(widget.roomId)
        .snapshots()
        .map(
          (snapshot) => QuerySnapshotEntries({
            for (final doc in snapshot.docs)
              doc.id: EntriesModel.fromJson(doc.data()),
          }),
        )
        .listen((wrapped) {
          if (!mounted) return;
          setState(() => _allEntries = wrapped.entries);
        });
  }

  Future<void> _startRound() async {
    await ref.read(gameServiceProvider).startRound(widget.roomId);
  }

  Future<void> _stopRound() async {
    await ref
        .read(gameServiceProvider)
        .stopRound(
          widget.roomId,
          roundStartedMillis: _game?.roundStartedMillis ?? 0,
        );
  }

  int _liveElapsedSeconds(GameModel game) {
    if (game.roundStartedMillis == 0) return 0;
    final elapsed =
        (DateTime.now().millisecondsSinceEpoch - game.roundStartedMillis) /
        1000;
    return elapsed < 0 ? 0 : elapsed.floor();
  }

  Future<void> _copyCode(String code) async {
    await Clipboard.setData(ClipboardData(text: code));
    if (!mounted) return;
    ScaffoldMessenger.of(
      context,
    ).showSnackBar(const SnackBar(content: Text('Room code copied')));
  }

  @override
  Widget build(BuildContext context) {
    final game = _game;

    return Scaffold(
      appBar: const GradientAppBar(title: 'Game Room'),
      body: game == null
          ? const Center(child: CircularProgressIndicator())
          : _buildBody(game),
    );
  }

  Widget _buildBody(GameModel game) {
    final stopped = !game.started && !game.firstStart;

    return ListView(
      controller: _scrollController,
      padding: const EdgeInsets.all(16),
      children: [
        Row(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            _LetterBadge(game: game, stopped: stopped),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    game.firstStart
                        ? 'Press start to draw a letter'
                        : stopped
                            ? 'Round stopped'
                            : "Go!",
                    style: Theme.of(context).textTheme.bodyMedium,
                  ),
                  const SizedBox(height: 8),
                  Wrap(
                    spacing: 8,
                    runSpacing: 8,
                    children: [
                      _ScorePill(score: _totalScore),
                      if (!game.firstStart) ...[
                        _RoundChip(
                          round: game.started
                              ? game.roundsPlayed + 1
                              : game.roundsPlayed,
                        ),
                        _TimerChip(
                          seconds: game.started
                              ? _liveElapsedSeconds(game)
                              : game.lastRoundSeconds,
                          live: game.started,
                        ),
                      ],
                      if (game.code.isNotEmpty)
                        _RoomCodeChip(
                          code: game.code,
                          onTap: () => _copyCode(game.code),
                        ),
                    ],
                  ),
                ],
              ),
            ),
          ],
        ),
        const SizedBox(height: 16),
        _PlayersRow(game: game, playerNames: _playerNames),
        const SizedBox(height: 16),
        _CategoryCard(
          label: 'Ensan',
          icon: Icons.person_rounded,
          color: AppColors.ensan,
          controller: _ensanController,
          enabled: game.started,
          showScoreButtons: stopped,
          selected: _ensanSelected,
          onSelected: (v) => setState(() => _ensanSelected = v),
          othersEntries: stopped
              ? _EntriesList(
                  entries: _allEntries,
                  playerNames: _playerNames,
                  currentUserId: _userId,
                  selector: (e) => e.ensan,
                )
              : null,
        ),
        const SizedBox(height: 14),
        _CategoryCard(
          label: '7ayawan',
          icon: Icons.pets_rounded,
          color: AppColors.hayawan,
          controller: _hayawanController,
          enabled: game.started,
          showScoreButtons: stopped,
          selected: _hayawanSelected,
          onSelected: (v) => setState(() => _hayawanSelected = v),
          othersEntries: stopped
              ? _EntriesList(
                  entries: _allEntries,
                  playerNames: _playerNames,
                  currentUserId: _userId,
                  selector: (e) => e.hayawan,
                )
              : null,
        ),
        const SizedBox(height: 14),
        _CategoryCard(
          label: 'Shay2',
          icon: Icons.category_rounded,
          color: AppColors.shay2,
          controller: _shay2Controller,
          enabled: game.started,
          showScoreButtons: stopped,
          selected: _shay2Selected,
          onSelected: (v) => setState(() => _shay2Selected = v),
          othersEntries: stopped
              ? _EntriesList(
                  entries: _allEntries,
                  playerNames: _playerNames,
                  currentUserId: _userId,
                  selector: (e) => e.shay2,
                )
              : null,
        ),
        const SizedBox(height: 24),
        GradientButton(
          label: game.started ? 'Stop' : 'Start',
          icon: game.started ? Icons.stop_rounded : Icons.play_arrow_rounded,
          gradient: game.started ? AppGradients.warm : AppGradients.primary,
          onPressed: game.started ? _stopRound : _startRound,
        ),
      ],
    );
  }
}

class QuerySnapshotEntries {
  QuerySnapshotEntries(this.entries);
  final Map<String, EntriesModel> entries;
}

/// The drawn letter as an animated gradient badge - the screen's
/// centerpiece, replacing the original's plain "Letter: X" text row.
/// Compact by design: it sits beside the score/code chips in a row rather
/// than centered above them, so the header doesn't push gameplay down.
class _LetterBadge extends StatelessWidget {
  const _LetterBadge({required this.game, required this.stopped});

  final GameModel game;
  final bool stopped;

  @override
  Widget build(BuildContext context) {
    final letterText = game.firstStart ? '?' : game.letter;

    return AnimatedSwitcher(
      duration: const Duration(milliseconds: 350),
      transitionBuilder: (child, animation) => ScaleTransition(
        scale: animation,
        child: FadeTransition(opacity: animation, child: child),
      ),
      child: Container(
        key: ValueKey('$letterText-$stopped'),
        width: 76,
        height: 76,
        decoration: BoxDecoration(
          gradient: AppGradients.warm,
          shape: BoxShape.circle,
          boxShadow: [
            BoxShadow(
              color: AppColors.amber.withValues(alpha: 0.35),
              blurRadius: 14,
              offset: const Offset(0, 6),
            ),
          ],
        ),
        alignment: Alignment.center,
        child: Text(
          letterText,
          style: GoogleFonts.fredoka(
            fontSize: 34,
            fontWeight: FontWeight.w700,
            color: Colors.white,
          ),
        ),
      ),
    );
  }
}

/// The current player's running score, as a small brand-colored pill.
class _ScorePill extends StatelessWidget {
  const _ScorePill({required this.score});

  final int score;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: scheme.primaryContainer,
        borderRadius: BorderRadius.circular(999),
      ),
      child: Text(
        'Score: $score',
        style: GoogleFonts.fredoka(
          fontSize: 14,
          fontWeight: FontWeight.w600,
          color: scheme.onPrimaryContainer,
        ),
      ),
    );
  }
}

/// Which round is in progress (or just finished), as a small pill.
class _RoundChip extends StatelessWidget {
  const _RoundChip({required this.round});

  final int round;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: AppColors.hayawan.withValues(alpha: 0.15),
        borderRadius: BorderRadius.circular(999),
      ),
      child: Text(
        'Round $round',
        style: GoogleFonts.fredoka(
          fontSize: 14,
          fontWeight: FontWeight.w600,
          color: scheme.brightness == Brightness.dark
              ? AppColors.hayawan
              : const Color(0xFF1E8A52),
        ),
      ),
    );
  }
}

/// How long the current round has been running (ticking live), or how
/// long the last one took once stopped - so players get a sense of pace
/// without either side needing to time it themselves.
class _TimerChip extends StatelessWidget {
  const _TimerChip({required this.seconds, required this.live});

  final int seconds;
  final bool live;

  static String _format(int totalSeconds) {
    final minutes = totalSeconds ~/ 60;
    final secs = totalSeconds % 60;
    return '$minutes:${secs.toString().padLeft(2, '0')}';
  }

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: AppColors.amber.withValues(alpha: 0.15),
        borderRadius: BorderRadius.circular(999),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(
            live ? Icons.timer_rounded : Icons.timer_outlined,
            size: 14,
            color: scheme.brightness == Brightness.dark
                ? AppColors.amber
                : const Color(0xFFB4750E),
          ),
          const SizedBox(width: 6),
          Text(
            _format(seconds),
            style: GoogleFonts.fredoka(
              fontSize: 14,
              fontWeight: FontWeight.w600,
              color: scheme.brightness == Brightness.dark
                  ? AppColors.amber
                  : const Color(0xFFB4750E),
            ),
          ),
        ],
      ),
    );
  }
}

/// The room's share code as a single tappable pill - replaces the earlier
/// text+icon crammed into the app bar, which had no room to breathe.
class _RoomCodeChip extends StatelessWidget {
  const _RoomCodeChip({required this.code, required this.onTap});

  final String code;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;

    return Material(
      color: scheme.secondaryContainer,
      borderRadius: BorderRadius.circular(999),
      child: InkWell(
        borderRadius: BorderRadius.circular(999),
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(
                Icons.groups_rounded,
                size: 14,
                color: scheme.onSecondaryContainer,
              ),
              const SizedBox(width: 6),
              Text(
                code,
                style: GoogleFonts.fredoka(
                  fontSize: 14,
                  fontWeight: FontWeight.w700,
                  letterSpacing: 1.5,
                  color: scheme.onSecondaryContainer,
                ),
              ),
              const SizedBox(width: 6),
              Icon(
                Icons.copy_rounded,
                size: 14,
                color: scheme.onSecondaryContainer,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

/// Horizontally-scrolling player chips (initial avatar + online dot + name
/// + score), replacing the original's compact vertical list.
class _PlayersRow extends ConsumerWidget {
  const _PlayersRow({required this.game, required this.playerNames});

  final GameModel game;
  final Map<String, String> playerNames;

  static const _palette = [
    AppColors.ensan,
    AppColors.hayawan,
    AppColors.shay2,
    AppColors.violet,
    AppColors.coral,
  ];

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final scheme = Theme.of(context).colorScheme;

    return SizedBox(
      height: 92,
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        itemCount: game.players.length,
        separatorBuilder: (context, index) => const SizedBox(width: 10),
        itemBuilder: (context, index) {
          final id = game.players[index];
          final name = playerNames[id] ?? '...';
          final score = game.scores[id] ?? 0;
          final online = ref.watch(onlineStatusProvider(id)).value == true;
          final color = _palette[id.hashCode.abs() % _palette.length];

          return Container(
            width: 76,
            padding: const EdgeInsets.symmetric(vertical: 6),
            decoration: BoxDecoration(
              color: scheme.surface,
              borderRadius: BorderRadius.circular(16),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withValues(alpha: 0.05),
                  blurRadius: 8,
                  offset: const Offset(0, 4),
                ),
              ],
            ),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Stack(
                  clipBehavior: Clip.none,
                  children: [
                    CircleAvatar(
                      radius: 16,
                      backgroundColor: color,
                      child: Text(
                        name.isNotEmpty ? name[0].toUpperCase() : '?',
                        style: const TextStyle(
                          color: Colors.white,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ),
                    if (online)
                      Positioned(
                        right: -1,
                        bottom: -1,
                        child: Container(
                          width: 10,
                          height: 10,
                          decoration: BoxDecoration(
                            color: AppColors.success,
                            shape: BoxShape.circle,
                            border: Border.all(
                              color: scheme.surface,
                              width: 2,
                            ),
                          ),
                        ),
                      ),
                  ],
                ),
                const SizedBox(height: 4),
                Text(
                  name,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(fontSize: 11),
                ),
                Text(
                  '$score',
                  style: TextStyle(
                    fontSize: 11,
                    fontWeight: FontWeight.bold,
                    color: color,
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}

/// A category's card: colored icon header, answer field and 0/5/10
/// self-score chips, with other players' answers folded in underneath
/// once the round is stopped.
class _CategoryCard extends StatelessWidget {
  const _CategoryCard({
    required this.label,
    required this.icon,
    required this.color,
    required this.controller,
    required this.enabled,
    required this.showScoreButtons,
    required this.selected,
    required this.onSelected,
    this.othersEntries,
  });

  final String label;
  final IconData icon;
  final Color color;
  final TextEditingController controller;
  final bool enabled;
  final bool showScoreButtons;
  final int? selected;
  final ValueChanged<int> onSelected;
  final Widget? othersEntries;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;

    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: scheme.surface,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: color.withValues(alpha: 0.25), width: 1.5),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.04),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(6),
                decoration: BoxDecoration(
                  color: color.withValues(alpha: 0.15),
                  borderRadius: BorderRadius.circular(10),
                ),
                child: Icon(icon, color: color, size: 18),
              ),
              const SizedBox(width: 8),
              Text(
                label,
                style: GoogleFonts.fredoka(
                  fontWeight: FontWeight.w600,
                  fontSize: 16,
                  color: color,
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),
          Row(
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              Expanded(
                child: TextField(
                  controller: controller,
                  enabled: enabled,
                  decoration: const InputDecoration(
                    hintText: 'Type your answer',
                    isDense: true,
                  ),
                ),
              ),
              if (showScoreButtons) ...[
                const SizedBox(width: 8),
                _ScoreChip(
                  value: 0,
                  selected: selected == 0,
                  color: color,
                  onTap: onSelected,
                ),
                const SizedBox(width: 4),
                _ScoreChip(
                  value: 5,
                  selected: selected == 5,
                  color: color,
                  onTap: onSelected,
                ),
                const SizedBox(width: 4),
                _ScoreChip(
                  value: 10,
                  selected: selected == 10,
                  color: color,
                  onTap: onSelected,
                ),
              ],
            ],
          ),
          othersEntries ?? const SizedBox.shrink(),
        ],
      ),
    );
  }
}

/// A 0/5/10 self-score option, tinted with its category's color and
/// popping slightly larger with a soft glow when selected.
class _ScoreChip extends StatelessWidget {
  const _ScoreChip({
    required this.value,
    required this.selected,
    required this.color,
    required this.onTap,
  });

  final int value;
  final bool selected;
  final Color color;
  final ValueChanged<int> onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      borderRadius: BorderRadius.circular(10),
      onTap: () => onTap(value),
      child: AnimatedScale(
        duration: const Duration(milliseconds: 150),
        scale: selected ? 1.1 : 1,
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 150),
          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
          decoration: BoxDecoration(
            color: selected ? color : color.withValues(alpha: 0.12),
            borderRadius: BorderRadius.circular(10),
            boxShadow: selected
                ? [
                    BoxShadow(
                      color: color.withValues(alpha: 0.4),
                      blurRadius: 8,
                      offset: const Offset(0, 3),
                    ),
                  ]
                : null,
          ),
          child: Text(
            '$value',
            style: TextStyle(
              color: selected ? Colors.white : color,
              fontWeight: FontWeight.bold,
            ),
          ),
        ),
      ),
    );
  }
}

/// Other players' answers once the round is stopped. The current player's
/// own answer is left out here since it's already visible in their field
/// above.
class _EntriesList extends StatelessWidget {
  const _EntriesList({
    required this.entries,
    required this.playerNames,
    required this.currentUserId,
    required this.selector,
  });

  final Map<String, EntriesModel> entries;
  final Map<String, String> playerNames;
  final String currentUserId;
  final String Function(EntriesModel) selector;

  @override
  Widget build(BuildContext context) {
    final others = entries.entries.where((e) => e.key != currentUserId);
    if (others.isEmpty) return const SizedBox.shrink();

    return Padding(
      padding: const EdgeInsets.only(top: 10),
      child: Wrap(
        spacing: 12,
        runSpacing: 4,
        children: others.map((entry) {
          final name = playerNames[entry.key] ?? entry.key;
          final value = selector(entry.value);
          return Text.rich(
            TextSpan(
              children: [
                TextSpan(
                  text: '$name: ',
                  style: const TextStyle(
                    fontWeight: FontWeight.w800,
                    fontSize: 12,
                  ),
                ),
                TextSpan(text: value, style: const TextStyle(fontSize: 12)),
              ],
            ),
          );
        }).toList(),
      ),
    );
  }
}
