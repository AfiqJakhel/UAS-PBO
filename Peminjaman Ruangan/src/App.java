import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class App {
    private ArrayList<PinjamRuangan> daftarRuangan;

    public App(){
        daftarRuangan = new ArrayList<>();
        loadRuanganFromDatabase();
    }

    private void loadRuanganFromDatabase() {
        String sql = "SELECT * FROM ruangan"; // Ganti dengan nama tabel Anda
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String nama = rs.getString("nama_ruangan");
                int kapasitas = rs.getInt("kapasitas");
                String kodeRuangan = rs.getString("kode_ruangan");
                boolean tersedia = rs.getBoolean("tersedia");
                String namaPeminjam = rs.getString("nama_peminjam");
                PinjamRuangan ruangan = new PinjamRuangan(nama, kapasitas, kodeRuangan);
                ruangan.setTersedia(tersedia);
                ruangan.setNamaPeminjam(namaPeminjam); // Set nama peminjam
                daftarRuangan.add(ruangan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void tambahRuangan(PinjamRuangan ruangan) {
        String sql = "INSERT INTO ruangan (nama_ruangan, kapasitas, kode_ruangan, tersedia) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ruangan.getNama());
            pstmt.setInt(2, ruangan.getKapasitas());
            pstmt.setString(3, ruangan.getKodeRuangan());
            pstmt.setBoolean(4, ruangan.isTersedia());
            pstmt.executeUpdate();
            daftarRuangan.add(ruangan); // Tambahkan ke daftar lokal
            System.out.println("Ruangan " + ruangan.getNama() + " berhasil ditambahkan.");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void pinjamRuangan(String kodeRuangan, String namaPeminjam) {
        for (PinjamRuangan ruangan : daftarRuangan) {
            if (ruangan.getKodeRuangan().equalsIgnoreCase(kodeRuangan)) {
                System.out.println("Memeriksa ruangan: " + ruangan.getNama());
                if (ruangan.isTersedia()) {
                    // Jika ruangan tersedia, set status dan waktu pinjam
                    ruangan.setTersedia(false);
                    ruangan.setNamaPeminjam(namaPeminjam);
                    ruangan.setWaktuPinjam(new Date()); // Set waktu pinjam ke waktu saat ini
    
                    // Simpan ke database dengan UPDATE
                    String sqlUpdate = "UPDATE ruangan SET nama_peminjam = ?, waktu_pinjam = ?, tersedia = ? WHERE kode_ruangan = ?";
                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
                        pstmt.setString(1, namaPeminjam);
                        pstmt.setTimestamp(2, new java.sql.Timestamp(ruangan.getWaktuPinjam().getTime())); // Konversi ke Timestamp
                        pstmt.setBoolean(3, false);
                        pstmt.setString(4, ruangan.getKodeRuangan());
                        int rowsAffected = pstmt.executeUpdate();
                        
                        if (rowsAffected > 0) {
                            System.out.println("Ruangan " + ruangan.getNama() + " berhasil dipinjam.");
                        } else {
                            System.out.println("Gagal memperbarui peminjaman. Ruangan mungkin tidak ditemukan.");
                        }
                    } catch (SQLException e) {
                        System.out.println("Error saat memperbarui peminjaman: " + e.getMessage());
                    }
                    return;
                } else {
                    System.out.println("Ruangan " + ruangan.getNama() + " tidak tersedia.");
                    return;
                }
            }
        }
        System.out.println("Ruangan dengan kode " + kodeRuangan + " tidak ditemukan.");
    }

    public void kembalikanRuangan(String kodeRuangan, String namaPeminjam) {
        // Memeriksa apakah ruangan dengan kode tersebut ada dan dipinjam
        String sqlCheck = "SELECT nama_peminjam FROM ruangan WHERE kode_ruangan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmtCheck = conn.prepareStatement(sqlCheck)) {
            pstmtCheck.setString(1, kodeRuangan);
            ResultSet rs = pstmtCheck.executeQuery();
            
            if (rs.next()) {
                String peminjamTerdaftar = rs.getString("nama_peminjam");
                if (peminjamTerdaftar != null && peminjamTerdaftar.equalsIgnoreCase(namaPeminjam)) {
                    // Jika nama peminjam cocok, lanjutkan dengan pengembalian
                    System.out.println("Memproses pengembalian untuk ruangan: " + kodeRuangan);

                    
                    // Mengubah status ruangan menjadi tersedia
                    String sqlUpdate = "UPDATE ruangan SET nama_peminjam = NULL, waktu_pinjam = ?, tersedia = TRUE WHERE kode_ruangan = ?";
                    try (PreparedStatement pstmtUpdate = conn.prepareStatement(sqlUpdate)) {
                        // Mencari objek ruangan dari daftarRuangan
                        PinjamRuangan ruangan = null;
                        for (PinjamRuangan r : daftarRuangan) {
                            if (r.getKodeRuangan().equalsIgnoreCase(kodeRuangan)) {
                                ruangan = r;
                                break;
                            }
                        }
    
                        // Cek apakah objek ruangan ditemukan
                        if (ruangan != null) {
                            // Cek apakah waktuPinjam null
                            if (ruangan.getWaktuPinjam() == null) {
                                pstmtUpdate.setNull(1, java.sql.Types.TIMESTAMP); // Mengatur nilai NULL
                            } else {
                                pstmtUpdate.setTimestamp(1, new java.sql.Timestamp(ruangan.getWaktuPinjam().getTime())); // Mengatur nilai waktu
                            }
                            pstmtUpdate.setString(2, kodeRuangan); // Mengatur kode ruangan
    
                            int rowsAffected = pstmtUpdate.executeUpdate();
                            
                            if (rowsAffected > 0) {
                                ruangan.setTersedia(true); // Set local status to available
                                ruangan.setNamaPeminjam(null); // Clear the borrower name
                                ruangan.setWaktuPinjam(null); // Clear the borrow time
                                System.out.println("Ruangan " + kodeRuangan + " berhasil dikembalikan.");
                            } else {
                                System.out.println("Gagal mengembalikan ruangan. Ruangan mungkin tidak ditemukan di database.");
                            }
                        } else {
                            System.out.println("Ruangan dengan kode " + kodeRuangan + " tidak ditemukan dalam daftar.");
                        }
                    }
                } else {
                    System.out.println("Nama peminjam tidak cocok. Ruangan " + kodeRuangan + " dipinjam oleh " + peminjamTerdaftar + ".");
                }
            } else {
                System.out.println("Ruangan dengan kode " + kodeRuangan + " tidak ditemukan atau belum dipinjam.");
            }
        } catch (SQLException e) {
            System.out.println("Error saat memproses pengembalian: " + e.getMessage());
        }
    }

    public void tampilkanRuangan() {
        String sql = "SELECT kode_ruangan, nama_ruangan, kapasitas, tersedia, nama_peminjam, waktu_pinjam FROM ruangan";
        for (PinjamRuangan ruangan : daftarRuangan) { 
            try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
                
                if (!rs.isBeforeFirst()) { // Cek jika tidak ada hasil
                    System.out.println("Tidak Ada Ruangan Yang Tersedia");
                    return;
                }
                
                while (rs.next()) {
                    String kodeRuangan = rs.getString("kode_ruangan");
                    String namaRuangan = rs.getString("nama_ruangan");
                    int kapasitas = rs.getInt("kapasitas");
                    boolean tersedia = rs.getBoolean("tersedia");
                    String namaPeminjam = rs.getString("nama_peminjam");
                    Date waktuPinjam = rs.getTimestamp("waktu_pinjam");

                    StringBuilder output = new StringBuilder();
                output.append("Kode Ruangan: ").append(kodeRuangan)
                    .append(" | Nama Ruangan: ").append(namaRuangan)
                    .append(" | Kapasitas: ").append(kapasitas)
                    .append(" | Tersedia: ").append(tersedia ? "Ya" : "Tidak");

                // Only display the borrower's name and time if the room is not available
                if (!tersedia) {
                    // Check if namaPeminjam is null and handle it
                    if (namaPeminjam != null) {
                        output.append(" | Nama Peminjam: ").append(namaPeminjam);
                    } else {
                        output.append(" | Nama Peminjam: Tidak ada");
                    }

                    // Check if waktuPinjam is not null and append it
                    if (waktuPinjam != null) {
                        output.append(" | Waktu Pinjam: ").append(waktuPinjam);

                        long durasi = ruangan.hitungDurasiPinjam();
                        output.append(" | Durasi Peminjaman: ").append(durasi); output.append(" Menit");
                    }
                }

                // Print the complete output in one line
                System.out.println(output.toString());
                }
            } catch (SQLException e) {
                System.out.println("Error saat mengambil data ruangan: " + e.getMessage());
            }
        }
    }

    public void updateRuangan(Scanner scanner, String kodeRuangan) {
        for (PinjamRuangan ruangan : daftarRuangan) {
            if (ruangan.getKodeRuangan().equalsIgnoreCase(kodeRuangan)) {
                System.out.println("Mengupdate Ruangan: " + ruangan.getNama());
                System.out.print("Masukkan nama baru (tekan Enter untuk tidak mengubah): ");
                String namaBaru = scanner.nextLine();
                if (!namaBaru.isEmpty()) {
                    ruangan.setNama(namaBaru);
                }
    
                System.out.print("Masukkan kapasitas baru (tekan Enter untuk tidak mengubah): ");
                String kapasitasBaru = scanner.nextLine();
                if (!kapasitasBaru.isEmpty()) {
                    ruangan.setKapasitas(Integer.parseInt(kapasitasBaru));
                }
    
                System.out.print("Apakah ruangan ini tersedia? (true/false, tekan Enter untuk tidak mengubah): ");
                String tersediaBaru = scanner.nextLine();
                if (!tersediaBaru.isEmpty()) {
                    ruangan.setTersedia(Boolean.parseBoolean(tersediaBaru));
                }
    
                // Update database
                String sql = "UPDATE ruangan SET nama_ruangan = ?, kapasitas = ?, tersedia = ? WHERE kode_ruangan = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, ruangan.getNama());
                    pstmt.setInt(2, ruangan.getKapasitas());
                    pstmt.setBoolean(3, ruangan.isTersedia());
                    pstmt.setString(4, kodeRuangan);
                    pstmt.executeUpdate();
                    System.out.println("Ruangan berhasil diperbarui.");
                } catch (SQLException e) {
                    System.out.println("Error: " + e.getMessage());
                }
                return;
            }
        }
        System.out.println("Ruangan dengan kode " + kodeRuangan + " tidak ditemukan.");
    }

    public void hapusRuangan(String kodeRuangan) {
        // Mencari ruangan dalam daftar lokal
        for (int i = 0; i < daftarRuangan.size(); i++) {
            PinjamRuangan ruangan = daftarRuangan.get(i);
            if (ruangan.getKodeRuangan().equalsIgnoreCase(kodeRuangan)) {
                // Menghapus dari database
                String sql = "DELETE FROM ruangan WHERE kode_ruangan = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, kodeRuangan);
                    pstmt.executeUpdate();
                    System.out.println("Ruangan " + ruangan.getNama() + " berhasil dihapus dari database.");
                } catch (SQLException e) {
                    System.out.println("Error: " + e.getMessage());
                }
    
                // Menghapus dari daftar lokal
                daftarRuangan.remove(i);
                System.out.println("Ruangan " + ruangan.getNama() + " berhasil dihapus dari daftar.");
                return;
            }
        }
        System.out.println("Ruangan dengan kode " + kodeRuangan + " tidak ditemukan.");
    }

    public static void main(String[] args) {
        App gedung = new App();
        Scanner scanner = new Scanner(System.in);
        int pilihan;

        do {
            System.out.println("\nMenu:");
            System.out.println("1. Tambah Ruangan");
            System.out.println("2. Tampilkan Ruangan");
            System.out.println("3. Pinjam Ruangan");
            System.out.println("4. Kembalikan Ruangan");
            System.out.println("5. Update Ruangan");
            System.out.println("6. Hapus Ruangan");
            System.out.println("7. Keluar");
            System.out.print("Pilih: ");
            pilihan = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (pilihan) {
                case 1:
                    System.out.print("Masukkan nama ruangan: ");
                    String nama = scanner.nextLine();
                    System.out.print("Masukkan kapasitas: ");
                    int kapasitas = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    System.out.print("Masukkan kode ruangan: ");
                    String kodeRuangan = scanner.nextLine();

                    PinjamRuangan ruanganBaru = new PinjamRuangan(nama, kapasitas, kodeRuangan);
                    gedung.tambahRuangan(ruanganBaru);
                    break;
                case 2:
                    gedung.tampilkanRuangan();
                    break;
                case 3:
                    System.out.print("Masukkan kode ruangan yang ingin dipinjam: ");
                    String kodePinjam = scanner.nextLine();
                    System.out.print("Nama Peminjam: ");
                    String namaPeminjam = scanner.nextLine();
                    gedung.pinjamRuangan(kodePinjam, namaPeminjam);
                    break;
                case 4:
                    System.out.print("Masukkan kode ruangan yang ingin dikembalikan: ");
                    String kodeKembali = scanner.nextLine();
                    System.out.print("Nama Peminjam: ");
                    String namaKembali = scanner.nextLine();
                    gedung.kembalikanRuangan(kodeKembali, namaKembali);
                    break;
                case 5:
                    System.out.print("Masukkan kode ruangan yang ingin diupdate: ");
                    String kodeGedung = scanner.nextLine();
                    gedung.updateRuangan(scanner, kodeGedung);
                    break;
                case 6:
                    System.out.print("Masukkan kode ruangan yang ingin dihapus: ");
                    String kodeHapus = scanner.nextLine();
                    gedung.hapusRuangan(kodeHapus);
                    break;
                case 7:
                    System.out.println("Keluar dari program.");
                    break;
                default:
                    System.out.println("Pilihan tidak valid. Silakan coba lagi.");
            }
        } while (pilihan != 7);
        scanner.close();
    }
}