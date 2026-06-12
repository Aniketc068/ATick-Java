# API reference

All operations are static methods on `io.github.aniketc068.atick.Atick`. Every method takes raw
`byte[]` for PDFs and certificates, and an options JSON string where applicable. On any failure a
method throws `Atick.AtickException` — a public static class extending `RuntimeException`. The error
text is available from `e.getMessage()`.

```java
import io.github.aniketc068.atick.Atick;
```

## Signing

```java
static byte[] signPfx(byte[] pdf, byte[] pfx, String optionsJson)
```

Sign `pdf` with a `.pfx`/`.p12`/`.pem` credential (the format is auto-detected). For a PEM file pass
the password as the empty string `""` inside the options. Returns the signed PDF bytes.

- **pdf** — the PDF to sign.
- **pfx** — the credential bytes (`.pfx`, `.p12`, or `.pem`).
- **optionsJson** — the options JSON (see the [Options](#options-json) table). Pass the credential
  password as the `password` key; use `""` for PEM.
- **returns** — the signed PDF as `byte[]`.

```java
import io.github.aniketc068.atick.Atick;

byte[] pdf = Files.readAllBytes(Path.of("in.pdf"));
byte[] pfx = Files.readAllBytes(Path.of("signer.pfx"));

String options = """
    {
      "password": "secret",
      "cn": "Aniket Chaturvedi",
      "reason": "Approval",
      "page": 1,
      "rect": [40, 40, 240, 140],
      "pades": true,
      "timestamp": true,
      "tsa_url": "http://timestamp.example/tsa"
    }
    """;

try {
    byte[] signed = Atick.signPfx(pdf, pfx, options);
    Files.write(Path.of("signed.pdf"), signed);
} catch (Atick.AtickException e) {
    System.err.println("signing failed: " + e.getMessage());
}
```

```java
static byte[] signField(byte[] pdf, byte[] pfx, String optionsJson)
```

Sign an existing empty signature field. Use the `field_name` option to select the field. Returns the
signed PDF bytes.

- **pdf** — a PDF containing an empty signature field (see [`prepareFields`](#field-templates)).
- **pfx** — the credential bytes.
- **optionsJson** — must include `field_name`; same credential and signing keys as `signPfx`.
- **returns** — the signed PDF as `byte[]`.

## Deferred / remote-key signing

These three methods cover the deferred (eSign / HSM / remote-key) flow: prepare the PDF, sign the
returned bytes elsewhere, then embed the resulting CMS.

```java
static byte[][] prepare(byte[] pdf, String optionsJson)
```

Step 1 of deferred signing. Adds an empty signature field, the appearance, and the signature
container, then returns the exact bytes that must be signed. Returns a `byte[][]` of length 2:

- index `0` — the **prepared PDF** (`byte[]`).
- index `1` — the **bytes to sign** (`byte[]`); hash and sign these with the remote key.

- **pdf** — the PDF to prepare.
- **optionsJson** — appearance and signing options (see the [Options](#options-json) table).
- **returns** — `byte[][]{ prepared, bytesToSign }`.

```java
static byte[] cmsPfx(byte[] data, byte[] pfx, String optionsJson)
```

Produce a detached PKCS#7 / CMS signature over `data` using a PFX. Useful for producing the CMS that
[`embed`](#embed) expects when the signing credential is a local PFX.

- **data** — the bytes to sign (typically index `1` from `prepare`).
- **pfx** — the credential bytes.
- **optionsJson** — `password`, `hash_algo`, `pades`, `timestamp`, `tsa_url`, `tsa_auth`, `ltv`.
- **returns** — the detached CMS as `byte[]`.

```java
static byte[] embed(byte[] prepared, byte[] cms)
```

Embed a detached CMS / PKCS#7 into a prepared PDF. Returns the signed PDF bytes.

- **prepared** — the prepared PDF (index `0` from `prepare`).
- **cms** — the detached CMS (from `cmsPfx`, an eSign reply, or an HSM).
- **returns** — the signed PDF as `byte[]`.

```java
import io.github.aniketc068.atick.Atick;

byte[][] step = Atick.prepare(pdf, options);
byte[] prepared    = step[0];
byte[] bytesToSign = step[1];

byte[] cms    = Atick.cmsPfx(bytesToSign, pfx, "{\"password\":\"secret\"}");
byte[] signed = Atick.embed(prepared, cms);
```

## Field templates

```java
static byte[] prepareFields(byte[] pdf, String optionsJson)
```

Create an empty signature field as a template: the appearance is drawn, but the signature is left
empty so it can be signed later with [`signField`](#signing). Returns the PDF bytes.

- **pdf** — the PDF to add the field to.
- **optionsJson** — appearance options plus `field_name`, `page`, `rect` / `placements`.
- **returns** — the PDF with an empty field as `byte[]`.

## Long-term validation & timestamps

```java
static byte[] addDocTimestamp(byte[] pdf, String optionsJson)
```

Add an archive DocTimeStamp (and the DSS validation material) to an already-signed PDF, producing a
PAdES-B-LTA document. Returns the timestamped PDF bytes.

- **pdf** — an already-signed PDF.
- **optionsJson** — `tsa_url`, `tsa_auth`, `ltv`, `contents_size`.
- **returns** — the timestamped PDF as `byte[]`.

## Documents & utilities

```java
static byte[] setMetadata(byte[] pdf, String optionsJson)
```

Set the document information (`/Info`) metadata on a PDF. Returns the updated PDF bytes.

- **pdf** — the PDF to update.
- **optionsJson** — `title`, `author`, `subject`, `keywords`, `application`, `created`, `modified`
  (see the [Metadata options](#metadata-options) table).
- **returns** — the updated PDF as `byte[]`.

```java
static byte[] decrypt(byte[] pdf, String password)
```

Decrypt a password-protected PDF. Returns the plaintext PDF bytes.

- **pdf** — the encrypted PDF.
- **password** — the open (user) password.
- **returns** — the decrypted PDF as `byte[]`.

```java
static void setFastSigning(boolean on)
```

Enable or disable the in-memory revocation cache (used to speed up repeated CRL/OCSP lookups).
Passing `false` disables it.

- **on** — `true` to enable the cache, `false` to disable it.

```java
static String version()
```

Return the engine version string.

- **returns** — the version as a `String`.

```java
System.out.println("ATick " + Atick.version());
```

(options-json)=
## Options JSON

The `optionsJson` argument is a JSON object string. All keys are optional unless a method note says
otherwise. Keys are grouped below by purpose.

### Identity & appearance text

| Key | Type | Meaning |
| --- | --- | --- |
| `cn` | string | Common name shown in the appearance. |
| `org` | string | Organisation line. |
| `ou` | string | Organisational unit line. |
| `location` | string | Signing location, also written to the signature. |
| `reason` | string | Reason for signing, also written to the signature. |
| `text` | string | Free text shown in the appearance. |
| `date` | string | Date string shown in the appearance. |
| `dn` | string | Full distinguished name line. |
| `body` | string | Custom-text-only appearance body (`\n` = new line, `*x*` = bold). |
| `heading` | string | Heading line above the signature details. |

### Verified mark

| Key | Type | Meaning |
| --- | --- | --- |
| `show_mark` | bool | Draw the verified mark. |
| `green_tick` | bool | Use the "?" verified mark. |
| `always_check` | bool | Always draw the verified/checked mark. |
| `mark_color` | string hex / name / `[r,g,b]` | Colour of the mark. |
| `mark_gradient` | array of colours | Gradient fill for the mark. |
| `mark_scale` | number | Scale factor for the mark size. |

### Layout & styling

| Key | Type | Meaning |
| --- | --- | --- |
| `text_color` | string hex / name / `[r,g,b]` | Text colour. |
| `bg_color` | string hex / name / `[r,g,b]` | Background colour of the appearance. |
| `border` | bool | Draw a border around the appearance. |
| `font_size` | number | Font size of the appearance text. |
| `width` | number | Appearance width. |
| `height` | number | Appearance height. |

### Placement

| Key | Type | Meaning |
| --- | --- | --- |
| `page` | int | Page number for the signature (1-based). |
| `rect` | `[x1, y1, x2, y2]` | Rectangle of the appearance on `page`. |
| `placements` | `[[page, [x1, y1, x2, y2]], ...]` | Multiple appearance placements (one signature, several pages). |
| `mode` | `"single"` \| `"shared"` | Whether placements share one signature (`"single"`) or are separate. |
| `field_name` | string | Name of the signature field. |

### Cryptography & PAdES

| Key | Type | Meaning |
| --- | --- | --- |
| `pades` | bool | Produce a PAdES signature. |
| `hash_algo` | `"sha256"` \| `"sha384"` \| `"sha512"` | Digest algorithm. |
| `timestamp` | bool | Add an RFC-3161 signature timestamp. |
| `tsa_url` | string | Timestamp authority URL. |
| `tsa_auth` | `["user", "pass"]` | Basic-auth credentials for the TSA. |
| `ltv` | bool | Add long-term validation material (DSS). |
| `lta` | bool | Add an archive DocTimeStamp (PAdES-B-LTA). |
| `contents_size` | int | Size of the signature `/Contents` placeholder (default `16384`). |

### Certification & locking

| Key | Type | Meaning |
| --- | --- | --- |
| `certify` | int | Certification level: `1` = no changes, `2` = form filling, `3` = form filling + annotations. |
| `lock_fields` | `["*"]` or names | Fields to lock after signing (`["*"]` = all). |

### Verification

| Key | Type | Meaning |
| --- | --- | --- |
| `verify` | bool | Verify the certificate before signing. |
| `verify_expiry` | bool | Check certificate validity dates. |
| `verify_crl` | bool | Check the CRL. |
| `verify_ocsp` | bool | Check OCSP. |

### Document security

| Key | Type | Meaning |
| --- | --- | --- |
| `open_password` | string | User/open password for the output PDF. |
| `encrypt_password` | string | Password used to encrypt the output PDF. |
| `owner_password` | string | Owner/permissions password for the output PDF. |

(metadata-options)=
## Metadata options

These keys apply to [`setMetadata`](#documents-utilities).

| Key | Type | Meaning |
| --- | --- | --- |
| `title` | string | Document title. |
| `author` | string | Document author. |
| `subject` | string | Document subject. |
| `keywords` | string | Document keywords. |
| `application` | string | Creating/producing application. |
| `created` | string | Creation date. |
| `modified` | string | Modification date. |

## Exceptions

```java
public static class AtickException extends RuntimeException
```

Thrown by every `Atick` operation on failure — bad password, malformed PDF, network error, invalid
options, and so on. The error text is available from `getMessage()`.

```java
try {
    byte[] signed = Atick.signPfx(pdf, pfx, options);
} catch (Atick.AtickException e) {
    System.err.println("ATick error: " + e.getMessage());
}
```
