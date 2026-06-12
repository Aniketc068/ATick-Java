// ATick Java example  (Python: 17_multi_revision.py) — sign, then sign the signed PDF again (revisions).
import io.github.aniketc068.atick.Atick;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MultiRevision {
    static byte[] read(String n) throws Exception { return Files.readAllBytes(Paths.get("samples", n)); }
    static String now() { return new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.ENGLISH).format(new Date()); }
    static void save(String n, byte[] b) throws Exception {
        Files.createDirectories(Paths.get("signed"));
        Files.write(Paths.get("signed", n), b);
        System.out.println("  " + n + " (" + b.length + " bytes)");
    }
    static String opt(String field, int[] rect) {
        return "{\"password\":\"ABC12\",\"cn\":\"DS TEST CERTIFICATE 06\",\"reason\":\"Approved\",\"date\":\"" + now()
            + "\",\"field_name\":\"" + field + "\",\"page\":1,\"rect\":[" + rect[0] + "," + rect[1] + "," + rect[2] + "," + rect[3] + "],\"pades\":true}";
    }
    public static void main(String[] a) throws Exception {
        byte[] pfx = read("ABC12.pfx");
        byte[] r1 = Atick.signPfx(read("blank.pdf"), pfx, opt("Rev1", new int[]{300, 55, 575, 175}));  save("rev1.pdf", r1);
        byte[] r2 = Atick.signPfx(r1, pfx, opt("Rev2", new int[]{40, 640, 260, 750}));                 save("rev2.pdf", r2);
        byte[] r3 = Atick.signPfx(r2, pfx, opt("Rev3", new int[]{40, 400, 260, 510}));                 save("rev3.pdf", r3);
    }
}
