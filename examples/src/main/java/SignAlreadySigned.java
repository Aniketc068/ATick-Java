// ATick Java example  (Python: sign_already_signed.py) — add a NEW signature to an ALREADY-signed
// PDF. Incremental update: the existing signature(s) keep their byte ranges and stay valid; the new
// one is valid too. Just sign the signed PDF with a NEW, unique field_name.
import io.github.aniketc068.atick.Atick;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class SignAlreadySigned {
    static byte[] read(String n) throws Exception { return Files.readAllBytes(Paths.get("samples", n)); }
    static String now() { return new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.ENGLISH).format(new Date()); }
    static void save(String n, byte[] b) throws Exception {
        Files.createDirectories(Paths.get("signed"));
        Files.write(Paths.get("signed", n), b);
        System.out.println("  " + n + " (" + b.length + " bytes)");
    }
    public static void main(String[] a) throws Exception {
        byte[] pfx = read("ABC12.pfx"), alreadySigned = read("signed.pdf");   // an already-signed input
        byte[] resigned = Atick.signPfx(alreadySigned, pfx, "{\"password\":\"ABC12\",\"cn\":\"DS TEST CERTIFICATE 06\","
            + "\"reason\":\"Counter-signed\",\"date\":\"" + now() + "\",\"field_name\":\"Atick_2\","
            + "\"page\":1,\"rect\":[40,640,260,750],\"pades\":true}");
        save("added_signature.pdf", resigned);
    }
}
