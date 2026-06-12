// ATick Java example  (Python: 04_certify_and_lock.py) — DocMDP certification + lock all fields.
import io.github.aniketc068.atick.Atick;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class CertifyLock {
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
        String base = "\"password\":\"ABC12\",\"cn\":\"DS TEST CERTIFICATE 06\",\"reason\":\"Approved\",\"date\":\"" + now()
            + "\",\"placements\":" + pl + ",\"mode\":\"single\",\"pades\":true";
        // certify = 2 (DocMDP FORM_FILLING — later form fill / signing still allowed)
        save("04_certified_formfill.pdf", Atick.signPfx(pdf3, pfx, "{" + base + ",\"certify\":2}"));
        // certify = 1 (DocMDP NO_CHANGES) and lock every form field
        save("04_certified_locked.pdf", Atick.signPfx(pdf3, pfx, "{" + base + ",\"certify\":1,\"lock_fields\":[\"*\"]}"));
    }
}
