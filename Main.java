public class Main{
    public static void main(String[] args){
        Studente utente=new Studente("Ayoub", "Miladi");
        System.out.println("utente creato :"+ utente.getNome() + " "+ utente.getCognome());
        Esame analisi1=new Esame("Analisi 1", 9, 26, true);
        Esame dataBase=new Esame("Data Base", 9, 0, false);
        Esame sistemiOperativi=new Esame("Sistemi Operativi", 9, 23, true);
        utente.aggiungiEsameSuperato(analisi1);
        utente.aggiungiEsameSuperato(dataBase);
        utente.aggiungiEsameSuperato(sistemiOperativi);
        Esame architettura=new Esame("Architettura", 6, 0,false);
        Esame programmazione1=new Esame("Programmazione1", 9, 0,false);
        sistemiOperativi.aggiungiPropedeutico(architettura);
        sistemiOperativi.aggiungiPropedeutico(programmazione1);
        architettura.setSuperato(true);
        architettura.setVoto(25);
        programmazione1.setSuperato(true);
        programmazione1.setVoto(27);
        utente.aggiungiEsameSuperato(architettura);
        utente.aggiungiEsameSuperato(programmazione1);
        Argomento mem=new Argomento("Gestione della memoria", "Gerarchia delle memorie e Cache");
        Argomento cpu=new Argomento("Pipeline della CPU","Esecuzione delle istruzioni e hazard");
        architettura.aggiungiArgomento(mem);
        architettura.aggiungiArgomento(cpu);
        if(utente.possoFarlo(sistemiOperativi)){
            System.out.println("Ottimo puoi fare"+ sistemiOperativi.getNome());
        }else{
            System.out.println("Mi dispiace non puoi farlo" + sistemiOperativi.getNome()+ ",ti mancano dei propedeutici");
        }
        System.out.println("Aggiornamento libretto");
        System.out.println("Cfu contenuti in questo momento" + utente.getcfuOttenuti());
        System.out.println("CFU Mancanti per la Triennale: " + utente.getcfuMancanti());
        System.out.println("Media Attuale: " + utente.getMedia());
        
    }
}