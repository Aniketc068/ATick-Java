# PAdES levels

ATick produces all four PAdES baseline levels. Adobe Acrobat shows the level in the advanced
signature properties.

| Level | Options | What it adds |
|---|---|---|
| **B-B** | `"pades":true` | a PAdES (CAdES) signature with the ESS signing-certificate-v2 attribute |
| **B-T** | `+ "timestamp":true` | an RFC-3161 signature timestamp |
| **B-LT** | `+ "ltv":true` | the DSS: full chain + CRLs + OCSP responses + per-signature VRI |
| **B-LTA** | `+ "lta":true` | a document timestamp over the whole file |

Options are passed as a JSON string. Failures throw `Atick.AtickException`.

```java
import io.github.aniketc068.atick.Atick;
import java.nio.file.*;

byte[] pdf = Files.readAllBytes(Path.of("input.pdf"));
byte[] pfx = Files.readAllBytes(Path.of("signer.pfx"));

// B-B
byte[] bb = Atick.signPfx(pdf, pfx,
        "{\"password\":\"••••\",\"pades\":true}");

// B-T
byte[] bt = Atick.signPfx(pdf, pfx,
        "{\"password\":\"••••\",\"pades\":true,\"timestamp\":true}");

// B-LT
byte[] blt = Atick.signPfx(pdf, pfx,
        "{\"password\":\"••••\",\"pades\":true,\"timestamp\":true,\"ltv\":true}");

// B-LTA
byte[] blta = Atick.signPfx(pdf, pfx,
        "{\"password\":\"••••\",\"pades\":true,\"timestamp\":true,\"lta\":true}");

Files.write(Path.of("signed.pdf"), blta);
```

For **B-LT** and **B-LTA** ATick embeds the complete validation material (the signer chain, its
CRLs and full `OCSPResponse`s, the OCSP responder certificates, a per-signature VRI, and the
`/Extensions /ESIC` declaration) so Adobe reports **"PAdES Signature Level: B-LT"**.

```{note}
Each level is cumulative: `"lta":true` implies the document timestamp on top of B-LT validation
material, so a B-LTA call typically sets `"pades"`, `"timestamp"`, `"ltv"`, and `"lta"` together.
```

## PAdES vs. plain CMS, and `/M`

- `"pades":true` → SubFilter `ETSI.CAdES.detached`; the signature dictionary carries `/M` (signing
  time), which Adobe uses to classify the PAdES level.
- `"pades":false` → SubFilter `adbe.pkcs7.detached`, a plain PKCS#7 signature with **no `/M`**.

## Custom TSA

The timestamp authority is configurable. Set `tsa_url` to your RFC-3161 endpoint, and supply
HTTP Basic credentials with `tsa_auth` (a `["user","pass"]` pair) when the TSA requires them.
`hash_algo` selects the digest (`"sha256"`, `"sha384"`, or `"sha512"`).

```java
import io.github.aniketc068.atick.Atick;
import java.nio.file.*;

byte[] pdf = Files.readAllBytes(Path.of("input.pdf"));
byte[] pfx = Files.readAllBytes(Path.of("signer.pfx"));

byte[] signed = Atick.signPfx(pdf, pfx,
        "{\"password\":\"••••\",\"pades\":true,\"timestamp\":true,\"ltv\":true,"
      + "\"tsa_url\":\"https://tsa.example.com/tsr\","
      + "\"tsa_auth\":[\"user\",\"pass\"],"
      + "\"hash_algo\":\"sha256\"}");
```

## Document timestamp on an existing signature

`Atick.addDocTimestamp` adds an archive `DocTimeStamp` over the whole file, upgrading an
already-signed PDF to **B-LTA**. It takes the same JSON options (for example `tsa_url` and
`tsa_auth`) so the archive timestamp can use a custom TSA.

```java
import io.github.aniketc068.atick.Atick;
import java.nio.file.*;

byte[] signedPdf = Files.readAllBytes(Path.of("signed.pdf"));

byte[] archived = Atick.addDocTimestamp(signedPdf,
        "{\"tsa_url\":\"https://tsa.example.com/tsr\"}");

Files.write(Path.of("signed-lta.pdf"), archived);
```

```{tip}
Call `Atick.version()` to confirm the library build in use when reporting an issue.
```
