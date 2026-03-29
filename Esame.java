import java.util.ArrayList;
import java.util.List;
public class Esame{
    private String nome;
    private int cfu;
    private int voto;
    private boolean superato;
    private List <Argomento> argomenti;
    private List <Esame> propedeutici; 
    public Esame(String nome, int cfu, int voto, boolean superato){
        this.nome=nome;
        this.cfu=cfu;
        this.voto=voto;
        this.superato=superato;
        this.argomenti=new ArrayList<>(); 
        this.propedeutici=new ArrayList <>();
    }
    public String getNome(){
        return this.nome;
    }
    public int getCfu(){
        return this.cfu;
    }
    public int getVoto(){
        return this.voto;
    }
    public boolean getSuperato(){
        return this.superato;
    }
    public void setNome(String nome){
        this.nome=nome;
    }
    public void setCfu(int cfu){
        this.cfu=cfu;
    }
    public void setVoto(int voto){
        this.voto=voto;
    }
    public void setSuperato(boolean superato){
        this.superato=superato;
    }
    // Metodo per aggiungere un singolo argomento
    public void aggiungiArgomento(Argomento argomento) {
        this.argomenti.add(argomento);
    }
    public void aggiungiPropedeutico(Esame propedeutico){
        this.propedeutici.add(propedeutico);
    }
    public List<Esame> getPropedeutici(){
        return this.propedeutici;
    }
}