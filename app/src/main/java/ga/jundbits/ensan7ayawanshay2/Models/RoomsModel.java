package ga.jundbits.ensan7ayawanshay2.Models;

import java.util.List;

public class RoomsModel {

    private List<String> players;
    private long timestamp_millis;

    public RoomsModel() {

    }

    public RoomsModel(List<String> players, long timestamp_millis) {
        this.players = players;
        this.timestamp_millis = timestamp_millis;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void setPlayers(List<String> players) {
        this.players = players;
    }

    public long getTimestamp_millis() {
        return timestamp_millis;
    }

    public void setTimestamp_millis(long timestamp_millis) {
        this.timestamp_millis = timestamp_millis;
    }

}
