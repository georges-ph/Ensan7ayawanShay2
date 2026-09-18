class UsersModel {
  final String id;
  final String name;
  final String email;
  final String image;
  final String fcmToken;
  final bool notifications;

  const UsersModel({
    required this.id,
    required this.name,
    required this.email,
    required this.image,
    required this.fcmToken,
    required this.notifications,
  });

  factory UsersModel.fromJson(Map<String, dynamic> json) {
    return UsersModel(
      id: json['id'] as String? ?? '',
      name: json['name'] as String? ?? '',
      email: json['email'] as String? ?? '',
      image: json['image'] as String? ?? '',
      fcmToken: json['fcm_token'] as String? ?? '',
      notifications: json['notifications'] as bool? ?? true,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'email': email,
      'image': image,
      'fcm_token': fcmToken,
      'notifications': notifications,
    };
  }

  UsersModel copyWith({
    String? name,
    String? image,
    String? fcmToken,
    bool? notifications,
  }) {
    return UsersModel(
      id: id,
      name: name ?? this.name,
      email: email,
      image: image ?? this.image,
      fcmToken: fcmToken ?? this.fcmToken,
      notifications: notifications ?? this.notifications,
    );
  }
}
