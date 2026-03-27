import java.util.ArrayList;
import java.util.List;
public class Studente{
    private String nome;
    private String cognome;
    private List<Esame> esami;
    private float media;
    private int cfuOttenuti;
    private int cfuMancanti;
    public Studente(String nome,String cognome,int cfuOttenuti){
        this.nome=nome;
        this.cognome=cognome;
        this.cfuOttenuti=cfuOttenuti;
        this.esami=new ArrayList<>();
    }
    public String getNome(){S
        return this.nome;
    }
    public String getCognome(){
        return this.cognome;
    }
    public int getcfuOttenuti(){
        return this.cfuOttenuti;
    }
    public int getcfuMancanti(){
        return this.cfuMancanti;
    }
    public float getMedia(){
        return this.media;
    }
    
    public void setNome(String nome){
        this.nome=nome;
    }
    public void setCognome(String cognome){
        this.cognome=cognome;
    }
    public void setcfuOttenuti(int cfuOttenuti){
        this.cfuOttenuti=cfuOttenuti;
    }
    public void setcfuMancanti(int cfuMancanti){
        this.cfuMancanti=cfuMancanti;
    }
    public void setMedia(float media){
        this.media=media;
    }
    // Metodo per registrare un esame superato nel libretto
    public void aggiungiEsame(Esame esame) {
        this.esami.add(esame);
    }
}