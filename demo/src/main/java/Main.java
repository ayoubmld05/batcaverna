import java.util.List;
import java.util.Scanner;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.sound.midi.SysexMessage;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;
import java.io.IOException;


import java.io.File;
import java.io.IOException;

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
                System.out.println("\n Bentornato " + utenteLoggato.getNome() + " " + utenteLoggato.getCognome());
                //qui devo fare le query
                double media=db.getMediaStudente(utenteLoggato.getNome());
                System.out.println("\n La tua media "+ media);
                int cfu=db.getCfuStudente(utenteLoggato.getNome());
                System.out.println("\n I tuoi cfu "+ cfu);
                List <Esame> listaEsamiNonPassati=db.getListaEsamiNonSuperati(utenteLoggato.getNome());
                







                System.out.println("Quale esame vuoi tentare?");
                String esameScelto = scanner.nextLine();
                
                // Creiamo la "scatola" dell'esame scelto
                Esame esameDaFare = new Esame(esameScelto, 0, 0, false,0.0f);
                
                System.out.println("\n🔎 Avvio scansione predittiva sul Grafo...");
                // QUI AVVIENE LA MAGIA! Passiamo l'esame e il db!
                utenteLoggato.valutaRischio(esameDaFare, db,nome);
                System.out.println("Vuoi inserire del materiale nuovo?");
                JFileChooser fileChooser = new JFileChooser();

                System.out.println("\nVuoi analizzare un PDF del professore per questo esame? (si/no)");
                String risposta = scanner.nextLine().trim();

                while(risposta.equalsIgnoreCase("si")) {
                    
                    // NIENTE PIÙ JFILECHOOSER! Chiediamo il percorso direttamente nel terminale.
                    System.out.println(" Trascina il file PDF dal Finder qui nel terminale e premi Invio (oppure scrivi il percorso a mano):");
                    String percorsoFile = scanner.nextLine();
                    
                    // Pulizia del percorso: a volte il Mac aggiunge apici o spazi extra quando trascini i file
                    percorsoFile = percorsoFile.replace("\"", "").replace("'", "").trim();
                    
                    File filePdf = new File(percorsoFile);

                    // Controlliamo che il file esista davvero e sia un PDF
                    if (filePdf.exists() && filePdf.isFile() && filePdf.getName().toLowerCase().endsWith(".pdf")) {
                        System.out.println("✅ File trovato: " + filePdf.getName());
                        
                        try (PDDocument documento = Loader.loadPDF(filePdf)) {
                            System.out.println("📖 Lettura del PDF in corso...");
                            PDFTextStripper estrattore = new PDFTextStripper();
                            String testoEstratto = estrattore.getText(documento);
                            
                            if(testoEstratto.length() > 5000) {
                                testoEstratto = testoEstratto.substring(0, 5000);
                            }
                            
                            // Pulizia radicale del testo
                            String testoPulito = testoEstratto.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ").replace("\t", " ");                                
                            
                            String prompt = "Sei un assistente per l'apprendimento. Leggi il seguente testo:\n" +
                            "--- INIZIO TESTO ---\n" +
                            testoPulito + "\n" +
                            "--- FINE TESTO ---\n" +
                            "Estrai gli argomenti principali e genera un test di autovalutazione.\n" +
                            "DEVI rispondere ESCLUSIVAMENTE con un JSON strutturato in questo esatto modo, senza aggiungere codice markdown o altro testo:\n" +
                            "{\n" +
                            "  \\\"esame_riferimento\\\": \\\"" + esameScelto + "\\\",\n" +
                            "  \\\"argomenti_estratti\\\": [\n" +
                            "    {\n" +
                            "      \\\"nome\\\": \\\"Nome Argomento\\\",\n" +
                            "      \\\"descrizione\\\": \\\"Descrizione Argomento\\\",\n" +
                            "      \\\"test_associato\\\": { \\\"id\\\": \\\"T_AUTO_001\\\", \\\"titolo\\\": \\\"Test\\\", \\\"num_domande\\\": 5, \\\"soglia_superamento\\\": 3 }\n" +
                            "    }\n" +
                            "  ]\n" +
                            "}";

                            String requestBodyJSON = "{\"contents\":[{\"parts\":[{\"text\":\"" + prompt + "\"}]}]}";
                            
                            // LA TUA CHIAVE API (Assicurati che sia corretta e senza spazi!)
                            String apiGemini="AIzaSyDHPXKTi_toa11UiJTZzeb-NpmR5bj7exU";
                            // Passiamo dalla versione v1beta alla versione v1 (stabile)
                            // Il trim() ci salva da eventuali spazi accidentali    
                            // Ora puntiamo dritti a gemini-2.0-flash!
                            // Puntiamo alla versione V1 (stabile) del modello 1.5-flash
                            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiGemini.trim();
                            HttpClient client = HttpClient.newHttpClient();
                            HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(requestBodyJSON))
                            .build();
                            
                            System.out.println("🤖 Invio dati a Gemini per l'analisi...");
                            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                            
                            if(response.statusCode() == 200){
                                System.out.println("✅ RISPOSTA RICEVUTA DA GEMINI!");
                                Gson gson = new Gson();
                                JsonObject rispostaCompleta = gson.fromJson(response.body(), JsonObject.class);
                                String jsonInterno = rispostaCompleta.getAsJsonArray("candidates")
                                                                .get(0).getAsJsonObject()
                                                                .getAsJsonObject("content")
                                                                .getAsJsonArray("parts")
                                                                .get(0).getAsJsonObject()
                                                                .get("text").getAsString();
                                
                                jsonInterno = jsonInterno.replace("```json", "").replace("```", "").trim();
                                
                                RispostaLLM datiEstratti = gson.fromJson(jsonInterno, RispostaLLM.class);

                                System.out.println("💾 Salvataggio nel database...");
                                
                                if (datiEstratti != null && datiEstratti.getArgomentiEstratti() != null) {
                                    for(Argomento arg : datiEstratti.getArgomentiEstratti()){
                                        System.out.println("📌 Creazione Argomento: " + arg.getNome());
                                        Test testAssociato = arg.getTestAssociato();
                                        if(testAssociato != null) {
                                            db.creazioneTestPerArgomento(esameScelto, testAssociato, arg);
                                        } else {
                                            System.out.println("⚠️ Nessun test associato per questo argomento.");
                                        }
                                    }
                                    System.out.println("✨ Processo completato! Nodi iniettati nel Grafo.");
                                } else {
                                    System.out.println("❌ Errore: Gemini non ha restituito gli argomenti nel formato corretto.");
                                }

                            } else {
                                System.out.println("❌ Errore HTTP: " + response.statusCode());
                                System.out.println("Dettaglio dell'errore da Google: " + response.body()); 
                            }
                        } catch (Exception e) {
                            System.out.println("❌ Errore durante l'elaborazione del PDF!");
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("❌ Errore: Il file non esiste, non è un PDF, oppure il percorso è sbagliato.");
                    }
                    
                    System.out.println("\nVuoi caricare un altro PDF? (si/no)");
                    risposta = scanner.nextLine().trim();
                }
                
            } else {
                System.out.println("\n❌ Nessuno studente trovato. Riprova.");
            }
            
            scanner.close();

        } catch (Exception e) {
            System.err.println("Errore critico: " + e.getMessage());
        }
    } 
}