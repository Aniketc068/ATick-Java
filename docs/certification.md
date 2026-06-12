# Certification & field locking

## Certification (DocMDP)

A **certifying** signature declares which later changes are allowed. Pass the `certify` option to
the first signature:

```java
import io.github.aniketc068.atick.Atick;
import java.nio.file.*;

byte[] pdf = Files.readAllBytes(Path.of("contract.pdf"));
byte[] pfx = Files.readAllBytes(Path.of("signer.pfx"));

// P=1 — no changes at all
byte[] out1 = Atick.signPfx(pdf, pfx, "{\"password\":\"••••\",\"certify\":1}");

// P=2 — form filling + signing
byte[] out2 = Atick.signPfx(pdf, pfx, "{\"password\":\"••••\",\"certify\":2}");

// P=3 — form filling + annotations
byte[] out3 = Atick.signPfx(pdf, pfx, "{\"password\":\"••••\",\"certify\":3}");
```

Omit `certify` (or set it to `0`) to produce a normal, non-certifying approval signature.

| Level | Value | Allows |
|---|---|---|
| `NONE` | 0 | a normal approval signature (no certification) |
| `NO_CHANGES` | 1 | nothing — any later change (incl. another signature, LTV, timestamp) breaks it |
| `FORM_FILLING` | 2 | filling form fields + adding signatures |
| `FORM_FILLING + ANNOTATIONS` | 3 | the above + annotations |

```{note}
`NO_CHANGES` (P=1) forbids **everything** afterwards — so it cannot be combined with later LTV,
document timestamps, or extra approval signatures. Use it as a single, final signature. For a
document that will gather more signatures, certify with `2` (`FORM_FILLING`) or `3`
(`FORM_FILLING + ANNOTATIONS`).
```

## Field locking (FieldMDP)

Lock specific form fields so they cannot be changed after signing — without certifying the whole
document:

```java
// lock these fields only
byte[] locked = Atick.signPfx(pdf, pfx,
    "{\"password\":\"••••\",\"lock_fields\":[\"ApproverName\"]}");

// lock ALL fields
byte[] lockedAll = Atick.signPfx(pdf, pfx,
    "{\"password\":\"••••\",\"lock_fields\":[\"*\"]}");
```

If a locked field is altered after signing, the signature is reported as invalid.

You can also **certify and lock** in one signature — combine `certify` with `lock_fields`:

```java
byte[] out = Atick.signPfx(pdf, pfx,
    "{\"password\":\"••••\",\"certify\":1,\"lock_fields\":[\"*\"]}");
```

## Pre-sign checks

Validate the signing certificate **before** signing. These checks run prior to producing any
output, and signing is **refused** if a check fails — so an invalid certificate never produces a
signature.

```{list-table}
:header-rows: 1

* - Option
  - Effect
* - `verify`
  - run the full set of certificate checks below
* - `verify_expiry`
  - certificate must not be expired (or not yet valid)
* - `verify_crl`
  - certificate must not be revoked per its CRL
* - `verify_ocsp`
  - certificate must not be revoked per OCSP
* - `trusted_roots`
  - chain (built from AIA) must reach one of these pinned root SHA-1 hex strings
```

```java
byte[] out = Atick.signPfx(pdf, pfx,
    "{\"password\":\"••••\"," +
    "\"verify\":true," +                                  // not expired + CRL + OCSP + not revoked
    "\"trusted_roots\":[\"<root SHA-1>\",\"<another>\"]}"); // chain must reach one of these
```

You can also enable the individual checks instead of the umbrella `verify` flag:

```java
byte[] out = Atick.signPfx(pdf, pfx,
    "{\"password\":\"••••\"," +
    "\"verify_expiry\":true," +
    "\"verify_crl\":true," +
    "\"verify_ocsp\":true}");
```

Because a failed pre-sign check refuses to sign, it surfaces as an `Atick.AtickException`. Wrap the
call in a try/catch so a revoked or expired certificate is handled instead of crashing:

```java
import io.github.aniketc068.atick.Atick;
import java.nio.file.*;

try {
    byte[] out = Atick.signPfx(pdf, pfx,
        "{\"password\":\"••••\"," +
        "\"verify\":true," +
        "\"trusted_roots\":[\"<root SHA-1>\"]}");
    Files.write(Path.of("signed.pdf"), out);
} catch (Atick.AtickException e) {
    // certificate expired, revoked (CRL/OCSP), or chain did not reach a pinned root —
    // nothing was signed
    System.err.println("Signing refused: " + e.getMessage());
}
```

```{note}
`verify_crl` and `verify_ocsp` reach out to the CA's revocation endpoints (discovered from the
certificate). If those endpoints are unreachable the check cannot complete and signing is refused —
keep the catch block above in place.
```
