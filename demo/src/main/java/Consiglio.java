// File: Consiglio.java
public class Consiglio extends Interazione {
    private String testoGenerato;    
    private String categoriaAzione;  
    private String statoEsecuzione; 

    public Consiglio(String id, long timestamp, String testoGenerato, String categoria) {
        super(id, timestamp);
        this.testoGenerato = testoGenerato;
        this.categoriaAzione = categoria;
        this.statoEsecuzione = "PENDING"; 
    }

    public String getTestoGenerato() { return testoGenerato; }
    public String getCategoriaAzione() { return categoriaAzione; }
}