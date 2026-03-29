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
    public Studente(String nome,String cognome){
        this.nome=nome;
        this.cognome=cognome;
        this.cfuTriennale=180;
        this.libretto=new ArrayList<>();
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
    public boolean possoFarlo(Esame esame){
        if (esame.getPropedeutici() == null || esame.getPropedeutici().isEmpty()) {
            return true;
        }
        for( Esame curr : esame.getPropedeutici()){
            if(this.superato(curr)==false){
                return false;
            }
        }
        return true;
    }
    
}