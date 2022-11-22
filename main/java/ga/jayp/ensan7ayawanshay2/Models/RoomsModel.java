package ga.jayp.ensan7ayawanshay2.Models;

import java.util.List;

public class RoomsModel {

    private List<String> players;

    public RoomsModel() {

    }

    public RoomsModel(List<String> players) {
        this.players = players;
    }

    public List<String> getPlayers() {
        return players;
    }

}
