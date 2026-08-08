#  Alfred: The GraphRAG Academic Tutor

Alfred è un Tutor Accademico basato sull'Intelligenza Artificiale, progettato per guidare gli studenti universitari nel loro percorso di studi. Sfruttando un'architettura **GraphRAG (Retrieval-Augmented Generation basata su Grafo)**, Alfred non si limita a rispondere a domande, ma analizza l'intera carriera dello studente per elaborare strategie predittive, consigliare percorsi di studio ottimali e generare test di autovalutazione dinamici.

##  Architettura e Tecnologie

Il cuore del sistema unisce l'affidabilità topologica dei database a grafo con le capacità di ragionamento avanzato dei Large Language Models (LLM).

* **Backend:** Java 17+
* **Graph Database:** Neo4j (Cypher Query Language)
* **LLM Engine:** Google Gemini (Modello 3.5 Flash) tramite API REST
* **Document Parsing:** Apache PDFBox per l'estrazione di testo da PDF (Manifesti degli studi, appunti)
* **Data Binding:** Google Gson per la mappatura deterministica dei JSON

## Funzionalità Principali (Core Features)

* **Analisi Predittiva del Rischio:** Attraverso query Cypher, il sistema valuta il "Tasso di Mortalità" degli esami e le propedeuticità, avvisando lo studente di potenziali blocchi accademici (Colli di bottiglia).
* **GraphRAG e Memoria Episodica:** Alfred ricorda l'esito dei test passati, le lacune dello studente e i consigli forniti nelle sessioni precedenti, garantendo un'interazione continua e personalizzata.
* **Ingegneria Didattica Automatica:** Estrae argomenti, CFU e vincoli dai PDF ufficiali del regolamento didattico per popolare automaticamente il Database a Grafo.
* **Dynamic Testing:** Genera e somministra in tempo reale test a risposta multipla tramite LLM, corregge le risposte, fornisce spiegazioni dettagliate e inietta i risultati come nuovi nodi nel Grafo per tracciare i progressi.

##  Flusso Logico del Grafo

Il database Neo4j è strutturato per mappare sia la teoria che l'esperienza utente:
1. Nodi principali: `Studente`, `PianoCarriera`, `Esame`, `Argomento`, `Test`, `Consiglio`.
2. Relazioni chiave: `POSSIEDE`, `CONTIENE_ESAME`, `RICHIEDE_PROPEDEUTICITA`, `HA_SVOLTO`, `HA_LACUNA_IN`.

Quando l'utente fallisce un test, il sistema inietta automaticamente una relazione di "lacuna", permettendo ad Alfred di ricalcolare dinamicamente la strategia di studio futura.

## 🛠️Installazione e Setup (Local PoC)

1. **Prerequisiti:** 
   * Java Development Kit (JDK) installato.
   * Neo4j Desktop (o Neo4j Aura) attivo sulla porta `bolt://localhost:7687`.
2. **Clonare la repository:**
   `git clone https://github.com/ayoubmld05/batcaverna`
3. **Variabili d'ambiente:** 
   * Creare un file `config.properties` nella root del progetto.
   * Inserire la propria API Key di Google Gemini: `API_KEY=la_tua_chiave`
4. **Librerie esterne (JARs):** Assicurarsi di includere nel classpath `neo4j-java-driver`, `gson`, e `pdfbox`.
5. **Esecuzione:** Compilare ed eseguire la classe `Main.java`.

---
*Progetto sviluppato come Proof of Concept per l'integrazione di Graph Database e Intelligenze Artificiali Generative in ambito EdTech.*