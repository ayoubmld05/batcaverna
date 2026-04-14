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
    public void valutaRischio(Esame esame, DatabaseManager db){
        List <Esame> listaProp=db.verifica_propedeutici(esame.getNome());
        float rischioMassimo=0.0f;
        Esame esamePiuRischioso=null;
        if(listaProp.size()==0){
            System.out.println("Nessun rischio: non ci sono esami propedeutici.");
        }else{
            for(Esame esameCorrente:listaProp){
               Esame curr=db.superato_propedeutico(esame.getNome(), this.getNome());
               if(curr!=null){
                int lacuna=30-curr.getVoto();
                float rischioAttuale=lacuna*curr.getTassoMortalita();
                System.out.println("-> Esame: " + curr.getNome() + " | Voto: " + curr.getVoto() + 
                                   " | Mortalità: " + curr.getTassoMortalita() + 
                                   " => INDICE RISCHIO: " + rischioAttuale);
                if(rischioMassimo<rischioAttuale){
                    rischioMassimo=rischioAttuale;
                    esamePiuRischioso=curr;
                }
               }else{
                System.out.println("Non si è superato l'esame prepedeuticio "+ curr.getNome());
                return;
               }
            }
            float allarme = 5.0f; //ma non posso..fa ridere già così che io metta una regola fissa come soglia d'allarme
            if(rischioMassimo<allarme){
                System.out.println("Non c'è nulla di cui preoccuparsi...infatti il tuo rischio massimo è di appena "+rischioMassimo+" mentre la soglia è "+allarme);
                System.out.println("Le tue basi sono solide...buono studio per l'esame " +esame.getNome());
            }else{
                System.out.println("Il Rischio purtroppo è piuttosto  alto (" + rischioMassimo + ") derivante dalle tue lacune in: " + esamePiuRischioso.getNome());
                System.out.println("Prima di tentare " + esame.getNome() + ",  ti consiglio di ripassare questi argomenti chiave:");
                List <Argomento> argomentiDellEsame=db.haArgomenti(esamePiuRischioso.getNome());
                if(argomentiDellEsame!=null && !argomentiDellEsame.isEmpty()){
                    for(Argomento curr:argomentiDellEsame){
                        System.out.println("Devi ripassare" + curr.getNome() + " " + curr.getDescrizione());
                    }
                }else{
                    System.out.println("Nessun argomento specifico trovato");
                }
            }
        }
    }
      
    
   
    
    public boolean possoFarlo(Esame esame,DatabaseManager db){
        if (esame.getPropedeutici() == null || esame.getPropedeutici().isEmpty()) {
            return true;
        }
        for( Esame curr : esame.getPropedeutici()){
            if(this.superato(curr)==false){
                return false;
            }
        }
        this.valutaRischio(esame,db);
        return true;
    }
}
    
