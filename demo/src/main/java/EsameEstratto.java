import java.util.List;
import java.util.ArrayList;

public class EsameEstratto {
    // Devono corrispondere alle chiavi JSON ("nome_esame", "esami_propedeutici")
    private String nome_esame;
    private int cfu;
    private List<String> esami_propedeutici;

    public EsameEstratto(String nome_esame, int cfu){
        this.nome_esame = nome_esame;
        this.cfu = cfu;
        this.esami_propedeutici = new ArrayList<>();
    }

    public int getCfu() {
        return cfu;
    }
    public List<String> getEsamiPropedeutici() {
        return esami_propedeutici;
    }
    public String getNomeEsame() {
        return nome_esame;
    }
}