import java.util.Scanner; 

public class Main {
    public static void main(String[] args) {
        
        String uri = "bolt://localhost:7687";
        String utente = "neo4j";
        String password = "zakaria11"; 

        System.out.println("Connessione al Cervello Predittivo (Neo4j)...");

        try (DatabaseManager db = new DatabaseManager(uri, utente, password)) {
            
            Scanner scanner = new Scanner(System.in);
            System.out.println("\n--- LOGIN STUDENTE ---");
            System.out.print("Nome: ");
            String nome = scanner.nextLine();
            System.out.print("Cognome: "); 
            String cognome = scanner.nextLine();
            
            Studente utenteLoggato = db.login_Studente(nome, cognome);
            
            if(utenteLoggato != null) {
                System.out.println("\n✅ Bentornato " + utenteLoggato.getNome() + " " + utenteLoggato.getCognome());
                
                System.out.println("\nQuale esame vuoi tentare?");
                String esameScelto = scanner.nextLine();
                
                // Creiamo la "scatola" dell'esame scelto
                Esame esameDaFare = new Esame(esameScelto, 0, 0, false,0.0f);
                
                System.out.println("\n🔎 Avvio scansione predittiva sul Grafo...");
                // QUI AVVIENE LA MAGIA! Passiamo l'esame e il db!
                utenteLoggato.valutaRischio(esameDaFare, db);
                
            } else {
                System.out.println("\n❌ Nessuno studente trovato. Riprova.");
            }
            
            scanner.close();

        } catch (Exception e) {
            System.err.println("Errore critico: " + e.getMessage());
        }
    } 
}