// ATick Java example  (Python: 03_appearance.py) — full appearance via the Style options.
import io.github.aniketc068.atick.Atick;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Appearance {
    static byte[] read(String n) throws Exception { return Files.readAllBytes(Paths.get("samples", n)); }
    static String now() { return new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.ENGLISH).format(new Date()); }
    static void save(String n, byte[] b) throws Exception {
        Files.createDirectories(Paths.get("signed"));
        Files.write(Paths.get("signed", n), b);
        System.out.println("  " + n + " (" + b.length + " bytes)");
    }
    public static void main(String[] a) throws Exception {
        byte[] pfx = read("ABC12.pfx"), pdf3 = read("blank3.pdf");
        String pl = "[[1,[320,600,575,720]],[2,[40,360,295,480]],[3,[170,55,425,175]]]";
        byte[] s = Atick.signPfx(pdf3, pfx, "{\"password\":\"ABC12\",\"cn\":\"DS TEST CERTIFICATE 06\","
            + "\"org\":\"Acme Corp CA\",\"ou\":\"Class 3\",\"location\":\"New Delhi, India\","
            + "\"reason\":\"Approved for release\",\"text\":\"Verified by ATick\",\"date\":\"" + now()
            + "\",\"width\":240,\"height\":120,\"placements\":" + pl + ",\"mode\":\"single\",\"pades\":true}");
        save("03_appearance.pdf", s);
    }
}
