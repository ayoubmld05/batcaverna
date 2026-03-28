public class Main{
    public static void main(String[] args){
        Studente utente=new Studente("Ayoub", "Miladi");
        System.out.println("utente creato :"+ utente.getNome() + " "+ utente.getCognome());
        Esame analisi1=new Esame("Analisi 1", 9, 26, true);
        Esame dataBase=new Esame("Data Base", 9, 0, false);
        Esame sistemiOperativi=new Esame("Sistemi Operativi", 9, 23, true);
        utente.aggiungiEsame(analisi1);
        utente.aggiungiEsame(dataBase);
        utente.aggiungiEsame(sistemiOperativi);
        System.out.println("Aggiornamento libretto");
        System.out.println("Cfu contenuti in questo momento" + utente.getcfuOttenuti());
        System.out.println("CFU Mancanti per la Triennale: " + utente.getcfuMancanti());
        System.out.println("Media Attuale: " + utente.getMedia());
    }

}