# Signing methods

ATick for Java signs with a credential file or with an external key holder (USB token, smart-card,
HSM, Windows certificate store). Every signing call takes its configuration as a single **JSON
options string**, and every failure throws `Atick.AtickException` (a `RuntimeException`).

```java
import io.github.aniketc068.atick.Atick;
import java.nio.file.*;
```

## 1. PFX / P12 / PEM file

`Atick.signPfx` is the primary method. It accepts both **PKCS#12** (`.pfx` / `.p12`) and **PEM** —
the format is auto-detected.

```java
byte[] pdf = Files.readAllBytes(Path.of("in.pdf"));
byte[] pfx = Files.readAllBytes(Path.of("signer.pfx"));

byte[] signed = Atick.signPfx(pdf, pfx,
    "{\"password\":\"••••\",\"cn\":\"Aniket\",\"reason\":\"Approved\",\"pades\":true}");

Files.write(Path.of("out.pdf"), signed);
```

### PEM credentials

A PEM credential is an unencrypted PKCS#8 / PKCS#1 private key plus one or more `CERTIFICATE`
blocks. Pass its bytes as the `pfx` argument and use an empty `password` (`""`):

```java
byte[] pem = Files.readAllBytes(Path.of("signer.pem"));

byte[] signed = Atick.signPfx(pdf, pem,
    "{\"password\":\"\",\"cn\":\"Aniket\",\"pades\":true}");
```

```{note}
Because the format is auto-detected, the same `signPfx` call works for `.pfx`, `.p12`, and `.pem`.
Only the `password` differs: the PKCS#12 passphrase for `.pfx`/`.p12`, and `""` for PEM.
```

## 2. USB token / smart-card / HSM / Windows store (deferred flow)

ATick for Java does not load PKCS#11 libraries or the Windows store itself. To sign with a key that
never leaves a token, card, HSM, or the OS store, use the **deferred flow**: ATick prepares the
document and hands you the exact bytes to sign, you produce the CMS signature with your own JCA
provider (for example `SunPKCS11`, `SunMSCAPI`, or a vendor provider), and ATick embeds it.

```java
// Step 1 — prepare. Returns { prepared, bytesToSign }.
byte[][] out = Atick.prepare(pdf,
    "{\"cn\":\"Aniket\",\"reason\":\"Approved\",\"pades\":true,\"hash_algo\":\"sha256\"}");
byte[] prepared    = out[0];
byte[] bytesToSign = out[1];

// Step 2 — produce a CMS signature with your own provider.
//   Sign `bytesToSign` using the token / smart-card / HSM / Windows-store key.
//   This is your JCA code (SunPKCS11, SunMSCAPI, or a vendor provider).
byte[] cms = signWithMyProvider(bytesToSign);   // returns a CMS/PKCS#7 SignedData

// Step 3 — embed the CMS into the prepared document.
byte[] signed = Atick.embed(prepared, cms);
Files.write(Path.of("out.pdf"), signed);
```

```{tip}
The CMS you build in step 2 must cover **`bytesToSign`** exactly and use the same `hash_algo` you
passed to `Atick.prepare`. This is the standard eSign / detached-signature pattern: ATick owns the
PDF structure, your provider owns the private key.
```

If you have the key material in software (a `.pfx`/`.p12`/`.pem`), ATick can also build the CMS for
you with `Atick.cmsPfx`, then `Atick.embed`:

```java
byte[][] out = Atick.prepare(pdf, "{\"cn\":\"Aniket\",\"pades\":true}");
byte[] cms    = Atick.cmsPfx(out[1], pfx, "{\"password\":\"••••\",\"pades\":true}");
byte[] signed = Atick.embed(out[0], cms);
```

## Common options

All signing calls (`signPfx`, `prepare` / `cmsPfx`, `signField`) accept the same JSON keys.

| Key | Meaning |
|---|---|
| `"pades": true` | PAdES (`ETSI.CAdES.detached`); `false` → plain CMS (`adbe.pkcs7.detached`) |
| `"hash_algo": "sha256"` | `"sha256"`, `"sha384"`, `"sha512"` |
| `"timestamp": true` | add an RFC-3161 signature timestamp (B-T) |
| `"tsa_url": "…"`, `"tsa_auth": ["user","pass"]` | choose / authenticate the timestamp authority |
| `"ltv": true` | embed long-term validation (B-LT) |
| `"lta": true` | add a document timestamp (B-LTA) |
| `"certify": 1`, `"lock_fields": …` | certification & locking |
| `"verify": true`, `"verify_expiry"`, `"verify_crl"`, `"verify_ocsp"` | pre-sign expiry / CRL / OCSP / chain checks |
| `"field_name": "…"` | the signature field name (auto-uniquified — `Atick_1`, `Atick_2`, …) |
| `"mode": "single" \| "shared"` | one signature on many pages, or many fields sharing one value |

`signPfx` additionally accepts `"open_password"` (decrypt an encrypted input), and
`"encrypt_password"` / `"owner_password"` (password-protect the output).

### Appearance options

The visible signature block is also configured through the same JSON: `cn`, `org`, `ou`,
`location`, `reason`, `text`, `date`, `dn`, `body`, `heading`, `show_mark`, `green_tick`,
`always_check`, `mark_color` (hex `"#E53935"`, name `"blue"`, or `[r,g,b]`), `mark_gradient`,
`mark_scale`, `text_color`, `bg_color`, `border`, `font_size`, `width`, `height`, `page`,
`rect` (`[x1,y1,x2,y2]`), and `placements` (`[[page,[x1,y1,x2,y2]], …]`).

```java
byte[] signed = Atick.signPfx(pdf, pfx,
    "{\"password\":\"••••\",\"cn\":\"Aniket\",\"reason\":\"Approved\","
  + "\"show_mark\":true,\"green_tick\":true,\"mark_color\":\"#E53935\","
  + "\"page\":1,\"rect\":[36,36,236,96],\"pades\":true}");
```

## Multi-signatory (sign an already-signed PDF)

ATick signs as an **incremental update**: existing signatures keep their byte ranges and stay
valid. Just sign the already-signed PDF again — the field name is auto-uniquified so it never
collides.

```java
byte[] v1 = Atick.signPfx(pdf, pfx, "{\"password\":\"••••\",\"cn\":\"Aniket\",\"pades\":true}"); // Atick_1
byte[] v2 = Atick.signPfx(v1,  pfx, "{\"password\":\"••••\",\"cn\":\"Reviewer\",\"pades\":true}"); // Atick_2
```

The same holds for the deferred flow: run `Atick.prepare` -> external CMS -> `Atick.embed` on the
already-signed bytes to add another signature.
