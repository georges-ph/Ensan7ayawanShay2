class EntriesModel {
  final String ensan;
  final String hayawan;
  final String shay2;

  const EntriesModel({
    this.ensan = '',
    this.hayawan = '',
    this.shay2 = '',
  });

  factory EntriesModel.fromJson(Map<String, dynamic> json) {
    return EntriesModel(
      ensan: json['ensan'] as String? ?? '',
      hayawan: json['hayawan'] as String? ?? '',
      shay2: json['shay2'] as String? ?? '',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'ensan': ensan,
      'hayawan': hayawan,
      'shay2': shay2,
    };
  }
}
