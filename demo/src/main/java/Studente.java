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
    public void valutaRischio(Esame esame,DatabaseManager db){
        int somma=0;
        int sommaCfu=0;
        int votoPiuBasso=31;
        String nomeDelpiuBasso= "";
        int votoPiuAlto=0;
        String nomeDelpiuAlto="";
        Esame basso=null;
        Esame alto=null;
      List <Esame> listaProp=db.verifica_propedeutici(esame.getNome());
      if(listaProp.size()==0){
        System.out.println("Nessun rischio: non ci sono esami propedeutici.");
      }else{
        for(Esame esameCorrente:listaProp){
               Esame curr=db.superato_propedeutico(esameCorrente.getNome(), this.getNome());
               if(curr!=null){
                if(curr.getVoto()<=votoPiuBasso){
                    basso=curr;
                    votoPiuBasso=curr.getVoto();
                    nomeDelpiuBasso=curr.getNome();
                }
                if(curr.getVoto()>votoPiuAlto){
                    alto=curr;
                    votoPiuAlto=curr.getVoto();
                    nomeDelpiuAlto=curr.getNome();
                }
                 somma+=curr.getVoto()*curr.getCfu();
                 sommaCfu+=curr.getCfu();
               }else{
                System.out.println("Non hai superato l'esame"+ esameCorrente.getNome());
               }
               
        }   
        if (sommaCfu > 0) {
            float media = (float) somma / sommaCfu;
            System.out.println("La media dei tuoi esami propedeutici è " + media);
        } else {
            System.out.println("Non hai superato nessun esame propedeutico per fare media.");
        }
        System.out.println("La media dei tuoi esami propedeutici è" + " "+media );
        System.out.println("Il voto più basso che hai preso tra i tuoi propedeutici è" +" "+nomeDelpiuBasso + " "+votoPiuBasso);
        System.out.println("Il voto più alto che hai preso tra i tuoi propedeutici è" +" "+nomeDelpiuAlto +" "+ votoPiuAlto);
        if(votoPiuBasso >= 22){
            System.out.println("Non c'è tantissimo da preoccuparti... bisogna rivedere soltanto questi argomenti:");
        } else {
            System.out.println("Allarme rosso! Abbiamo bisogno di recuperare al meglio questi argomenti:");
            //chiamo gli argomenti dell più basso
            if(basso!=null ){       
                List <Argomento> arg=db.haArgomenti(basso.getNome());
                int count=arg.size();
                for(Argomento curr:arg){
                    System.out.println("Bisogna ripassare"+curr.getNome());
                    if(count>1){
                        System.out.println("Anche...");
                    }
                    count--;
                }
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
    
