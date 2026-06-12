// ATick Java example  (Python: 18_date_and_pem.py, date part) — any strftime-style date in the appearance.
import io.github.aniketc068.atick.Atick;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class DateFormats {
    static byte[] read(String n) throws Exception { return Files.readAllBytes(Paths.get("samples", n)); }
    static void save(String n, byte[] b) throws Exception {
        Files.createDirectories(Paths.get("signed"));
        Files.write(Paths.get("signed", n), b);
        System.out.println("  " + n + " (" + b.length + " bytes)");
    }
    static String fmt(String pattern) { return new SimpleDateFormat(pattern, Locale.ENGLISH).format(new Date()); }
    public static void main(String[] a) throws Exception {
        byte[] pfx = read("ABC12.pfx"), pdf = read("blank.pdf");
        String[][] variants = {
            {"iso",    fmt("yyyy-MM-dd HH:mm:ss")},
            {"eu",     fmt("dd/MM/yyyy HH:mm")},
            {"us",     fmt("MM-dd-yyyy hh:mm a")},
            {"words",  fmt("EEEE, dd MMMM yyyy")},
            {"custom", fmt("'Signed on' dd-MMM-yyyy 'at' hh:mm a")},
        };
        for (String[] v : variants) {
            String opt = "{\"password\":\"ABC12\",\"cn\":\"DS TEST CERTIFICATE 06\",\"reason\":\"Approved\","
                + "\"date\":\"" + v[1] + "\",\"page\":1,\"rect\":[300,55,575,175],\"pades\":true}";
            save("18_date_" + v[0] + ".pdf", Atick.signPfx(pdf, pfx, opt));
        }
    }
}
