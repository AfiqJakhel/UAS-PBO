public class Ruangan {
    protected String nama;
    protected int kapasitas;
    protected boolean tersedia;
    protected String kodeRuangan;
    
    //Constructor
    public Ruangan(String nama, int kapasitas, String kodeRuangan){
        this.nama = nama;
        this.kapasitas = kapasitas;
        this.kodeRuangan = kodeRuangan;
        this.tersedia = true;
    }

    public String getNama(){
        return nama;
    }

    public int getKapasitas(){
        return kapasitas;
    }

    public boolean isTersedia(){
        return tersedia;
    }

    public void setTersedia(boolean tersedia){
        this.tersedia = tersedia;
    }

    public String getKodeRuangan(){
        return kodeRuangan;
    }
}
