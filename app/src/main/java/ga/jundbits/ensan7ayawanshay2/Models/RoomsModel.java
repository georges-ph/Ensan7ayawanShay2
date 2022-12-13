package ga.jundbits.ensan7ayawanshay2.Models;

public class RoomsModel {

    private String created_by;
    private long timestamp_millis;

    public RoomsModel() {

    }

    public RoomsModel(String created_by, long timestamp_millis) {
        this.created_by = created_by;
        this.timestamp_millis = timestamp_millis;
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public long getTimestamp_millis() {
        return timestamp_millis;
    }

    public void setTimestamp_millis(long timestamp_millis) {
        this.timestamp_millis = timestamp_millis;
    }

}
