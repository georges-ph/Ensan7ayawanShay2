class UsersModel {
  final String id;
  final String name;
  final String email;
  final String image;

  const UsersModel({
    required this.id,
    required this.name,
    required this.email,
    required this.image,
  });

  factory UsersModel.fromJson(Map<String, dynamic> json) {
    return UsersModel(
      id: json['id'] as String? ?? '',
      name: json['name'] as String? ?? '',
      email: json['email'] as String? ?? '',
      image: json['image'] as String? ?? '',
    );
  }

  Map<String, dynamic> toJson() {
    return {'id': id, 'name': name, 'email': email, 'image': image};
  }

  UsersModel copyWith({String? name, String? image}) {
    return UsersModel(
      id: id,
      name: name ?? this.name,
      email: email,
      image: image ?? this.image,
    );
  }
}
