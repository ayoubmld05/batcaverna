public class TestSvolto extends Interazione {
    private String idTest; // Quale test ha fatto
    private int punteggioOttenuto; // Il voto preso
    private boolean superato; // True se punteggio >= soglia_superamento

    public TestSvolto(String id, long timestamp, String idTest, int punteggio, boolean superato) {
        super(id, timestamp);
        this.idTest = idTest;
        this.punteggioOttenuto = punteggio;
        this.superato = superato;
    }
}