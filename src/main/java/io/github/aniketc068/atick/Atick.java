package io.github.aniketc068.atick;

// ATick for Java — a complete PDF digital-signature library.
// Calls the compiled ATick engine through JNA (pure Java; no JNI C glue). The matching native
// engine for the running OS/arch is bundled inside this jar and loaded automatically by JNA.
// Works on Java 8 and up. Every failure is a normal Java AtickException.
import com.sun.jna.IntegerType;
import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import java.nio.charset.StandardCharsets;

public final class Atick {

    /** Thrown by any ATick operation that fails. */
    public static class AtickException extends RuntimeException {
        public AtickException(String message) { super(message); }
    }

    /** C size_t (pointer-sized on every platform — 32-bit and 64-bit alike). */
    public static class size_t extends IntegerType {
        public size_t() { this(0); }
        public size_t(long value) { super(Native.SIZE_T_SIZE, value); }
    }

    interface Lib extends Library {
        Lib I = Native.load("atick", Lib.class);
        String atick_version();
        void atick_free(Pointer ptr, size_t len);
        void atick_set_brand(String tag);
        int atick_sign_pfx(byte[] pdf, size_t pdfLen, byte[] pfx, size_t pfxLen, byte[] opt, PointerByReference out, Pointer outLen);
        int atick_embed(byte[] prep, size_t prepLen, byte[] cms, size_t cmsLen, PointerByReference out, Pointer outLen);
        int atick_cms_pfx(byte[] data, size_t dataLen, byte[] pfx, size_t pfxLen, byte[] opt, PointerByReference out, Pointer outLen);
        int atick_decrypt(byte[] pdf, size_t pdfLen, byte[] pw, PointerByReference out, Pointer outLen);
        int atick_prepare(byte[] pdf, size_t pdfLen, byte[] opt, PointerByReference out, Pointer outLen, PointerByReference outData, Pointer outDataLen);
        void atick_set_fast_signing(int on);
        int atick_add_doctimestamp(byte[] pdf, size_t pdfLen, byte[] opt, PointerByReference out, Pointer outLen);
        int atick_set_metadata(byte[] pdf, size_t pdfLen, byte[] opt, PointerByReference out, Pointer outLen);
        int atick_prepare_fields(byte[] pdf, size_t pdfLen, byte[] opt, PointerByReference out, Pointer outLen);
        int atick_sign_field(byte[] pdf, size_t pdfLen, byte[] pfx, size_t pfxLen, byte[] opt, PointerByReference out, Pointer outLen);
    }

    // FIXED branding: this is the Java binding -> "ATick_java" everywhere branding is fixed.
    static { try { Lib.I.atick_set_brand("java"); } catch (Throwable ignored) {} }

    private static byte[] utf8z(String s) { return ((s == null ? "" : s) + "\0").getBytes(StandardCharsets.UTF_8); }

    private static long len(Memory m) {
        return Native.SIZE_T_SIZE == 8 ? m.getLong(0) : (m.getInt(0) & 0xFFFFFFFFL);
    }

    private static byte[] take(int rc, PointerByReference out, Memory outLen) {
        long n = len(outLen);
        Pointer p = out.getValue();
        byte[] buf = (p != null && n > 0) ? p.getByteArray(0, (int) n) : new byte[0];
        if (p != null) Lib.I.atick_free(p, new size_t(n));
        if (rc != 0) throw new AtickException(new String(buf, StandardCharsets.UTF_8));
        return buf;
    }

    /** The library version string. */
    public static String version() { return Lib.I.atick_version(); }

    /** Sign a PDF with a PFX/P12 (or PEM). optionsJson carries the password, appearance and flags. */
    public static byte[] signPfx(byte[] pdf, byte[] pfx, String optionsJson) {
        PointerByReference out = new PointerByReference();
        Memory outLen = new Memory(Native.SIZE_T_SIZE);
        int rc = Lib.I.atick_sign_pfx(pdf, new size_t(pdf.length), pfx, new size_t(pfx.length), utf8z(optionsJson), out, outLen);
        return take(rc, out, outLen);
    }

    /** Embed a detached CMS/PKCS#7 into a prepared PDF. */
    public static byte[] embed(byte[] prepared, byte[] cms) {
        PointerByReference out = new PointerByReference();
        Memory outLen = new Memory(Native.SIZE_T_SIZE);
        int rc = Lib.I.atick_embed(prepared, new size_t(prepared.length), cms, new size_t(cms.length), out, outLen);
        return take(rc, out, outLen);
    }

    /** Produce a detached CMS over data signed with a PFX. */
    public static byte[] cmsPfx(byte[] data, byte[] pfx, String optionsJson) {
        PointerByReference out = new PointerByReference();
        Memory outLen = new Memory(Native.SIZE_T_SIZE);
        int rc = Lib.I.atick_cms_pfx(data, new size_t(data.length), pfx, new size_t(pfx.length), utf8z(optionsJson), out, outLen);
        return take(rc, out, outLen);
    }

    /** Decrypt a password-protected PDF. */
    public static byte[] decrypt(byte[] pdf, String password) {
        PointerByReference out = new PointerByReference();
        Memory outLen = new Memory(Native.SIZE_T_SIZE);
        int rc = Lib.I.atick_decrypt(pdf, new size_t(pdf.length), utf8z(password), out, outLen);
        return take(rc, out, outLen);
    }

    /** Enable/disable fast signing (reuse fetched revocation across many documents in one run). */
    public static void setFastSigning(boolean on) { Lib.I.atick_set_fast_signing(on ? 1 : 0); }

    /** Add a standalone archive DocTimeStamp (+ DSS) to an already-signed PDF. */
    public static byte[] addDocTimestamp(byte[] pdf, String optionsJson) {
        PointerByReference out = new PointerByReference();
        Memory outLen = new Memory(Native.SIZE_T_SIZE);
        int rc = Lib.I.atick_add_doctimestamp(pdf, new size_t(pdf.length), utf8z(optionsJson), out, outLen);
        return take(rc, out, outLen);
    }

    /** Set document metadata (Title/Author/Subject/Keywords/Creator/CreationDate/ModDate via JSON). */
    public static byte[] setMetadata(byte[] pdf, String optionsJson) {
        PointerByReference out = new PointerByReference();
        Memory outLen = new Memory(Native.SIZE_T_SIZE);
        int rc = Lib.I.atick_set_metadata(pdf, new size_t(pdf.length), utf8z(optionsJson), out, outLen);
        return take(rc, out, outLen);
    }

    /** Prepare an empty signing field (template) — appearance drawn, signature left empty. */
    public static byte[] prepareFields(byte[] pdf, String optionsJson) {
        PointerByReference out = new PointerByReference();
        Memory outLen = new Memory(Native.SIZE_T_SIZE);
        int rc = Lib.I.atick_prepare_fields(pdf, new size_t(pdf.length), utf8z(optionsJson), out, outLen);
        return take(rc, out, outLen);
    }

    /** Sign an existing empty field (e.g. from {@link #prepareFields}) with a PFX/P12/PEM. */
    public static byte[] signField(byte[] pdf, byte[] pfx, String optionsJson) {
        PointerByReference out = new PointerByReference();
        Memory outLen = new Memory(Native.SIZE_T_SIZE);
        int rc = Lib.I.atick_sign_field(pdf, new size_t(pdf.length), pfx, new size_t(pfx.length), utf8z(optionsJson), out, outLen);
        return take(rc, out, outLen);
    }

    /** Prepare a PDF for deferred / remote (eSign / HSM) signing. Returns {prepared, bytesToSign};
     *  sign the second element externally and call {@link #embed}. */
    public static byte[][] prepare(byte[] pdf, String optionsJson) {
        PointerByReference out = new PointerByReference();
        Memory outLen = new Memory(Native.SIZE_T_SIZE);
        PointerByReference outData = new PointerByReference();
        Memory outDataLen = new Memory(Native.SIZE_T_SIZE);
        int rc = Lib.I.atick_prepare(pdf, new size_t(pdf.length), utf8z(optionsJson), out, outLen, outData, outDataLen);
        long n = len(outLen);
        Pointer p = out.getValue();
        byte[] prepared = (p != null && n > 0) ? p.getByteArray(0, (int) n) : new byte[0];
        if (p != null) Lib.I.atick_free(p, new size_t(n));
        if (rc != 0) throw new AtickException(new String(prepared, StandardCharsets.UTF_8));
        long dn = len(outDataLen);
        Pointer dp = outData.getValue();
        byte[] data = (dp != null && dn > 0) ? dp.getByteArray(0, (int) dn) : new byte[0];
        if (dp != null) Lib.I.atick_free(dp, new size_t(dn));
        return new byte[][]{prepared, data};
    }
}
