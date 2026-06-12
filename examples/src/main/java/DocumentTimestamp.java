// ATick Java example  (Python: document_timestamp.py) — sign (B-LT), then add an archive
// DocTimeStamp over the whole document (-> PAdES-B-LTA).
import io.github.aniketc068.atick.Atick;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class DocumentTimestamp {
    static byte[] read(String n) throws Exception { return Files.readAllBytes(Paths.get("samples", n)); }
    static String now() { return new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.ENGLISH).format(new Date()); }
    static void save(String n, byte[] b) throws Exception {
        Files.createDirectories(Paths.get("signed"));
        Files.write(Paths.get("signed", n), b);
        System.out.println("  " + n + " (" + b.length + " bytes)");
    }
    public static void main(String[] a) throws Exception {
        byte[] pfx = read("ABC12.pfx"), pdf = read("blank.pdf");
        // 1) sign B-LT
        byte[] signed = Atick.signPfx(pdf, pfx, "{\"password\":\"ABC12\",\"cn\":\"DS TEST CERTIFICATE 06\","
            + "\"reason\":\"Approved\",\"date\":\"" + now() + "\",\"page\":1,\"rect\":[300,55,575,175],"
            + "\"pades\":true,\"timestamp\":true,\"ltv\":true}");
        // 2) add a standalone archive DocTimeStamp -> B-LTA
        byte[] lta = Atick.addDocTimestamp(signed, "{}");
        save("document_timestamp.pdf", lta);
    }
}
