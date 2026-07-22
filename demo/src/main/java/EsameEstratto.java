import java.util.ArrayList;

public class EsameEstratto {
    private String nomeEsame;
    private int cfu;
    private List<String> listaPropedeutici;
    public EsameEstratto(String nomeEsame,int cfu){
        this.nomeEsame=nomeEsame;
        this.cfu=cfu;
        this.listaPropedeutici=new ArrayList<>();
    }
    public int getCfu() {
        return cfu;
    }
    public List<EsameEstrato> getListaPropedeutici() {
        return listaPropedeutici;
    }
    public String getNomeEsame() {
        return nomeEsame;
    }
}