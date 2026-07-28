import java.util.List;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;

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
            System.out.print("Matricola: "); 
            String matricola = scanner.nextLine();
           
            Studente utenteLoggato = db.login_Studente(matricola,nome, cognome);

            if(utenteLoggato != null) {
                System.out.println("\n[SISTEMA] Autenticazione riuscita. Sincronizzazione memoria dell'Agente...");
                List<Esame> pianoStudi=db.getPianoDiStudi(utenteLoggato.getMatricola());
                if(pianoStudi.isEmpty()){
                    System.out.println("Non hai ancora caricato il tuo piano di studi, rimedia subito");
                    System.out.print("Ricordati di inserire il tuo piano di studi, dai non perdere tempo, fallo adesso");
                         // Crea il selettore di file
                JFileChooser fileChooser = new JFileChooser();
                
                // Imposta il filtro per mostrare solo i file PDF
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Documenti PDF", "pdf");
                fileChooser.setFileFilter(filter);
                fileChooser.setAcceptAllFileFilterUsed(false); // Disabilita l'opzione "Tutti i file"

                // Mostra la finestra di dialogo all'utente
                int risultato = fileChooser.showOpenDialog(null);

                // Se l'utente ha selezionato un file e ha cliccato "Apri"
                if (risultato == JFileChooser.APPROVE_OPTION) {
                    File fileSelezionato = fileChooser.getSelectedFile();
                    System.out.println("Hai scelto il file: " + fileSelezionato.getAbsolutePath());
                    
                    // Da qui puoi passare "fileSelezionato" ai metodi visti prima (per aprirlo o leggerlo)
                     try (PDDocument documento = Loader.loadPDF(fileSelezionato)) {
                            System.out.println("📖 Lettura del Regolamento Didattico in corso...");
                            PDFTextStripper estrattore = new PDFTextStripper();
                            String testoEstratto = estrattore.getText(documento);
                            if(testoEstratto.length() > 6000) testoEstratto = testoEstratto.substring(0, 6000);
                            String testoPulito = testoEstratto.replace("\n", " ").replace("\r", " ").replace("\t", " ");

                            String promptPiano = "Sei un estrattore dati universitario. Leggi il seguente testo tratto da un manifesto degli studi:\n" +
                            "--- INIZIO ---\n" + testoPulito + "\n--- FINE ---\n" +
                            "Trova tutti gli esami, i CFU, e le propedeuticità indicate.\n" +
                            "DEVI rispondere ESCLUSIVAMENTE con un JSON strutturato così, senza nient'altro:\n" +
                            "{\n  \"piano_di_studi\": [\n    { \"nome_esame\": \"Nome Esame\", \"cfu\": 9, \"esami_propedeutici\": [\"Nome Esame Bloccante\"] }\n  ]\n}";

                            Gson gsonPiano = new Gson();
                            java.util.Map<String, Object> reqMapPiano = java.util.Map.of("contents", java.util.List.of(java.util.Map.of("parts", java.util.List.of(java.util.Map.of("text", promptPiano)))));
                            
                            System.out.println("🤖 Alfred sta analizzando le propedeuticità...");
                            String urlPiano = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + "la_mia_chiave";
                            HttpClient client = HttpClient.newHttpClient();
                            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlPiano)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(gsonPiano.toJson(reqMapPiano))).build();
                            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                            
                            if (response.statusCode() == 200) {
                                JsonObject jsonResponse = gsonPiano.fromJson(response.body(), JsonObject.class);
                                String jsonInterno = jsonResponse.getAsJsonArray("candidates").get(0).getAsJsonObject().getAsJsonObject("content").getAsJsonArray("parts").get(0).getAsJsonObject().get("text").getAsString().replace("```json", "").replace("```", "").trim();
                                
                                RispostaPianoStudi datiPiano = gsonPiano.fromJson(jsonInterno, RispostaPianoStudi.class);
                                System.out.println("💾 Costruzione topologia Grafo in corso...");
                                // *** DEVI AVER CREATO QUESTO METODO NEL DB MANAGER ***
                                db.salvaPianoDiStudi(utenteLoggato.getMatricola(), datiPiano); 
                                
                                // Aggiorniamo la lista del piano studi appena caricata
                                pianoStudi = db.getPianoDiStudi(utenteLoggato.getMatricola());
                            } else {
                                System.out.println("❌ Errore API durante il caricamento del piano: " + response.statusCode());
                            }
                        } catch (Exception e) {
                            System.out.println("❌ Errore elaborazione PDF Regolamento: " + e.getMessage());
                        }
                    
                } else {
                    System.out.println("Selezione annullata dall'utente.");
                }
                }else{
                    StringBuilder promptConsiglio = new StringBuilder();
                    promptConsiglio.append("Sei Alfred, un Tutor accademico esperto. Analizza questo piano di studi e consiglia il miglior percorso che lo studente può intraprendere, spiegando la strategia (es. liberare propedeuticità, bilanciare CFU alti/bassi).\n");    
                    promptConsiglio.append("--- PIANO DI STUDI DELLO STUDENTE ---\n");
                    String carriera=db.getConsiglio(utenteLoggato.getMatricola());
                    promptConsiglio.append(carriera);
                    
                    //GSON
                    Gson gsonConsiglio=new Gson();
                    java.util.Map<String, Object> reqMapConsiglio = java.util.Map.of(
                        "contents", java.util.List.of(
                            java.util.Map.of("parts", java.util.List.of(
                                java.util.Map.of("text", promptConsiglio.toString())
                            ))
                        )
                    );
                    //spedisco a gemini
                    try{
                        String apiGemini="AQ.Ab8RN6I68n6-3Luk5V6oZ-eCTbYKTF8i_r0ly4EwLe5KMbniKAS";
                        String urlConsiglio = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiGemini;
                        HttpClient client=HttpClient.newHttpClient();
                        HttpRequest request=HttpRequest.newBuilder().uri(URI.create(urlConsiglio)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(gsonConsiglio.toJson(reqMapConsiglio)))
                        .build();
                        System.out.println("Alfred sta esaminando il miglior percorso");
                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        if (response.statusCode() == 200) {
                            // 4. LEGGHIAMO LA RISPOSTA
                            JsonObject jsonResponse = gsonConsiglio.fromJson(response.body(), JsonObject.class);
                            String rispostaAlfred = jsonResponse.getAsJsonArray("candidates")
                                    .get(0).getAsJsonObject()
                                    .getAsJsonObject("content")
                                    .getAsJsonArray("parts")
                                    .get(0).getAsJsonObject()
                                    .get("text").getAsString().trim();

                            // 5. MOSTRA IL CAPOLAVORO!
                            System.out.println("\n--- 🧠 IL PERCORSO MIGLIORE SECONDO ALFRED ---\n");
                            System.out.println(rispostaAlfred);
                            
                            // (Opzionale: salvi il consiglio nel Grafo come "Memoria")
                            db.salvaNuovoConsiglio(utenteLoggato.getNome(), rispostaAlfred);
                            
                        } else {
                             System.out.println("❌ Errore API: " + response.statusCode());
                        }
                    } catch (Exception ex) {
                        System.err.println("Errore di rete durante la consultazione di Alfred: " + ex.getMessage());
                    }
                    }
                
                // --- INIZIO: RISVEGLIO DI ALFRED ---
                double media = db.getMediaStudente(utenteLoggato.getNome());
                int cfu = db.getCfuStudente(utenteLoggato.getNome());
                List<Esame> esamiFalliti = db.getListaEsamiNonSuperati(utenteLoggato.getNome());
                List<Consiglio> ultimiConsigli = db.getListaConsigli(utenteLoggato.getNome());

                StringBuilder promptBuilder = new StringBuilder();
                promptBuilder.append("Sei Alfred, un Tutor AI universitario avanzato e proattivo. ");
                promptBuilder.append("Lo studente ").append(utenteLoggato.getNome()).append(" ha appena fatto il login. ");
                promptBuilder.append("DATI GRAFO: Media: ").append(String.format("%.1f", media)).append(", CFU: ").append(cfu).append(". ");
                
                if (!esamiFalliti.isEmpty()) {
                    promptBuilder.append("Attenzione, ha fallito l'esame: ").append(esamiFalliti.get(0).getNome()).append(". ");
                }
                if (!ultimiConsigli.isEmpty()) {
                    promptBuilder.append("L'ultima volta gli hai detto: '").append(ultimiConsigli.get(0).getTestoGenerato()).append("'. ");
                }
                promptBuilder.append("TASK: Scrivi un breve messaggio di bentornato (max 3 frasi). Sii empatico, fagli capire che ricordi la sua situazione e proponigli di mettersi al lavoro. Rispondi SOLO con il messaggio, senza formattazioni o virgolette.");

                // Costruiamo il JSON in modo sicuro con Gson
                Gson gsonAlfred = new Gson();
                java.util.Map<String, Object> reqMapAlfred = java.util.Map.of(
                    "contents", java.util.List.of(
                        java.util.Map.of("parts", java.util.List.of(
                            java.util.Map.of("text", promptBuilder.toString())
                        ))
                    )
                );
                String requestBodyAlfred = gsonAlfred.toJson(reqMapAlfred);

                try {
                    String apiGemini = "AQ.Ab8RN6I68n6-3Luk5V6oZ-eCTbYKTF8i_r0ly4EwLe5KMbniKAS"; 
                    
                    String urlAlfred = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiGemini;
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(urlAlfred))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(requestBodyAlfred))
                            .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        JsonObject jsonResponse = gsonAlfred.fromJson(response.body(), JsonObject.class);
                        String rispostaAlfred = jsonResponse.getAsJsonArray("candidates")
                                .get(0).getAsJsonObject()
                                .getAsJsonObject("content")
                                .getAsJsonArray("parts")
                                .get(0).getAsJsonObject()
                                .get("text").getAsString().trim();

                        System.out.println("\n🤖 ALFRED: " + rispostaAlfred);
                        db.salvaNuovoConsiglio(utenteLoggato.getNome(), rispostaAlfred);
                        System.out.println("[SISTEMA] Memoria di Alfred aggiornata nel Grafo.\n");
                    } else {
                        System.out.println("🤖 ALFRED: Ciao " + utenteLoggato.getNome() + ", sono online in modalità limitata oggi.\n");
                    }
                } catch (Exception e) {
                    System.out.println("🤖 ALFRED: Ciao " + utenteLoggato.getNome() + ", pronto per studiare?\n");
                }
                // --- FINE: RISVEGLIO DI ALFRED ---


                System.out.println("Quale esame vuoi tentare?");
                String esameScelto = scanner.nextLine();
                
                Esame esameDaFare = new Esame(esameScelto, 0, 0, false, 0.0f);
                
                System.out.println("\n🔎 Avvio scansione predittiva sul Grafo...");
                utenteLoggato.valutaRischio(esameDaFare, db, nome);

                System.out.println("\nVuoi analizzare un PDF del professore per questo esame? (si/no)");
                String risposta = scanner.nextLine().trim();

                while(risposta.equalsIgnoreCase("si")) {
                    
                    System.out.println(" Trascina il file PDF dal Finder qui nel terminale e premi Invio (oppure scrivi il percorso a mano):");
                    String percorsoFile = scanner.nextLine();
                    
                    percorsoFile = percorsoFile.replace("\"", "").replace("'", "").trim();
                    File filePdf = new File(percorsoFile);

                    if (filePdf.exists() && filePdf.isFile() && filePdf.getName().toLowerCase().endsWith(".pdf")) {
                        System.out.println("✅ File trovato: " + filePdf.getName());
                        
                        try (PDDocument documento = Loader.loadPDF(filePdf)) {
                            System.out.println("📖 Lettura del PDF in corso...");
                            PDFTextStripper estrattore = new PDFTextStripper();
                            String testoEstratto = estrattore.getText(documento);
                            
                            if(testoEstratto.length() > 5000) {
                                testoEstratto = testoEstratto.substring(0, 5000);
                            }
                            
                            String testoPulito = testoEstratto.replace("\n", " ").replace("\r", " ").replace("\t", " ");                                
                            
                            // PROMPT PULITO
                            String prompt = "Sei un assistente per l'apprendimento. Leggi il seguente testo:\n" +
                            "--- INIZIO TESTO ---\n" +
                            testoPulito + "\n" +
                            "--- FINE TESTO ---\n" +
                            "Estrai gli argomenti principali e genera un test di autovalutazione.\n" +
                            "DEVI rispondere ESCLUSIVAMENTE con un JSON strutturato in questo esatto modo, senza aggiungere codice markdown o altro testo:\n" +
                            "{\n" +
                            "  \"esame_riferimento\": \"" + esameScelto + "\",\n" +
                            "  \"argomenti_estratti\": [\n" +
                            "    {\n" +
                            "      \"nome\": \"Nome Argomento\",\n" +
                            "      \"descrizione\": \"Descrizione Argomento\",\n" +
                            "      \"test_associato\": { \"id\": \"T_AUTO_001\", \"titolo\": \"Test\", \"num_domande\": 5, \"soglia_superamento\": 3 }\n" +
                            "    }\n" +
                            "  ]\n" +
                            "}";

                            // MAGIA GSON (Costruzione infallibile del JSON)
                            Gson gsonPipeline = new Gson();
                            java.util.Map<String, Object> requestMap = java.util.Map.of(
                                "contents", java.util.List.of(
                                    java.util.Map.of("parts", java.util.List.of(
                                        java.util.Map.of("text", prompt)
                                    ))
                                )
                            );
                            String requestBodyJSON = gsonPipeline.toJson(requestMap);
                            
                            String apiGemini = "AQ.Ab8RN6I68n6-3Luk5V6oZ-eCTbYKTF8i_r0ly4EwLe5KMbniKAS";
                            
                            // MODELLO CORRETTO (1.5-flash, il 2.5 non esiste)
                            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiGemini.trim();
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
                                JsonObject rispostaCompleta = gsonPipeline.fromJson(response.body(), JsonObject.class);
                                String jsonInterno = rispostaCompleta.getAsJsonArray("candidates")
                                                                .get(0).getAsJsonObject()
                                                                .getAsJsonObject("content")
                                                                .getAsJsonArray("parts")
                                                                .get(0).getAsJsonObject()
                                                                .get("text").getAsString();
                                
                                jsonInterno = jsonInterno.replace("```json", "").replace("```", "").trim();
                                
                                RispostaLLM datiEstratti = gsonPipeline.fromJson(jsonInterno, RispostaLLM.class);

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
            
        } catch (Exception e) {
            System.err.println("Errore critico: " + e.getMessage());
        }
    } 
}