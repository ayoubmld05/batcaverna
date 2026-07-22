public abstract class Interazione {
    protected String idInterazione; 
    protected long timestamp;       
    public Interazione(String idInterazione, long timestamp) {
        this.idInterazione = idInterazione;
        this.timestamp = timestamp;
    }
    
    public String getIdInterazione() {
        return idInterazione;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public void setIdInterazione(String idInterazione) {
        this.idInterazione = idInterazione;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }     
}