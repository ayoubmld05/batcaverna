
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;
import org.neo4j.driver.Values; 
public class DatabaseManager implements AutoCloseable {
    
    // Il "Driver" è l'oggetto che tiene aperta la porta tra Java e Neo4j
    private final Driver driver;

    // Costruttore: qui metti le credenziali del tuo database locale
    public DatabaseManager(String uri, String user, String password) {
        // Di default l'URI locale di Neo4j è "bolt://localhost:7687"
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    // Metodo obbligatorio per chiudere la porta quando finiamo
    @Override
    public void close() throws Exception {
        driver.close();
    }

    // IL NOSTRO PRIMO TEST: Leggiamo tutti gli esami dal Grafo!
    public void stampaTuttiGliEsami() {
        // Apriamo una "Sessione" (una conversazione con il DB)
        try (Session session = driver.session()) {
            
            // Scriviamo la query in linguaggio Cypher, proprio come facevi sull'interfaccia!
            String query = "MATCH (e:Esame) RETURN e.nome AS nomeEsame";
            
            // Eseguiamo la query e salviamo il risultato
            Result result = session.run(query);
            
            System.out.println("--- ESAMI TROVATI NEL DATABASE NEO4J ---");
            while (result.hasNext()) {
                Record record = result.next();
                // Estraiamo la stringa usando il nome che gli abbiamo dato nella query (nomeEsame)
                System.out.println("- " + record.get("nomeEsame").asString());
            }
        }
    }
    public Studente login_Studente(String nomeCercato, String cognomeCercato){
        //per aprire la porta di Noe4j faccio
        try (Session session = driver.session()) {
            String query = "MATCH (s:Studente {nome: $nome, cognome: $cognome}) RETURN s.nome AS nomeDB, s.cognome AS cognomeDB";
            Result result = session.run(query, Values.parameters("nome", nomeCercato, "cognome", cognomeCercato));
            if (result.hasNext()) {
                Record record = result.next();
                String nomeTrovato = record.get("nomeDB").asString();
                String cognomeTrovato = record.get("cognomeDB").asString();
                return new Studente(nomeTrovato, cognomeTrovato);//manca da mettere la matricola
            }
        }
        return null;
    }
    public List <Esame> verifica_propedeutici(String nomeEsame){
        List<Esame> prop=new ArrayList<>();
        try(Session session=driver.session()){
            String query = "MATCH (p:Esame)-[:PROPEDEUTICO]->(e:Esame {nome: $nome}) RETURN p.nome AS EsamePropedeutico, p.cfu as cfuProp, p.tasso_mortalita AS rischioMortalita";           
            Result result=session.run(query,Values.parameters("nome",nomeEsame));
            while(result.hasNext()){
                Record record = result.next();
                String nomeTrovato=record.get("EsamePropedeutico").asString();
                int cfuTrovati=record.get("cfuProp").asInt();
                float rischio = record.get("rischioMortalita").asFloat(0.0f);
                Esame trovato = new Esame(nomeTrovato, cfuTrovati, 0, false, rischio);
                prop.add(trovato);
            }
            return prop;
        }catch(Exception e) {
            System.err.println("Errore DB: " + e.getMessage());
        }
            return null;
        }  
        public Esame superato_propedeutico(String nomeEsame, String nomeStudente){
            try(Session session=driver.session()){
                String query="MATCH (s:Studente{nome:$nomeStudente})-[ass:HA_SUPERATO]->(e:Esame{nome: $nomeEsame}) RETURN e.nome as nomeProp, e.cfu as cfuProp,ass.voto as votoPreso, e.tasso_mortalita AS rischioMortalita";
                Result result=session.run(query,Values.parameters("nomeStudente",nomeStudente,"nomeEsame",nomeEsame)); 
                if(result.hasNext()){   
                    Record record=result.next();
                    String nomeTrovato=record.get("nomeProp").asString();
                    int votoTrovato=record.get("votoPreso").asInt();
                    int cfu=record.get("cfuProp").asInt();
                    float tasso_mortalita=record.get("rischioMortalita").asFloat();
                    Esame prop=new Esame(nomeTrovato,cfu ,votoTrovato,true,tasso_mortalita); 
                    return prop;
                }
            }
            return null;
        } 
        public List<Argomento> haArgomenti(String nomeEsame){
            List <Argomento> arg=new ArrayList<>();
            try(Session session=driver.session()){
                String query="MATCH (e:Esame{nome:$nomeEsame})-[:HA_ARGOMENTI]->(a:Argomento) RETURN a.nome as nomeArg, a.descrizione as descArg";
                Result result=session.run(query,Values.parameters("nomeEsame",nomeEsame));
                while(result.hasNext()){
                    Record record=result.next();
                    String nomeTrovato=record.get("nomeArg").asString();
                    String descrizioneTrovata=record.get("descArg").asString();
                    Argomento nuovo=new Argomento(nomeTrovato, descrizioneTrovata);
                    arg.add(nuovo);
                }
                return arg; 
            }catch(Exception e) {
                System.err.println("Errore DB: " + e.getMessage());
            }  
                return null; 
            }
           public Test registraEsitoTest(int esito,String nomeStudente,String ID_test){
            try(Session session=driver.session()){
                String query=
                "MATCH (s:Studente {nome: $nomeStudente}) " +
                "MATCH (t:Test {id: $idTest})-[:VALUTA]->(a:Argomento) " +
                // 1. Registriamo sempre lo svolgimento
                "MERGE (s)-[r:HA_SVOLTO]->(t) " +
                "SET r.punteggio = $esito, r.data = date() " +
                // 2. Usiamo FOREACH come un "if" per creare la lacuna solo se necessario
                // Senza interrompere il RETURN finale
                "FOREACH (_ IN CASE WHEN $esito < t.soglia_superamento THEN [1] ELSE [] END | " +
                "  MERGE (s)-[:HA_LACUNA_IN {gravità: 'alta'}]->(a) " +
                ") " +
                "RETURN t.id AS id, t.titolo AS titolo, t.num_domande AS n_domande, t.soglia_superamento AS soglia";
                Result result=session.run(query,Values.parameters("nomeStudente",nomeStudente, "idTest",ID_test,"esito", esito));
                if(result.hasNext()){
                    Record record=result.next();
                    String ID=record.get("id").asString();
                    String titolo=record.get("titolo").asString();
                    int num_domande=record.get("n_domande").asInt();
                    int soglia=record.get("soglia").asInt();
                    Test test=new Test(ID,titolo,num_domande,soglia);
                    return test;
                }
            }catch(Exception e) {
                System.err.println("Errore DB: " + e.getMessage());
            }
            return null;
           }

           public List <Test> haTest(String nomeEsame){
            List <Test> test=new ArrayList<>();
            try(Session session=driver.session()){
                String query="MATCH(e:Esame{nome:$nomeEsame})-[:HA_TEST_DI_PROVA]->(t:Test) RETURN t.titolo as nomeT, t.ID as tId, t.domande as tDom, t.soglia_superamento as tSoglia";
                Result result=session.run(query,Values.parameters("nomeEsame",nomeEsame));
                while(result.hasNext()){
                    Record record=result.next();
                    String nomeTest=record.get("nomeT").asString();
                    String idTest=record.get("tId").asString();
                    int domandeTest=record.get("tDom").asInt();
                    int sogliaTest=record.get("tSoglia").asInt();
                    Test nuovo=new Test(idTest,nomeTest,domandeTest,sogliaTest);
                    test.add(nuovo);
                }
                return test;
            }catch(Exception e) {
                System.err.println("Errore DB: " + e.getMessage());
            }
            return null;
           }
           public int haSuperatoTest(String nomeStudente, String nomeTest){
            try(Session session=driver.session()){
            String query="MATCH (s:Studente{nome:$nomeStudente})-[v:HA_SVOLTO]->(t:Test {titolo:$nomeTest}) RETURN v.punteggio as votoOttenuto";
            Result result=session.run(query,Values.parameters("nomeStudente",nomeStudente,"nomeTest",nomeTest));
            if(result.hasNext()){
                Record record=result.next();
                int punteggio=record.get("votoOttenuto").asInt();
                return punteggio;
            }
            }catch(Exception e) {
                System.err.println("Errore DB: " + e.getMessage());
            }
            return 0;
           }
         public  List <Argomento> contaLacuneAttive(String nomeStudente, String nomeEsame){
                List <Argomento> listaArgomenti=new ArrayList<>();
                try(Session session=driver.session()){
                    String query="MATCH (s:Studente{nome:$nomeStudente})-[:HA_LACUNA_IN]->(a:Argomento)<-[:HA_ARGOMENTI]-(e:Esame{nome:$nomeEsame}) RETURN a.nome as nomeArg, a.descrizione as descArg";
                    Result result=session.run(query,Values.parameters("nomeStudente",nomeStudente, "nomeEsame",nomeEsame));
                    while(result.hasNext()){
                        Record record=result.next();
                        String nomeArgomento=record.get("nomeArg").asString();
                        String descrizioneArgomento=record.get("descArg").asString();
                        Argomento nuovo=new Argomento(nomeArgomento, descrizioneArgomento);
                        listaArgomenti.add(nuovo);   
                    }
                    return listaArgomenti;     
                }catch(Exception e) {
                System.err.println("Errore DB: " + e.getMessage());
            }
            return null;
           }
           // si occupi di prendere l'esame, il test e l'argomento e creare la struttura nel grafo
        public void creazioneTestPerArgomento(String nomeEsame, Test test, Argomento argomento){
            String query = "MERGE (e:Esame{nome:$nomeEsame}) " +  // <-- nota lo spazio finale
                        "MERGE (t:Test{id:$idTest}) " + 
                        "MERGE (a:Argomento{nome:$nomeArgomento}) " +
                        "SET a.descrizione = $descrizione " +
                        "SET t.titolo = $titoloTest, t.num_domande = $numDomande, t.soglia_superamento = $soglia " +
                        "MERGE (e)-[:CONTIENE_ARGOMENTO]->(a) " +
                        "MERGE (a)-[:HA_TEST_DI_PROVA]->(t)";
                        
                try(Session session = driver.session()) {
                session.run(query, Values.parameters(
                    "nomeEsame", nomeEsame,
                    "nomeArgomento", argomento.getNome(),
                    "descrizione", argomento.getDescrizione(),
                    "titoloTest", test.getTitolo(),
                    "idTest", test.getID(),
                    "numDomande", test.getNum_domande(),
                    "soglia", test.getSoglia_superamento()
                ));
            } catch(Exception e) {
                System.err.println("Errore DB in creazioneTest: " + e.getMessage());
            }
        }
        public double getMediaStudente(String nomeStudente) {
            int count = 0;
            double sommaVoti = 0.0;
            try (Session session = driver.session()) {
                // toInteger() salva la situazione se il voto è stato salvato come stringa
                String query = "MATCH (s:Studente{nome:$nomeStudente})-[a:HA_SUPERATO]->(e:Esame) RETURN toInteger(a.voto) AS votoEsame";
                Result result = session.run(query, Values.parameters("nomeStudente", nomeStudente));
                
                while (result.hasNext()) {
                    Record record = result.next();
                    if (!record.get("votoEsame").isNull()) {
                        sommaVoti += record.get("votoEsame").asDouble();
                        count++;
                    }
                }
            } catch (Exception e) {
                // Ora il messaggio di errore è accurato
                System.err.println("Errore DB in getMediaStudente: " + e.getMessage());
            }
            // Evitiamo l'eccezione matematica di divisione per zero se non ci sono esami
            return count == 0 ? 0.0 : sommaVoti / count;
        }
        public int getCfuStudente(String nomeStudente) {
            int cfu = 0;
            try (Session session = driver.session()) {
                // Aggiunti i DUE PUNTI prima di HA_SUPERATO e toInteger() sul cfu
                String query = "MATCH (s:Studente{nome:$nomeStudente})-[:HA_SUPERATO]->(e:Esame) RETURN toInteger(e.cfu) as cfuEsame";
                Result result = session.run(query, Values.parameters("nomeStudente", nomeStudente));
                
                while (result.hasNext()) {
                    Record record = result.next();
                    if (!record.get("cfuEsame").isNull()) {
                        cfu += record.get("cfuEsame").asInt();
                    }
                }
            } catch (Exception e) {
                // Ora il messaggio di errore è accurato
                System.err.println("Errore DB in getCfuStudente: " + e.getMessage());
            }
            return cfu;
        }
        
        public List <Esame> getListaEsamiNonSuperati(String nomeStudente){
            List <Esame> lista=new ArrayList<>();
            try(Session session=driver.session()){
                String query="MATCH (s:Studente{nome:$nomeStudente}) -[v:HA_FALLITO]->(e:Esame) WHERE v.voto<18 RETURN e.nome AS nEsame, e.descrizione AS dEsame, e.tasso_mortalita AS tEsame, e.cfu AS cEsame, v.voto AS vEsame ";
                Result result=session.run(query,Values.parameters("nomeStudente",nomeStudente));
                while(result.hasNext()){
                    Record record=result.next();
                    String nomeEsame=record.get("nEsame").asString();
                    String descrizioneEsame=record.get("dEsame").asString();
                    int tasso_mortalitaEsame=record.get("tEsame").asInt();
                    int cfuEsame=record.get("cEsame").asInt();
                    int votoEsame=record.get("vEsame").asInt();
                    Esame nuovo=new Esame(nomeEsame,cfuEsame,votoEsame,false,tasso_mortalitaEsame);
                    lista.add(nuovo);
                }
            }catch(Exception e) {
                System.err.println("Errore DB in creazioneTest: " + e.getMessage());
            }
            return lista;
        }
        
        //devo andare a fare le ultime query
        public List<Consiglio> getListaConsigli(String nomeStudente) {
            List<Consiglio> lista = new ArrayList<>();
            try (Session session = driver.session()) {
                // Naviga dal nodo Studente fino al Consiglio, ordinandoli dal più recente
                String query = "MATCH (s:Studente {nome:$nomeStudente})-[:HA_EFFETTUATO]->(i:Interazione)-[:HA_GENERATO]->(c:Consiglio) " +
                               "RETURN c.idInterazione AS id, c.timestamp AS time, c.testoGenerato AS testo, c.categoriaAzione AS categoria " +
                               "ORDER BY c.timestamp DESC LIMIT 5";
                               
                Result result = session.run(query, Values.parameters("nomeStudente", nomeStudente));
                
                while (result.hasNext()) {
                    Record record = result.next();
                    String id = record.get("id").asString();
                    long timestamp = record.get("time").asLong();
                    String testo = record.get("testo").asString();
                    String categoria = record.get("categoria").asString();
                    
                    Consiglio consiglio = new Consiglio(id, timestamp, testo, categoria);
                    lista.add(consiglio);
                }
            } catch (Exception e) {
                System.err.println("Errore DB recupero consigli: " + e.getMessage());
            }
            return lista;
        }
        // File: DatabaseManager.java (Aggiungi questo metodo prima della chiusura dell'ultima parentesi graffa)

public void salvaNuovoConsiglio(String nomeStudente, String testoConsiglio) {
    try (Session session = driver.session()) {
        // Creiamo il nodo e lo colleghiamo allo studente in una singola query atomica (Altamente Efficace)
        String query = 
            "MATCH (s:Studente {nome: $nomeStudente}) " +
            "CREATE (c:Interazione:Consiglio { " +
            "   idInterazione: randomUUID(), " +
            "   timestamp: timestamp(), " +
            "   testoGenerato: $testo, " +
            "   categoriaAzione: 'SALUTO_PROATTIVO', " +
            "   statoEsecuzione: 'COMPLETED' " +
            "}) " +
            "CREATE (s)-[:HA_EFFETTUATO]->(c)";
            
        session.run(query, Values.parameters(
            "nomeStudente", nomeStudente,
            "testo", testoConsiglio
        ));
    } catch (Exception e) {
        System.err.println("Errore durante il salvataggio della memoria di Alfred: " + e.getMessage());
    }
}
public void calcolaRischioAvanzato(String nomeStudente, String nomeEsame) {
    String query = 
        "MATCH (s:Studente {nome: $nomeStudente}) " +
        "MATCH (e:Esame {nome: $nomeEsame}) " +
        "OPTIONAL MATCH (s)-[:HA_LACUNA_IN]->(a:Argomento)<-[:CONTIENE_ARGOMENTO]-(e) " +
        "WITH e, count(a) AS numeroLacune, collect(a.nome) AS argomentiLacunosi " +
        "WITH e.nome AS nomeEsame, numeroLacune, argomentiLacunosi, e.tasso_mortalita AS mortalita, " +
        "(numeroLacune * e.tasso_mortalita) AS rischioCalcolato " +
        "RETURN nomeEsame, numeroLacune, argomentiLacunosi, mortalita, rischioCalcolato";

    try (Session session = driver.session()) {
        Result result = session.run(query, Values.parameters(
            "nomeStudente", nomeStudente, 
            "nomeEsame", nomeEsame
        ));

        if (result.hasNext()) {
            Record record = result.next();
            int numeroLacune = record.get("numeroLacune").asInt();
            float rischio = record.get("rischioCalcolato").asFloat();
            List<Object> argomenti = record.get("argomentiLacunosi").asList();

            System.out.println("--- REPORT DI RISCHIO PREDITTIVO ---");
            if (rischio > 2.0f) {
                System.out.println("⚠️ ALLARME RISCHIO ACCADEMICO ⚠️");
                System.out.println("Rischio calcolato: " + rischio + " (Soglia superata)");
                System.out.println("Trovate " + numeroLacune + " lacune critiche sugli argomenti:");
                for (Object arg : argomenti) {
                    System.out.println(" - " + arg.toString());
                }
            } else {
                System.out.println("✅ Rischio calcolato: " + rischio + ". Nessuna lacuna bloccante. Puoi procedere.");
            }
        }
    } catch (Exception e) {
        System.err.println("Errore durante il calcolo topologico del rischio: " + e.getMessage());
    }
}    
public void caricaAppunti(String matricola, String nomeAppunto,String id_appunto,LocalDate data, boolean verificato){
    try(Session session=driver.session()){
       String query="MATCH (s:Studente {matricola: $matricolaStudente})\n" +
                      "CREATE (s)-[:HA_CARICATO]->(ap:Appunto {\n" + 
                      "    id_appunto: $idAppunto, \n" + //
                      "    titolo: $titoloAppunto, \n" + //
                      "    data_caricamento: $dataCaricamento\n" + //
                      " verificato:$Verificato "+
                      "})\n" + //
                      "";
                      session.run(query,Values.parameters("matricolaStudente",matricola ,"id_appunto",id_appunto,"dataCaricamento",data, "Verificato",verificato));
    }catch (Exception e) {
        System.err.println("Errore durante il salvataggio della memoria di Alfred: " + e.getMessage());
    }
}

//chiamata solo se il flag dello studente è true
public List<Esame> getPianoDiStudi(String matricola){
    List<Esame> pianoCarriera = new ArrayList<>();
    String query="MATCH (s:Studente{matricola:$matricola })-[:HA_IN_PIANO]->(e:Esame) "+
    "RETURN e.nome AS nomeEsame, e.cfu AS cfu, e.tasso_mortalita AS mortalita, e.voto AS voto";
    try(Session session=driver.session()){
        Result result=session.run(query,Values.parameters("matricola",matricola));
        while(result.hasNext()){
            Record record=result.next();
            String nome=record.get("nomeEsame").asString();
            int cfu=record.get("cfu").asInt();
            float tasso=record.get("mortalità").asFloat();
            int voto=record.get("voto").asInt();
            boolean sup=false;
            if(voto>=18){
                sup=true;
            }
            Esame e=new Esame(nome, cfu, voto, sup, tasso);
            pianoCarriera.add(e);
        }
    }catch(Exception e){
        System.err.println("Errore durante la presa del piano di studi: " + e.getMessage());
    }
    return pianoCarriera;
}

}

