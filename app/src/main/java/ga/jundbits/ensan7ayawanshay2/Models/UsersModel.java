package ga.jundbits.ensan7ayawanshay2.Models;

public class UsersModel {

    private String id, name, image;
    private boolean online;

    public UsersModel() {

    }

    public UsersModel(String id, String name, String image, boolean online) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.online = online;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public boolean isOnline() {
        return online;
    }

}
