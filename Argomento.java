public class Argomento {
    private String nome;
    private String descrizione;

    public Argomento(String nome, String descrizione) {
        this.nome = nome;
        this.descrizione = descrizione;
    }

    public String getNome() { return this.nome; }
    public String getDescrizione() { return this.descrizione; }

    public void setNome(String nome) { this.nome = nome; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }
}