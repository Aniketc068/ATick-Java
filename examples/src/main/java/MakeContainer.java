// ATick Java example  (Python: make_container.py) — build the ATick appearance + an EMPTY signing
// container (ByteRange + empty Contents) without signing; an external signer fills it later.
import io.github.aniketc068.atick.Atick;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MakeContainer {
    static byte[] read(String n) throws Exception { return Files.readAllBytes(Paths.get("samples", n)); }
    static String now() { return new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.ENGLISH).format(new Date()); }
    static void save(String n, byte[] b) throws Exception {
        Files.createDirectories(Paths.get("signed"));
        Files.write(Paths.get("signed", n), b);
        System.out.println("  " + n + " (" + b.length + " bytes)");
    }
    public static void main(String[] a) throws Exception {
        byte[] pdf = read("blank.pdf");
        // prepare -> [preparedPdf, bytesToSign]; the prepared PDF is the container
        byte[][] pr = Atick.prepare(pdf, "{\"cn\":\"DS TEST CERTIFICATE 06\",\"reason\":\"Approved\",\"date\":\"" + now()
            + "\",\"page\":1,\"rect\":[300,55,575,175],\"pades\":true,\"contents_size\":16384}");
        save("container.pdf", pr[0]);
        System.out.println("  (bytes-to-sign: " + pr[1].length + " bytes — hand to an external signer)");
    }
}
