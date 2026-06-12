// ATick Java example  (Python: 12_metadata.py) — set document metadata, then sign.
import io.github.aniketc068.atick.Atick;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Metadata {
    static byte[] read(String n) throws Exception { return Files.readAllBytes(Paths.get("samples", n)); }
    static String now() { return new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.ENGLISH).format(new Date()); }
    static void save(String n, byte[] b) throws Exception {
        Files.createDirectories(Paths.get("signed"));
        Files.write(Paths.get("signed", n), b);
        System.out.println("  " + n + " (" + b.length + " bytes)");
    }
    public static void main(String[] a) throws Exception {
        byte[] pfx = read("ABC12.pfx"), pdf = read("blank.pdf");
        byte[] withMeta = Atick.setMetadata(pdf, "{\"title\":\"ATick Demo\",\"author\":\"Aniket Chaturvedi\","
            + "\"subject\":\"Digital signature\",\"keywords\":\"pdf,pades,atick\",\"application\":\"ATick\"}");
        byte[] signed = Atick.signPfx(withMeta, pfx, "{\"password\":\"ABC12\",\"cn\":\"DS TEST CERTIFICATE 06\","
            + "\"reason\":\"Approved\",\"date\":\"" + now() + "\",\"green_tick\":true,\"page\":1,\"rect\":[300,55,575,175],\"pades\":true}");
        save("12_metadata.pdf", signed);
    }
}
