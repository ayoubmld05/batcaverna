public class TestAssegnato extends Interazione {
    private String idTest;
    private String motivazione;

    public TestAssegnato(String id, long timestamp, String idTest, String motivazione) {
        super(id, timestamp);
        this.idTest = idTest;
        this.motivazione = motivazione;
    }
}