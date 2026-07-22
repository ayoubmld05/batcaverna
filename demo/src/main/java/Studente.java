import java.util.ArrayList;
import java.util.List;
public class Studente{
    private String nome;
    private String cognome;
    private List<Esame> libretto;
    private float media;
    private int cfuOttenuti;
    private int cfuMancanti;
    private int cfuTriennale;
    private String matricola;
    public Studente(String nome,String cognome,String matricola){
        this.nome=nome;
        this.cognome=cognome;
        this.cfuTriennale=180;
        this.libretto=new ArrayList<>();
        this.matricola=matricola;
    }
    public String getMatricola() {
        return this.matricola;
    }
    public boolean superato(Esame esame){
        for(Esame esameInLibretto : this.libretto){
            if(esameInLibretto.getNome().equals(esame.getNome()) && esameInLibretto.getSuperato()==true){
                return true;
            }
        }   
        return false;
    }
    public String getNome(){
        return this.nome;
    }
    public String getCognome(){
        return this.cognome;
    }
    public int getcfuOttenuti(){
       if(this.libretto==null || this.libretto.isEmpty()){
        return 0;
       }
       int cfuTotali=0;
       for(Esame curr:this.libretto){
         if(curr.getSuperato()){
            cfuTotali+=curr.getCfu();
         }
       }
       this.cfuOttenuti=cfuTotali;
       return this.cfuOttenuti;
    }
    public int getcfuMancanti(){
        return this.cfuTriennale-this.getcfuOttenuti();
    }
    public float getMedia(){
        if(this.libretto==null || this.libretto.isEmpty()){
            return 0;
        }
        int votoCorrente=0;
        int cfuCorrente=0;
        int cfuTotali=0;
        int somma=0;
        for(Esame esameCurr : this.libretto){
            if(esameCurr.getSuperato()){
            votoCorrente=esameCurr.getVoto();
            cfuCorrente=esameCurr.getCfu();
            cfuTotali+=cfuCorrente;
            somma+=votoCorrente*cfuCorrente;
            }
        }

        this.media=(float)somma/cfuTotali;
        return this.media;
    }
    public void setNome(String nome){
        this.nome=nome;
    }
    public void setCognome(String cognome){
        this.cognome=cognome;
    }
 
    public void aggiungiEsameSuperato(Esame esame) {
        this.libretto.add(esame);
    }
    
    
      
    
    public void valutaRischio(Esame esame, DatabaseManager db, String nomeStudente) {
    
        // 1. FEEDBACK UI: Manteniamo i messaggi sui test dell'esame corrente
        List<Test> listaTest = db.haTest(esame.getNome());
        for(Test curr : listaTest) {
            int esito = db.haSuperatoTest(nomeStudente, curr.getTitolo());
            if(esito == 0) {
                System.out.println("Non hai ancora affrontato il test di prova: " + curr.getTitolo());
            } else if(esito >= curr.getSoglia_superamento()) {
                System.out.println(" Ottimo, hai superato il test '" + curr.getTitolo() + "' con " + esito);
            } else {
                System.out.println(" Attenzione, il test '" + curr.getTitolo() + "' è andato male (Voto: " + esito + ", Soglia: " + curr.getSoglia_superamento() + ")");
            }
        }
    
        // 2. CORE LOGIC: Deleghiamo il calcolo del Rischio Predittivo (Pilastro B) a Neo4j
        System.out.println("\n Calcolo del Rischio Predittivo basato sui Ponti Semantici...");
        db.calcolaRischioAvanzato(this.nome, esame.getNome());
    }
    
    public boolean possoFarlo(Esame esame,DatabaseManager db,String nomeStudente){
        if (esame.getPropedeutici() == null || esame.getPropedeutici().isEmpty()) {
            return true;
        }
        for( Esame curr : esame.getPropedeutici()){
            if(this.superato(curr)==false){
                return false;
            }
        }
        this.valutaRischio(esame,db,nomeStudente);
        return true;
    }
}
    
