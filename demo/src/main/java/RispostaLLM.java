import java.util.List;

public class RispostaLLM {
    // I nomi delle variabili DEVONO essere identici alle chiavi del JSON
    private String esame_riferimento;
    private List<Argomento> argomenti_estratti;

    // Aggiungi solo i Getter (Gson farà il resto in automatico!)R
    public String getEsameRiferimento() { return esame_riferimento; }
    public List<Argomento> getArgomentiEstratti() { return argomenti_estratti; }
}
