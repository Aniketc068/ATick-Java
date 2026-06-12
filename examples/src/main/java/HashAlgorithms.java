// ATick Java example  (Python: 13_hash_algorithms.py) — SHA-256 / SHA-384 / SHA-512.
import io.github.aniketc068.atick.Atick;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class HashAlgorithms {
    static byte[] read(String n) throws Exception { return Files.readAllBytes(Paths.get("samples", n)); }
    static String now() { return new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.ENGLISH).format(new Date()); }
    static void save(String n, byte[] b) throws Exception {
        Files.createDirectories(Paths.get("signed"));
        Files.write(Paths.get("signed", n), b);
        System.out.println("  " + n + " (" + b.length + " bytes)");
    }
    public static void main(String[] a) throws Exception {
        byte[] pfx = read("ABC12.pfx"), pdf1 = read("blank.pdf");
        String base = "\"password\":\"ABC12\",\"cn\":\"DS TEST CERTIFICATE 06\",\"reason\":\"Approved\",\"date\":\"" + now()
            + "\",\"page\":1,\"rect\":[300,55,575,175],\"pades\":true";
        for (String h : new String[] { "sha256", "sha384", "sha512" })
            save("13_hash_" + h + ".pdf", Atick.signPfx(pdf1, pfx, "{" + base + ",\"hash_algo\":\"" + h + "\"}"));
    }
}
