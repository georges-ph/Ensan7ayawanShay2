package ga.jundbits.ensan7ayawanshay2.Models;

import java.util.List;
import java.util.Map;

public class GameModel {

    private boolean first_start, started;
    private String letter;
    private List<String> players;
    private Map<String, Integer> scores;
    private long timestamp_millis;

    private GameModel() {

    }

    public GameModel(boolean first_start, boolean started, String letter, List<String> players, Map<String, Integer> scores, long timestamp_millis) {
        this.first_start = first_start;
        this.started = started;
        this.letter = letter;
        this.players = players;
        this.scores = scores;
        this.timestamp_millis = timestamp_millis;
    }

    public boolean isFirst_start() {
        return first_start;
    }

    public void setFirst_start(boolean first_start) {
        this.first_start = first_start;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public String getLetter() {
        return letter;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void setPlayers(List<String> players) {
        this.players = players;
    }

    public Map<String, Integer> getScores() {
        return scores;
    }

    public void setScores(Map<String, Integer> scores) {
        this.scores = scores;
    }

    public long getTimestamp_millis() {
        return timestamp_millis;
    }

    public void setTimestamp_millis(long timestamp_millis) {
        this.timestamp_millis = timestamp_millis;
    }
}
