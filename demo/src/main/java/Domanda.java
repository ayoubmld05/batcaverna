public class Domanda extends Interazione {
    private String testoRichiesta;  //cosa ha chiesto lo studente
    public Domanda(String id, long timestamp, String testoRichiesta) {
        super(id, timestamp);
        this.testoRichiesta = testoRichiesta;
    }
} 
