// ATick Java example  (Python: 18_date_and_pem.py, PEM part) — sign with a .pem key+cert (not .pfx).
import io.github.aniketc068.atick.Atick;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Pem {
    static byte[] read(String n) throws Exception { return Files.readAllBytes(Paths.get("samples", n)); }
    static String now() { return new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.ENGLISH).format(new Date()); }
    static void save(String n, byte[] b) throws Exception {
        Files.createDirectories(Paths.get("signed"));
        Files.write(Paths.get("signed", n), b);
        System.out.println("  " + n + " (" + b.length + " bytes)");
    }
    public static void main(String[] a) throws Exception {
        byte[] pem = read("ABC12.pem"), pdf = read("blank.pdf");   // a .pem holding the key + cert
        byte[] signed = Atick.signPfx(pdf, pem, "{\"password\":\"ABC12\",\"cn\":\"DS TEST CERTIFICATE 06\","
            + "\"reason\":\"Approved\",\"date\":\"" + now() + "\",\"green_tick\":true,\"page\":1,\"rect\":[300,55,575,175],\"pades\":true}");
        save("18_sign_pem.pdf", signed);
    }
}
