public class RispostaPianoStudi {
    private List<EsameEstrato> listaEsami; 
    private int id;
    public List<EsameEstrato>  getListaEsami(){
        return this.listaEsami;
    }
    public int getId() {
        return id;
    }
    public void addEsame(EsameEstratto esame){
        this.listaEsami.add(esame);
    }
}
