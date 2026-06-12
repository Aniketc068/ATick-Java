// ATick Java example  (Python: 16_invisible.py) — invisible signature (placements: [] -> nothing drawn).
import io.github.aniketc068.atick.Atick;
import java.nio.file.*;

public class Invisible {
    static byte[] read(String n) throws Exception { return Files.readAllBytes(Paths.get("samples", n)); }
    static void save(String n, byte[] b) throws Exception {
        Files.createDirectories(Paths.get("signed"));
        Files.write(Paths.get("signed", n), b);
        System.out.println("  " + n + " (" + b.length + " bytes)");
    }
    public static void main(String[] a) throws Exception {
        byte[] pfx = read("ABC12.pfx"), pdf1 = read("blank.pdf");
        byte[] s = Atick.signPfx(pdf1, pfx, "{\"password\":\"ABC12\",\"cn\":\"DS TEST CERTIFICATE 06\","
            + "\"reason\":\"Invisible approval\",\"placements\":[],\"field_name\":\"InvisibleSignature\","
            + "\"pades\":true,\"timestamp\":true,\"ltv\":true}");
        save("16_invisible.pdf", s);
    }
}
