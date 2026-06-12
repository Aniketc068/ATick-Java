// ATick Java example  (Python: 09_verify_certificate.py) — pre-sign certificate checks.
// verify_expiry / verify_crl / verify_ocsp (or verify:true for all). Signing is REFUSED if a check
// fails — so an invalid certificate never produces a signature.
import io.github.aniketc068.atick.Atick;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Verify {
    static byte[] read(String n) throws Exception { return Files.readAllBytes(Paths.get("samples", n)); }
    static String now() { return new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.ENGLISH).format(new Date()); }
    static void save(String n, byte[] b) throws Exception {
        Files.createDirectories(Paths.get("signed"));
        Files.write(Paths.get("signed", n), b);
        System.out.println("  " + n + " (" + b.length + " bytes)");
    }
    public static void main(String[] a) throws Exception {
        byte[] pfx = read("ABC12.pfx"), pdf = read("blank.pdf");
        try {
            byte[] signed = Atick.signPfx(pdf, pfx, "{\"password\":\"ABC12\",\"cn\":\"DS TEST CERTIFICATE 06\","
                + "\"reason\":\"Approved\",\"date\":\"" + now() + "\",\"green_tick\":true,\"page\":1,"
                + "\"rect\":[300,55,575,175],\"pades\":true,\"verify_expiry\":true}");  // reject if expired
            save("09_verified.pdf", signed);
        } catch (Atick.AtickException e) {
            System.out.println("  verify rejected this certificate: " + e.getMessage());
        }
    }
}
