// ATick Java example  (Python: 08_deferred_esign.py) — Deferred (TWO-STEP) signing for a REMOTE key
// (eSign ESP / HSM / smart card), shown on several pages.
//
// When the private key lives elsewhere, ATick splits signing into two steps:
//   1) Atick.prepare(pdf, options) -> {preparedPdf, bytesToSign}
//        bytesToSign      = the exact bytes that must be signed (the ByteRange)
//        sha256(bytesToSign) = their hash -> send THIS to your eSign service if it wants a hash
//   2) your signer produces a DETACHED PKCS#7/CMS over bytesToSign
//   3) Atick.embed(preparedPdf, cms) -> signedPdf
//
// Below, ATick itself (Atick.cmsPfx) stands in for the external signer so the demo runs with no extra
// setup — replace that block with YOUR eSign ESP / HSM / token call (it just returns a detached CMS
// over bytesToSign).
import io.github.aniketc068.atick.Atick;
import java.nio.file.*;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

public class DeferredEsign {
    static byte[] read(String n) throws Exception { return Files.readAllBytes(Paths.get("samples", n)); }
    static String now() { return new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.ENGLISH).format(new Date()); }
    static String hex(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte x : b) sb.append(String.format("%02x", x & 0xff));
        return sb.toString();
    }
    static void save(String n, byte[] b) throws Exception {
        Files.createDirectories(Paths.get("signed"));
        Files.write(Paths.get("signed", n), b);
        System.out.println("  " + n + " (" + b.length + " bytes)");
    }
    public static void main(String[] a) throws Exception {
        byte[] pfx = read("ABC12.pfx"), pdf3 = read("blank3.pdf");

        // ---- STEP 1: prepare (no key needed) -> appearance on every page + bytes-to-sign ----
        String pl = "[[1,[40,640,260,750]],[2,[330,380,560,490]],[3,[180,60,400,170]]]";
        byte[][] pr = Atick.prepare(pdf3, "{\"cn\":\"DS TEST CERTIFICATE 06\",\"reason\":\"eSign\",\"date\":\"" + now()
            + "\",\"placements\":" + pl + ",\"mode\":\"single\",\"field_name\":\"Signature1\","
            + "\"signer_name\":\"DS TEST CERTIFICATE 06\",\"contents_size\":16384}");
        byte[] prepared = pr[0], bytesToSign = pr[1];
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytesToSign);
        System.out.println("STEP 1: hash to send to the signer: " + hex(digest));

        // ---- STEP 2: the EXTERNAL signer makes a detached CMS over bytesToSign ----
        //   >>> Replace this whole block with your eSign ESP / HSM / token call. <<<
        //   Here ATick itself stands in for the external signer (Atick.cmsPfx makes a detached CMS over
        //   the given bytes), so the demo runs with no extra setup. In production this CMS comes from the ESP.
        byte[] cms = Atick.cmsPfx(bytesToSign, pfx, "{\"password\":\"ABC12\",\"hash_algo\":\"sha256\"}");

        // ---- STEP 3: embed the CMS ----
        save("08_deferred.pdf", Atick.embed(prepared, cms));
        System.out.println("  signed on 3 pages via the two-step eSign flow");
    }
}
