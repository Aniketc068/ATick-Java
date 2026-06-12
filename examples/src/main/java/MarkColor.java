// ATick Java example  (Python: 11_mark_color.py) — the "?" mark colour + gradient.
import io.github.aniketc068.atick.Atick;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MarkColor {
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
        String[][] M = { {"default", ""}, {"hex", ",\"mark_color\":\"#3CB371\""}, {"named", ",\"mark_color\":\"orange\""},
            {"rgb255", ",\"mark_color\":[255,80,80]"}, {"gradient", ",\"mark_gradient\":[\"#FFD700\",\"#FF4500\"]"} };
        for (String[] m : M)
            save("11_mark_" + m[0] + ".pdf", Atick.signPfx(pdf1, pfx, "{" + base + m[1] + "}"));
    }
}
