import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;


public class PinjamRuangan extends Ruangan implements Peminjaman {
    private String namaPeminjam;
    private Date waktuPinjam;
    private Date waktuKembali;

    public void setKapasitas(int kapasitas){
        super.kapasitas = kapasitas;
    }

    public void setNama(String nama){
        super.nama = nama;
    }
    
    public void setNamaPeminjam(String namaPeminjam){
        this.namaPeminjam = namaPeminjam;
    }

    public String getNamaPeminjam(){
        return namaPeminjam;
    }

    // Constructor
    public PinjamRuangan(String nama, int kapasitas, String kodeRuangan) {
        super(nama, kapasitas, kodeRuangan);
        this.namaPeminjam = "";
        this.waktuPinjam = null;
        this.waktuKembali = null;
    }

    // Implementasi metode dari interface Peminjaman
    @Override
    public void Pinjam(String namaPeminjam) {
        this.namaPeminjam = namaPeminjam;

        // Mendapatkan waktu saat ini
        this.waktuPinjam = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        SimpleDateFormat waktu = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        System.out.println("Ruangan " + getNama() + " dipinjam oleh " + namaPeminjam + " pada " + waktu.format(waktuPinjam));
    }

    @Override
    public void pengembalian(String namaPeminjam){

        // Mendapatkan waktu saat ini
        this.waktuKembali = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        System.out.println("Ruangan " + getNama() + " Yang dipinjam Oleh : " + namaPeminjam + " dikembalikan pada " + sdf.format(waktuKembali));
    }

    public long hitungDurasiPinjam() {
        if (waktuPinjam == null) {
            System.out.println("Waktu peminjaman tidak tersedia.");
            return 0; // Atau bisa juga throw exception
        }

        // Mendapatkan waktu sekarang
        Date waktuSekarang = new Date();

        // Menghitung durasi pinjam dalam milidetik
        long durasiPinjamMillis = waktuSekarang.getTime() - waktuPinjam.getTime();

        // Mengonversi durasi dari milidetik ke menit
        long durasiPinjamMenit = durasiPinjamMillis / (1000 * 60);

        return durasiPinjamMenit; // Mengembalikan durasi dalam menit
    }


    public Date getWaktuPinjam() {
        return waktuPinjam;
    }

    public void setWaktuPinjam(Date waktuPinjam) {
        this.waktuPinjam = waktuPinjam;
    }
}