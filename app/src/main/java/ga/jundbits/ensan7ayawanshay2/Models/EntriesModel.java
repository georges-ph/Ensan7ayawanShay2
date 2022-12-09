package ga.jundbits.ensan7ayawanshay2.Models;

public class EntriesModel {

    private String ensan, hayawan, shay2;

    public EntriesModel() {

    }

    public EntriesModel empty() {
        return new EntriesModel("", "", "");
    }

    public EntriesModel(String ensan, String hayawan, String shay2) {
        this.ensan = ensan;
        this.hayawan = hayawan;
        this.shay2 = shay2;
    }

    public String getEnsan() {
        return ensan;
    }

    public void setEnsan(String ensan) {
        this.ensan = ensan;
    }

    public String getHayawan() {
        return hayawan;
    }

    public void setHayawan(String hayawan) {
        this.hayawan = hayawan;
    }

    public String getShay2() {
        return shay2;
    }

    public void setShay2(String shay2) {
        this.shay2 = shay2;
    }
}
