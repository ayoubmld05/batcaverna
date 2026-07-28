import java.util.List;

public class RispostaPianoStudi {
    // Deve corrispondere alla chiave JSON "piano_di_studi"
    private List<EsameEstratto> piano_di_studi; 
    private String id; 

    public List<EsameEstratto> getPianoDiStudi(){
        return this.piano_di_studi;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
}