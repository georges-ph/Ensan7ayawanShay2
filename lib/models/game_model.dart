class GameModel {
  final bool firstStart;
  final bool started;
  final String letter;
  final String createdBy;
  final List<String> players;
  final Map<String, int> scores;
  final int timestampMillis;

  const GameModel({
    required this.firstStart,
    required this.started,
    required this.letter,
    required this.createdBy,
    required this.players,
    required this.scores,
    required this.timestampMillis,
  });

  factory GameModel.fromJson(Map<String, dynamic> json) {
    return GameModel(
      firstStart: json['first_start'] as bool? ?? true,
      started: json['started'] as bool? ?? false,
      letter: json['letter'] as String? ?? '',
      createdBy: json['created_by'] as String? ?? '',
      players: List<String>.from(json['players'] as List? ?? const []),
      scores: (json['scores'] as Map?)?.map(
            (key, value) => MapEntry(key as String, (value as num).toInt()),
          ) ??
          const {},
      timestampMillis: (json['timestamp_millis'] as num?)?.toInt() ?? 0,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'first_start': firstStart,
      'started': started,
      'letter': letter,
      'created_by': createdBy,
      'players': players,
      'scores': scores,
      'timestamp_millis': timestampMillis,
    };
  }

  GameModel copyWith({
    bool? firstStart,
    bool? started,
    String? letter,
    Map<String, int>? scores,
  }) {
    return GameModel(
      firstStart: firstStart ?? this.firstStart,
      started: started ?? this.started,
      letter: letter ?? this.letter,
      createdBy: createdBy,
      players: players,
      scores: scores ?? this.scores,
      timestampMillis: timestampMillis,
    );
  }
}
