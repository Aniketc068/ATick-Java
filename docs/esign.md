# Indian eSign (CCA)

ATick for Java supports the CCA eSign Online Electronic Signature Service for **every API version**
(v1.x … v3.x). The flow is the same across versions — only the request XML attributes differ. The
same two-step pattern also covers any **remote key**: an HSM, USB token, smart-card, or the Windows
certificate store.

```
PDF  ->  SHA-256 of the ByteRange (the InputHash, hex)
     ->  build the <Esign …> request XML for your version, put the InputHash in <InputHash>
     ->  sign the request XML (your own means / your ESP's SDK)   [enveloped W3C XML-DSig]
     ->  POST it (multipart/form-data) to the ESP
     ->  EsignResp -> <DocSignature> (pkcs7 / pkcs7Pdf / pkcs7complete)
     ->  embed it into the PDF
```

```java
import io.github.aniketc068.atick.Atick;
import java.nio.file.*;
import java.security.MessageDigest;
```

Every call takes its configuration as a single **JSON options string**, and every failure throws
`Atick.AtickException`.

## Step 1 — prepare + hash

`Atick.prepare` returns a two-element array: index **0** is the prepared PDF, index **1** is the
exact bytes that must be signed (the ByteRange). The eSign **InputHash** is simply the SHA-256 of
index 1.

```java
byte[] pdf = Files.readAllBytes(Path.of("in.pdf"));

// options: cn, reason, placements / page+rect, field_name, pades, contents_size.
// Leave room for the chain + revocation + timestamp that a pkcs7Pdf reply carries.
byte[][] pr = Atick.prepare(pdf,
    "{\"cn\":\"Aniket\",\"reason\":\"Agreement\",\"pades\":true,"
  + "\"page\":1,\"rect\":[40,640,300,750],\"contents_size\":60000}");

byte[] prepared    = pr[0];
byte[] bytesToSign = pr[1];

// The InputHash that goes into <InputHash> (hex).
byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytesToSign);
StringBuilder sb = new StringBuilder();
for (byte b : digest) sb.append(String.format("%02x", b));
String inputHashHex = sb.toString();
```

## Step 2 — build and sign the request XML

Put `inputHashHex` into `<InputHash>`, then sign the request XML (an enveloped W3C XML-DSig) with
your own means — your ASP signing key or your ESP's SDK — and POST it to the ESP.

```java
String request =
    "<Esign ver=\"2.1\" sc=\"Y\" ts=\"…\" txn=\"TXN1\" ekycIdType=\"A\" aspId=\"…\" "
  + "AuthMode=\"1\" responseSigType=\"pkcs7Pdf\" responseUrl=\"https://…/\"><Docs>"
  + "<InputHash id=\"1\" hashAlgorithm=\"SHA256\" docInfo=\"Agreement\">"
  + inputHashHex
  + "</InputHash></Docs></Esign>";

// Sign `request` (enveloped XML-DSig) with your own means / your ESP's SDK,
// then POST the signed XML (multipart/form-data) to the ESP.
```

```{note}
The request XML is signed with **your ASP credential**, not with ATick. ATick's job is the PDF: it
produced `inputHashHex` from the ByteRange in step 1, and it will embed the ESP's reply in step 3.
```

## Step 3 — embed the ESP response

The `EsignResp` carries the signature in `<DocSignature>` (Base64). Decode it and pass the resulting
CMS bytes to `Atick.embed`, together with the prepared PDF from step 1.

```java
byte[] cms = java.util.Base64.getDecoder().decode(docSignatureBase64);  // from <DocSignature>

byte[] signed = Atick.embed(prepared, cms);
Files.write(Path.of("signed.pdf"), signed);
```

`pkcs7Pdf` and `pkcs7complete` responses already carry the full chain, the revocation (under
`pdfRevocationInfoArchival`) and a CA timestamp — so the embedded signature is **LTV-complete and
timestamped** out of the box.

## `responseSigType`

| Value | Returns | Embed with |
|---|---|---|
| `pkcs7` | a CMS, signer cert only (no revocation) | `Atick.embed` |
| `pkcs7Pdf` | a CMS, full chain + CRL/OCSP (signed attr) + timestamp | `Atick.embed` |
| `pkcs7complete` | a CMS, full chain + revocation (unsigned attr) | `Atick.embed` |

Request a `pkcs7Pdf` or `pkcs7complete` reply so the embedded signature is LTV-complete.

## Other remote keys — HSM, token, card, Windows store

The same three steps cover any key that never leaves its holder. Instead of POSTing to an ESP, sign
`bytesToSign` directly with your own JCA provider and produce a **detached CMS / PKCS#7 SignedData**:

- **HSM / USB token / smart-card** — `SunPKCS11` (or your vendor's PKCS#11 provider).
- **Windows certificate store** — `SunMSCAPI`.

```java
byte[][] pr = Atick.prepare(pdf, "{\"cn\":\"Aniket\",\"reason\":\"Approved\",\"pades\":true}");

// Sign pr[1] with your JCA provider; return a detached CMS over those exact bytes.
byte[] cms = signWithMyProvider(pr[1]);   // SunPKCS11 / SunMSCAPI / vendor provider

byte[] signed = Atick.embed(pr[0], cms);
Files.write(Path.of("signed.pdf"), signed);
```

```{tip}
The CMS you build in step 2 must cover **`pr[1]`** exactly and use the same hash algorithm
(SHA-256 by default) that ATick used to prepare the document. ATick owns the PDF structure; your
provider owns the private key.
```

## Simulating the ESP for testing

To run the whole flow end-to-end without a live ESP, build the detached CMS yourself from a
credential file with `Atick.cmsPfx`. It stands in for the external signer, producing a
`pkcs7Pdf`-style CMS over `pr[1]`:

```java
byte[] pfx = Files.readAllBytes(Path.of("signer.pfx"));

byte[][] pr   = Atick.prepare(pdf, "{\"cn\":\"Aniket\",\"pades\":true}");
byte[]   cms  = Atick.cmsPfx(pr[1], pfx,
                   "{\"password\":\"••••\",\"pades\":true,\"timestamp\":true}");
byte[]   done = Atick.embed(pr[0], cms);

Files.write(Path.of("signed.pdf"), done);
```
