public class Test {
    private String ID;
    private String titolo;
    private int num_domande;
    private int soglia_superamento;
    public Test(String ID, String titolo,int num_domande,int soglia_superamento){
        this.ID=ID;
        this.titolo=titolo;
        this.num_domande=num_domande;
        this.soglia_superamento=soglia_superamento;
    }
    public String getID(){
        return this.ID;
    }
    public String getTitolo(){
        return this.titolo;
    }
    public int getNum_domande(){
        return this.num_domande;
    }
    public int getSoglia_superamento(){
        return this.soglia_superamento;
    }
    public void setSoglia_superamento(int nuovaSoglia){
        this.soglia_superamento=nuovaSoglia;
    }

}
