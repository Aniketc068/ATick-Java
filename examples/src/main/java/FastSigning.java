// ATick Java example  (Python: fast_signing.py) — sign many documents fast by reusing the fetched
// revocation material across the run (set fast signing ON, sign a batch, turn it OFF).
import io.github.aniketc068.atick.Atick;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class FastSigning {
    static byte[] read(String n) throws Exception { return Files.readAllBytes(Paths.get("samples", n)); }
    static String now() { return new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.ENGLISH).format(new Date()); }
    static void save(String n, byte[] b) throws Exception {
        Files.createDirectories(Paths.get("signed"));
        Files.write(Paths.get("signed", n), b);
        System.out.println("  " + n + " (" + b.length + " bytes)");
    }
    public static void main(String[] a) throws Exception {
        byte[] pfx = read("ABC12.pfx"), pdf3 = read("blank3.pdf");
        String pl = "[[1,[40,640,260,750]],[2,[330,380,560,490]],[3,[180,60,400,170]]]";
        Atick.setFastSigning(true);          // reuse revocation across the batch
        for (int i = 1; i <= 5; i++) {
            String opt = "{\"password\":\"ABC12\",\"cn\":\"DS TEST CERTIFICATE 06\",\"reason\":\"Approved\",\"date\":\"" + now()
                + "\",\"placements\":" + pl + ",\"mode\":\"single\",\"pades\":true,\"timestamp\":true,\"ltv\":true}";
            save("fast_" + i + ".pdf", Atick.signPfx(pdf3, pfx, opt));
        }
        Atick.setFastSigning(false);
    }
}
