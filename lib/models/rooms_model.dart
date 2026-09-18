class RoomsModel {
  final String createdBy;
  final int timestampMillis;

  const RoomsModel({
    required this.createdBy,
    required this.timestampMillis,
  });

  factory RoomsModel.fromJson(Map<String, dynamic> json) {
    return RoomsModel(
      createdBy: json['created_by'] as String? ?? '',
      timestampMillis: (json['timestamp_millis'] as num?)?.toInt() ?? 0,
    );
  }
}
