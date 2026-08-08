import java.util.List;
import java.util.Scanner;
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

import java.util.Properties;
import java.io.FileInputStream;

public class Main {
    public static void main(String[] args) {
        // 1. CREDENZIALI (Definite una volta sola per tutto il programma)
        String uri = "bolt://localhost:7687";
        String utente = "neo4j";
        String password = "zakaria11"; 
        // --- LETTURA DELLA CHIAVE API DAL FILE SEGRETO ---
        String apiGemini = "";
        try (FileInputStream input = new FileInputStream("config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            apiGemini = prop.getProperty("API_KEY");
            
            if (apiGemini == null || apiGemini.trim().isEmpty()) {
                System.err.println(" Errore: La chiave API_KEY è vuota nel file config.properties!");
                return; // Blocca il programma
            }
        } catch (Exception e) {
            System.err.println(" Errore: File config.properties non trovato nella cartella principale.");
            System.err.println("Assicurati di aver creato il file e averci scritto API_KEY=la_tua_chiave");
            return; // Blocca il programma se non trova la chiave
        }
                          
        System.out.println("Connessione al Cervello Predittivo (Neo4j)...");

        try (DatabaseManager db = new DatabaseManager(uri, utente, password)) {
            
            Scanner scanner = new Scanner(System.in);
            System.out.println("\n--- LOGIN STUDENTE ---");
            System.out.print("Nome: ");
            String nome = scanner.nextLine().trim();
            System.out.print("Cognome: "); 
            String cognome = scanner.nextLine().trim();
            System.out.print("Matricola: "); 
            String matricola = scanner.nextLine().trim();
           
            Studente utenteLoggato = db.login_Studente(matricola, nome, cognome);

            if (utenteLoggato != null) {
                System.out.println("\n[SISTEMA] Autenticazione riuscita. Sincronizzazione memoria dell'Agente...");
                List<Esame> pianoStudi = db.getPianoDiStudi(utenteLoggato.getMatricola());
                
                // --- GESTIONE PIANO DI STUDI ---
                if (pianoStudi.isEmpty()) {
                    System.out.println("Non hai ancora caricato il tuo piano di studi, rimedia subito");
                    System.out.println("📄 Trascina il file PDF del Piano di Studi dal Finder qui nel terminale e premi Invio:");
                    String percorsoPiano = scanner.nextLine().replace("\"", "").replace("'", "").trim();
                    File fileSelezionato = new File(percorsoPiano);
                         
                    if (fileSelezionato.exists() && fileSelezionato.isFile() && fileSelezionato.getName().toLowerCase().endsWith(".pdf")) {
                        System.out.println("✅ Hai scelto il file: " + fileSelezionato.getAbsolutePath());
                        try (PDDocument documento = Loader.loadPDF(fileSelezionato)) {
                            System.out.println("📖 Lettura del Regolamento Didattico in corso...");
                            PDFTextStripper estrattore = new PDFTextStripper();
                            String testoEstratto = estrattore.getText(documento);
                            if (testoEstratto.length() > 6000) testoEstratto = testoEstratto.substring(0, 6000);
                            String testoPulito = testoEstratto.replace("\n", " ").replace("\r", " ").replace("\t", " ");

                            String promptPiano = "Sei un estrattore dati universitario. Leggi il seguente testo tratto da un manifesto degli studi:\n" +
                            "--- INIZIO ---\n" + testoPulito + "\n--- FINE ---\n" +
                            "Trova tutti gli esami, i CFU, e le propedeuticità indicate.\n" +
                            "DEVI rispondere ESCLUSIVAMENTE con un JSON strutturato così, senza nient'altro:\n" +
                            "{\n  \"piano_di_studi\": [\n    { \"nome_esame\": \"Nome Esame\", \"cfu\": 9, \"esami_propedeutici\": [\"Nome Esame Bloccante\"] }\n  ]\n}";

                            Gson gsonPiano = new Gson();
                            java.util.Map<String, Object> reqMapPiano = java.util.Map.of("contents", java.util.List.of(java.util.Map.of("parts", java.util.List.of(java.util.Map.of("text", promptPiano)))));
                            
                            System.out.println("🤖 Alfred sta analizzando le propedeuticità...");
                            String urlPiano = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=" + apiGemini.trim();
                            HttpClient client = HttpClient.newHttpClient();
                            HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(urlPiano))
                                .header("Content-Type", "application/json")
                                .POST(HttpRequest.BodyPublishers.ofString(gsonPiano.toJson(reqMapPiano)))
                                .build();

                            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                            if (response.statusCode() == 200) {
                                JsonObject jsonResponse = gsonPiano.fromJson(response.body(), JsonObject.class);
                                String jsonInterno = jsonResponse.getAsJsonArray("candidates").get(0).getAsJsonObject().getAsJsonObject("content").getAsJsonArray("parts").get(0).getAsJsonObject().get("text").getAsString().replace("```json", "").replace("```", "").trim();
                                
                                RispostaPianoStudi datiPiano = gsonPiano.fromJson(jsonInterno, RispostaPianoStudi.class);
                                System.out.println("💾 Costruzione topologia Grafo in corso...");
                                db.salvaPianoDiStudi(utenteLoggato.getMatricola(), datiPiano); 
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
                } else {
                    // --- CONSIGLIO STRATEGICO ---
                    StringBuilder promptConsiglio = new StringBuilder();
                    promptConsiglio.append("Sei Alfred, un Tutor accademico esperto. Analizza questo piano di studi e consiglia il miglior percorso che lo studente può intraprendere, spiegando la strategia (es. liberare propedeuticità, bilanciare CFU alti/bassi).\n");    
                    promptConsiglio.append("--- PIANO DI STUDI DELLO STUDENTE ---\n");
                    String carriera = db.getConsiglio(utenteLoggato.getMatricola());
                    promptConsiglio.append(carriera);
                    
                    Gson gsonConsiglio = new Gson();
                    java.util.Map<String, Object> reqMapConsiglio = java.util.Map.of("contents", java.util.List.of(java.util.Map.of("parts", java.util.List.of(java.util.Map.of("text", promptConsiglio.toString())))));
                    
                    try {
                        String urlConsiglio = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=" + apiGemini.trim();
                        HttpClient client = HttpClient.newHttpClient();
                        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlConsiglio)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(gsonConsiglio.toJson(reqMapConsiglio))).build();
                        System.out.println("🤖 Alfred sta esaminando il miglior percorso...");
                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        if (response.statusCode() == 200) {
                            JsonObject jsonResponse = gsonConsiglio.fromJson(response.body(), JsonObject.class);
                            String rispostaAlfred = jsonResponse.getAsJsonArray("candidates").get(0).getAsJsonObject().getAsJsonObject("content").getAsJsonArray("parts").get(0).getAsJsonObject().get("text").getAsString().trim();

                            System.out.println("\n--- 🧠 IL PERCORSO MIGLIORE SECONDO ALFRED ---\n");
                            System.out.println(rispostaAlfred);
                            db.salvaNuovoConsiglio(utenteLoggato.getNome(), rispostaAlfred);
                        }
                    } catch (Exception ex) {
                        System.err.println("Errore di rete durante la consultazione di Alfred: " + ex.getMessage());
                    }
                }
                
                // --- RISVEGLIO DI ALFRED E MESSAGGIO PROATTIVO ---
                double media = db.getMediaStudente(utenteLoggato.getNome());
                int cfu = db.getCfuStudente(utenteLoggato.getNome());
                List<Esame> esamiFalliti = db.getListaEsamiNonSuperati(utenteLoggato.getNome());
                List<Consiglio> ultimiConsigli = db.getListaConsigli(utenteLoggato.getNome());
                List <String> ultimiTest=db.ultimiTest(utenteLoggato.getMatricola());

                StringBuilder promptBuilder = new StringBuilder();
                promptBuilder.append("Sei Alfred, un Tutor AI universitario avanzato e proattivo. ");
                promptBuilder.append("Lo studente ").append(utenteLoggato.getNome()).append(" ha appena fatto il login. ");
                promptBuilder.append("DATI GRAFO: Media: ").append(String.format("%.1f", media)).append(", CFU: ").append(cfu).append(". ");
                
                if (!esamiFalliti.isEmpty()) {
                    promptBuilder.append("Attenzione, ha fallito l'esame: ").append(esamiFalliti.get(0).getNome()).append(". ");
                }
                if (!ultimiConsigli.isEmpty()) {
                    promptBuilder.append("Durante tutto il tuo monitoraggio, in passato gli hai dato tutti questi consigli: ");
                    for (Consiglio consiglio : ultimiConsigli) {
                        promptBuilder.append(" '").append(consiglio.getTestoGenerato()).append("'; ");
                    }
                }
                //aggiungo qui come novità i test fatti in passato
                if(!ultimiTest.isEmpty()){
                    promptBuilder.append("Di recente ha svolto questi test di autovalutazione: ");
                    for(String t:ultimiTest){
                        promptBuilder.append("[").append(t).append("] ");
                    }
                }
                promptBuilder.append("TASK: Scrivi un breve messaggio di bentornato (max 4 frasi). Sii empatico, fai un brevissimo accenno ai vecchi consigli e, se ha fatto dei test di recente, complimentati per i risultati alti o incoraggialo se i punteggi sono bassi. Rispondi SOLO con il messaggio, senza formattazioni o virgolette.");

                Gson gsonAlfred = new Gson();
                java.util.Map<String, Object> reqMapAlfred = java.util.Map.of("contents", java.util.List.of(java.util.Map.of("parts", java.util.List.of(java.util.Map.of("text", promptBuilder.toString())))));
                
                try {
                    String urlAlfred = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=" + apiGemini.trim();
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlAlfred)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(gsonAlfred.toJson(reqMapAlfred))).build();
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        JsonObject jsonResponse = gsonAlfred.fromJson(response.body(), JsonObject.class);
                        String rispostaAlfred = jsonResponse.getAsJsonArray("candidates").get(0).getAsJsonObject().getAsJsonObject("content").getAsJsonArray("parts").get(0).getAsJsonObject().get("text").getAsString().trim();
                        System.out.println("\n🤖 ALFRED: " + rispostaAlfred);
                        db.salvaNuovoConsiglio(utenteLoggato.getNome(), rispostaAlfred);
                        System.out.println("[SISTEMA] Memoria di Alfred aggiornata nel Grafo.\n");
                    } else {
                        System.out.println("🤖 ALFRED: Ciao " + utenteLoggato.getNome() + ", sono online in modalità limitata oggi.\n");
                    }
                } catch (Exception e) {
                    System.out.println("🤖 ALFRED: Ciao " + utenteLoggato.getNome() + ", pronto per studiare?\n");
                }
                
                // --- CICLO VITALE (MAIN LOOP) ---
                boolean alfredAcceso = true;
                while (alfredAcceso) {
                    System.out.println("\n=======================================================");
                    System.out.println("Quale esame vuoi tentare? (oppure scrivi 'esci' per spegnere)");
                    System.out.println("=======================================================");
                    String esameScelto = scanner.nextLine().trim();
                    
                    if (esameScelto.equalsIgnoreCase("esci")) {
                        System.out.println("\n🤖 ALFRED: Ho salvato tutti i progressi nella mia memoria a Grafo. Ottimo lavoro oggi, " + utenteLoggato.getNome() + ". A presto!");
                        System.out.println("--- CHIUSURA SISTEMA ---");
                        alfredAcceso = false;
                        break; 
                    }

                    Esame esameDaFare = new Esame(esameScelto, 0, 0, false, 0.0f);
                    
                    // --- GENERAZIONE TEST SUL TERMINALE ---
                    System.out.println("\n🤖 Alfred sta preparando un test di verifica su " + esameScelto + "...");
                    StringBuilder richiestaTest = new StringBuilder();
                    richiestaTest.append("Genera un test a risposta multipla di 3 domande sull'esame di ").append(esameScelto).append(". ");
                    richiestaTest.append("Rispondi ESCLUSIVAMENTE con un JSON strutturato in questo modo: {\"titolo_test\":\"Titolo\", \"lista_domande\": [{\"testo_domanda\":\"Domanda?\", \"opzioni\":[\"A) ops1\", \"B) ops2\", \"C) ops3\", \"D) ops4\"], \"risposta_corretta\":\"A\", \"spiegazione\":\"Il motivo è...\"}]}");
                    
                    Gson gsonTest = new Gson();
                    java.util.Map<String, Object> reqMapTest = java.util.Map.of("contents", java.util.List.of(java.util.Map.of("parts", java.util.List.of(java.util.Map.of("text", richiestaTest.toString())))));
                    
                    try {
                        String urlTest = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=" + apiGemini.trim();
                        HttpClient client = HttpClient.newHttpClient();
                        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlTest)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(gsonTest.toJson(reqMapTest))).build();
                        
                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        
                        if (response.statusCode() == 200) {
                            JsonObject jsonResponse = gsonTest.fromJson(response.body(), JsonObject.class);
                            String jsonPulito = jsonResponse.getAsJsonArray("candidates").get(0).getAsJsonObject().getAsJsonObject("content").getAsJsonArray("parts").get(0).getAsJsonObject().get("text").getAsString().replace("```json", "").replace("```", "").trim();
                            
                            RispostaLLMTest testGenerato = gsonTest.fromJson(jsonPulito, RispostaLLMTest.class);
                            
                            System.out.println("\n📝 INIZIO TEST: " + testGenerato.getTitoloTest());
                            int punteggio = 0;
                            //qui chiamo per salvare
                            if(testGenerato.getListaDomande() != null) {
                                for (int i = 0; i < testGenerato.getListaDomande().size(); i++) {
                                    DomandaTest dom = testGenerato.getListaDomande().get(i);
                                    System.out.println("\nDomanda " + (i+1) + ": " + dom.getTestoDomanda());
                                    for (String opzione : dom.getOpzioni()) {
                                        System.out.println(opzione);
                                    }
                                    
                                    System.out.print("\nDigita la tua risposta (es. A) e premi Invio: ");
                                    String rispostaUtente = scanner.nextLine().trim();
                                    
                                    if (rispostaUtente.equalsIgnoreCase(dom.getRispostaCorretta()) || rispostaUtente.contains(dom.getRispostaCorretta())) {
                                        System.out.println("✅ CORRETTO!");
                                        punteggio++;
                                    } else {
                                        System.out.println("❌ SBAGLIATO! La risposta giusta era: " + dom.getRispostaCorretta());
                                    }
                                    System.out.println("💡 Spiegazione: " + dom.getSpiegazione());
                                }
                                System.out.println("\n🏆 Test completato! Punteggio: " + punteggio + "/" + testGenerato.getListaDomande().size());
                                
                                // Salvataggio nel DB (Assicurati che il metodo accetti questi tipi)
                                String idTestUnico = "TEST_" + esameScelto.replaceAll("\\s+", "").toUpperCase() + "_" + System.currentTimeMillis();
                                db.salvataggioTest( utenteLoggato.getMatricola(),  idTestUnico,  testGenerato.getTitoloTest(),  testGenerato.getListaDomande().size(),  esameScelto,  punteggio);
                                System.out.println("[SISTEMA] Esito salvato nel Grafo.");
                            }
                        } else {
                            System.out.println("❌ Errore API durante la generazione del test.");
                        }
                    } catch (Exception e) {
                        System.out.println("❌ Si è verificato un errore durante il test: " + e.getMessage());
                    }

                    // --- SCANSIONE PREDITTIVA E ANALISI PDF ---
                    System.out.println("\n🔎 Avvio scansione predittiva sul Grafo...");
                    utenteLoggato.valutaRischio(esameDaFare, db, utenteLoggato.getNome());

                    System.out.println("\nVuoi analizzare un PDF del professore per questo esame? (si/no)");
                    String risposta = scanner.nextLine().trim();

                    while(risposta.equalsIgnoreCase("si")) {
                        System.out.println("📄 Trascina il file PDF dal Finder qui nel terminale e premi Invio:");
                        String percorsoFile = scanner.nextLine().replace("\"", "").replace("'", "").trim();
                        File filePdf = new File(percorsoFile);

                        if (filePdf.exists() && filePdf.isFile() && filePdf.getName().toLowerCase().endsWith(".pdf")) {
                            System.out.println("✅ File trovato. Lettura in corso...");
                            try (PDDocument documento = Loader.loadPDF(filePdf)) {
                                PDFTextStripper estrattore = new PDFTextStripper();
                                String testoEstratto = estrattore.getText(documento);
                                if(testoEstratto.length() > 5000) testoEstratto = testoEstratto.substring(0, 5000);
                                String testoPulito = testoEstratto.replace("\n", " ").replace("\r", " ").replace("\t", " ");                                
                                
                                String promptPdf = "Sei un assistente per l'apprendimento. Leggi il seguente testo:\n" + 
                                "--- INIZIO TESTO ---\n" + testoPulito + "\n--- FINE TESTO ---\n" +
                                "Estrai gli argomenti principali e genera un test di autovalutazione.\n" +
                                "DEVI rispondere ESCLUSIVAMENTE con un JSON strutturato in questo esatto modo, senza aggiungere codice markdown o altro testo:\n" +
                                "{\n  \"esame_riferimento\": \"" + esameScelto + "\",\n  \"argomenti_estratti\": [\n    {\n      \"nome\": \"Nome Argomento\",\n      \"descrizione\": \"Descrizione Argomento\",\n      \"test_associato\": { \"id\": \"T_AUTO_001\", \"titolo\": \"Test\", \"num_domande\": 5, \"soglia_superamento\": 3 }\n    }\n  ]\n}";

                                Gson gsonPipeline = new Gson();
                                java.util.Map<String, Object> reqMapPdf = java.util.Map.of("contents", java.util.List.of(java.util.Map.of("parts", java.util.List.of(java.util.Map.of("text", promptPdf)))));
                                
                                String urlPdf = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=" + apiGemini.trim();
                                HttpClient client = HttpClient.newHttpClient();
                                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlPdf)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(gsonPipeline.toJson(reqMapPdf))).build();
                                
                                System.out.println("🤖 Invio dati a Gemini per l'analisi e iniezione nel Grafo...");
                                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                                
                                if (response.statusCode() == 200) {
                                    JsonObject rispostaCompleta = gsonPipeline.fromJson(response.body(), JsonObject.class);
                                    String jsonInterno = rispostaCompleta.getAsJsonArray("candidates").get(0).getAsJsonObject().getAsJsonObject("content").getAsJsonArray("parts").get(0).getAsJsonObject().get("text").getAsString().replace("```json", "").replace("```", "").trim();
                                    
                                    RispostaLLM datiEstratti = gsonPipeline.fromJson(jsonInterno, RispostaLLM.class);
                                    if (datiEstratti != null && datiEstratti.getArgomentiEstratti() != null) {
                                        for (Argomento arg : datiEstratti.getArgomentiEstratti()) {
                                            Test testAssociato = arg.getTestAssociato();
                                            if (testAssociato != null) {
                                                db.creazioneTestPerArgomento(esameScelto, testAssociato, arg);
                                            }
                                        }
                                        System.out.println("✨ Processo completato! Nodi iniettati nel Grafo.");
                                    }
                                } else {
                                    System.out.println("❌ Errore HTTP: " + response.statusCode());
                                }
                            } catch (Exception e) {
                                System.out.println("❌ Errore elaborazione PDF: " + e.getMessage());
                            }
                        } else {
                            System.out.println("❌ File non valido.");
                        }
                        
                        System.out.println("\nVuoi caricare un altro PDF per questo esame? (si/no)");
                        risposta = scanner.nextLine().trim();
                    }
                }
            } else {
                System.out.println("\n❌ Nessuno studente trovato. Riprova.");
            }
        } catch (Exception e) {
            System.err.println("Errore critico globale: " + e.getMessage());
            e.printStackTrace();
        }
    } 
}