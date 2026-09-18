import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../models/entries_model.dart';
import '../models/game_model.dart';
import '../providers/auth_providers.dart';
import '../providers/firebase_providers.dart';
import '../services/firestore_paths.dart';
import '../theme/app_colors.dart';

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

  StreamSubscription<GameModel?>? _gameSub;
  StreamSubscription<QuerySnapshotEntries>? _entriesSub;

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
    _ensanController.dispose();
    _hayawanController.dispose();
    _shay2Controller.dispose();
    super.dispose();
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
    } else if (!game.firstStart) {
      if (_appliedStopForLetter != game.letter) {
        _appliedStopForLetter = game.letter;
        _appliedStartForLetter = null;

        await ref.read(gameServiceProvider).saveOwnEntries(
              widget.roomId,
              _userId,
              EntriesModel(
                ensan: _ensanController.text,
                hayawan: _hayawanController.text,
                shay2: _shay2Controller.text,
              ),
            );

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
    await ref.read(gameServiceProvider).stopRound(widget.roomId);
  }

  @override
  Widget build(BuildContext context) {
    final game = _game;

    return Scaffold(
      appBar: AppBar(title: const Text('Game Room')),
      body: game == null
          ? const Center(child: CircularProgressIndicator())
          : _buildBody(game),
    );
  }

  Widget _buildBody(GameModel game) {
    final stopped = !game.started && !game.firstStart;

    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _LabelValue(
                    label: 'Letter:',
                    value: game.firstStart
                        ? 'Press START button'
                        : stopped
                            ? '${game.letter}, STOPPED'
                            : game.letter,
                  ),
                  const SizedBox(height: 8),
                  _LabelValue(label: 'Your score:', value: '$_totalScore'),
                ],
              ),
            ),
            const SizedBox(width: 24),
            Expanded(
              child: _PlayersList(game: game, playerNames: _playerNames),
            ),
          ],
        ),
        const SizedBox(height: 24),
        _CategoryRow(
          label: 'Ensan',
          controller: _ensanController,
          enabled: game.started,
          showScoreButtons: stopped,
          selected: _ensanSelected,
          onSelected: (v) => setState(() => _ensanSelected = v),
        ),
        if (stopped)
          _EntriesList(
            entries: _allEntries,
            playerNames: _playerNames,
            selector: (e) => e.ensan,
          ),
        const SizedBox(height: 24),
        _CategoryRow(
          label: '7ayawan',
          controller: _hayawanController,
          enabled: game.started,
          showScoreButtons: stopped,
          selected: _hayawanSelected,
          onSelected: (v) => setState(() => _hayawanSelected = v),
        ),
        if (stopped)
          _EntriesList(
            entries: _allEntries,
            playerNames: _playerNames,
            selector: (e) => e.hayawan,
          ),
        const SizedBox(height: 24),
        _CategoryRow(
          label: 'Shay2',
          controller: _shay2Controller,
          enabled: game.started,
          showScoreButtons: stopped,
          selected: _shay2Selected,
          onSelected: (v) => setState(() => _shay2Selected = v),
        ),
        if (stopped)
          _EntriesList(
            entries: _allEntries,
            playerNames: _playerNames,
            selector: (e) => e.shay2,
          ),
        const SizedBox(height: 24),
        SizedBox(
          width: double.infinity,
          child: FilledButton(
            onPressed: game.started ? _stopRound : _startRound,
            child: Text(game.started ? 'Stop' : 'Start'),
          ),
        ),
      ],
    );
  }
}

class _LabelValue extends StatelessWidget {
  const _LabelValue({required this.label, required this.value});

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Text.rich(
      TextSpan(
        children: [
          TextSpan(text: '$label '),
          TextSpan(
            text: value,
            style: const TextStyle(fontWeight: FontWeight.bold),
          ),
        ],
      ),
    );
  }
}

class QuerySnapshotEntries {
  QuerySnapshotEntries(this.entries);
  final Map<String, EntriesModel> entries;
}

/// Compact vertical player list (online dot + name + score), matching
/// game_room_players_list_item.xml rather than a wrapped chip row.
class _PlayersList extends ConsumerWidget {
  const _PlayersList({required this.game, required this.playerNames});

  final GameModel game;
  final Map<String, String> playerNames;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: game.players.map((id) {
        final score = game.scores[id] ?? 0;
        final name = playerNames[id] ?? '...';

        return Padding(
          padding: const EdgeInsets.symmetric(vertical: 2),
          child: Row(
            children: [
              StreamBuilder<bool>(
                stream: ref.watch(presenceServiceProvider).onlineStatus(id),
                builder: (context, snapshot) {
                  if (snapshot.data != true) return const SizedBox(width: 8);
                  return const Padding(
                    padding: EdgeInsets.only(right: 4),
                    child: CircleAvatar(
                      radius: 4,
                      backgroundColor: Colors.green,
                    ),
                  );
                },
              ),
              Expanded(
                child: Text(name, style: const TextStyle(fontSize: 12)),
              ),
              Text('$score', style: const TextStyle(fontSize: 12)),
            ],
          ),
        );
      }).toList(),
    );
  }
}

/// A category's text field plus its 0/5/10 self-score buttons, side by
/// side, matching each TextInputLayout + points LinearLayout pair in
/// activity_game_room.xml.
class _CategoryRow extends StatelessWidget {
  const _CategoryRow({
    required this.label,
    required this.controller,
    required this.enabled,
    required this.showScoreButtons,
    required this.selected,
    required this.onSelected,
  });

  final String label;
  final TextEditingController controller;
  final bool enabled;
  final bool showScoreButtons;
  final int? selected;
  final ValueChanged<int> onSelected;

  @override
  Widget build(BuildContext context) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.center,
      children: [
        Expanded(
          child: TextField(
            controller: controller,
            enabled: enabled,
            decoration: InputDecoration(labelText: label),
          ),
        ),
        if (showScoreButtons) ...[
          const SizedBox(width: 8),
          _ScoreChip(value: 0, selected: selected == 0, onTap: onSelected),
          const SizedBox(width: 4),
          _ScoreChip(value: 5, selected: selected == 5, onTap: onSelected),
          const SizedBox(width: 4),
          _ScoreChip(value: 10, selected: selected == 10, onTap: onSelected),
        ],
      ],
    );
  }
}

/// Matches drawable/game_room_score_background.xml: a small rounded
/// twilight-lavender chip, turning green when selected (same as the
/// original's setBackgroundTintList(Color.GREEN) on tap).
class _ScoreChip extends StatelessWidget {
  const _ScoreChip({
    required this.value,
    required this.selected,
    required this.onTap,
  });

  final int value;
  final bool selected;
  final ValueChanged<int> onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      borderRadius: BorderRadius.circular(5),
      onTap: () => onTap(value),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 5),
        decoration: BoxDecoration(
          color: selected ? Colors.green : AppColors.twilightLavender1,
          borderRadius: BorderRadius.circular(5),
        ),
        child: Text('$value', style: const TextStyle(color: Colors.white)),
      ),
    );
  }
}

class _EntriesList extends StatelessWidget {
  const _EntriesList({
    required this.entries,
    required this.playerNames,
    required this.selector,
  });

  final Map<String, EntriesModel> entries;
  final Map<String, String> playerNames;
  final String Function(EntriesModel) selector;

  @override
  Widget build(BuildContext context) {
    if (entries.isEmpty) return const SizedBox.shrink();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: entries.entries.map((entry) {
        final name = playerNames[entry.key] ?? entry.key;
        final value = selector(entry.value);
        return Padding(
          padding: const EdgeInsets.symmetric(vertical: 2),
          child: Text('$name: $value'),
        );
      }).toList(),
    );
  }
}
