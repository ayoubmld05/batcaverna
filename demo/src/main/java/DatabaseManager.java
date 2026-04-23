
import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
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
                return new Studente(nomeTrovato, cognomeTrovato);
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
           List <Argomento> contaLacuneAttive(String nomeStudente, String nomeEsame){
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
        }

