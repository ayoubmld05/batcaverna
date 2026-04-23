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
    public void valutaRischio(Esame esame, DatabaseManager db,String nomeStudente){
        List <Esame> listaProp=db.verifica_propedeutici(esame.getNome());
        List <Test> listaTest=db.haTest(esame.getNome());
        for(Test curr : listaTest){
            int esito=db.haSuperatoTest(nomeStudente,curr.getTitolo());
            if(esito==0){
                System.out.println("Non hai ancora affrontato il test: " + curr.getTitolo());
            }else if(esito>curr.getSoglia_superamento()){
                System.out.println("Ottimo, hai superato il test "+ curr.getTitolo() + " nella parte dell'esame di "+ " "+ esame.getNome() + "con un ottimo voto, " + esito);
            }else{
                System.out.println("Caspita, controllando i test dell'esame "+ " "+ esame.getNome()+" " + "il test"+ curr.getTitolo() + " " + "non è andato benissimo...infatti hai preso " + esito + " "+ "mentre la soglia minima era di " + curr.getSoglia_superamento());
            }
        }
        if(listaProp.size()==0){
            System.out.println("Nessun rischio: non ci sono esami propedeutici.");
        }else{
            for(Esame esameCorrente:listaProp){
               Esame curr=db.superato_propedeutico(esame.getNome(), this.getNome());
               List <Test> listaTest2=new ArrayList<>();
                listaTest2=db.haTest(curr.getNome());
                for(Test curr2:listaTest2){
                    int esito=db.haSuperatoTest(nomeStudente, curr2.getTitolo());
                    if(esito>curr2.getSoglia_superamento()){
                        System.out.println("Ottimo, negli esemi propedeutici, hai superato il test "+ curr2.getTitolo() + " nella parte dell'esame di "+ " "+ esame.getNome() + "con un ottimo voto, " + esito);
                    }else{
                        System.out.println("Caspita, negli esemi propedeutici, controllando i test dell'esame "+ " "+ esame.getNome()+" " + "il test"+ curr2.getTitolo() + " " + "non è andato benissimo...infatti hai preso " + esito + " "+ "mentre la soglia minima era di " + curr2.getSoglia_superamento());
                    }
                }
            }
            List <Argomento> listaArgomentoConLacune= db.contaLacuneAttive(nomeStudente, esame.getNome());
            int numeroLacune =listaArgomentoConLacune.size();
            float rischioReale = numeroLacune * esame.getTassoMortalita(); // Es: 3 lacune * 0.8 mortalità = 2.4 rischio
            if(rischioReale > 2.0f) { // Ora la soglia ha un senso statistico!
                System.out.println("Attenzione, Hai " + numeroLacune + " lacune gravi su questo esame");
                System.out.println("Più precisamente, tra le lacune abbiamo:");
                for(Argomento arg:listaArgomentoConLacune){
                    System.out.println("Argomento:" + arg.getNome());
                }
            } else {
                System.out.println("Ottimo, Rischio calcolato a " + rischioReale + ". Puoi procedere.");
            }            
        }
    }
      
    
   
    
    public boolean possoFarlo(Esame esame,DatabaseManager db,String nomeStudente){
        if (esame.getPropedeutici() == null || esame.getPropedeutici().isEmpty()) {
            return true;
        }
        for( Esame curr : esame.getPropedeutici()){
            if(this.superato(curr)==false){
                return false;
            }
        }
        this.valutaRischio(esame,db,nomeStudente);
        return true;
    }
}
    
