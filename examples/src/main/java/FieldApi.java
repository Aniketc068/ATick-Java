// ATick Java example  (Python: 14_field_api.py) — low-level: prepare an empty field, then sign it.
import io.github.aniketc068.atick.Atick;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class FieldApi {
    static byte[] read(String n) throws Exception { return Files.readAllBytes(Paths.get("samples", n)); }
    static String now() { return new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.ENGLISH).format(new Date()); }
    static void save(String n, byte[] b) throws Exception {
        Files.createDirectories(Paths.get("signed"));
        Files.write(Paths.get("signed", n), b);
        System.out.println("  " + n + " (" + b.length + " bytes)");
    }
    public static void main(String[] a) throws Exception {
        byte[] pfx = read("ABC12.pfx"), pdf = read("blank.pdf");
        // 1) prepare an empty signing field (template) with the ATick appearance
        byte[] template = Atick.prepareFields(pdf, "{\"cn\":\"DS TEST CERTIFICATE 06\",\"reason\":\"Approved\","
            + "\"date\":\"" + now() + "\",\"field_name\":\"Sig1\",\"page\":1,\"rect\":[300,55,575,175],\"pades\":true}");
        save("14_prepared_fields_template.pdf", template);
        // 2) sign that field
        byte[] signed = Atick.signField(template, pfx, "{\"password\":\"ABC12\",\"field_name\":\"Sig1\","
            + "\"reason\":\"Approved\",\"pades\":true}");
        save("14_sign_field.pdf", signed);
    }
}
