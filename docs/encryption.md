# Encryption

ATick reads and writes password-protected PDFs through the same `Atick.signPfx` entry point,
plus a dedicated `Atick.decrypt` helper. All passwords are passed as keys inside the
options JSON string.

```java
import io.github.aniketc068.atick.Atick;
import java.nio.file.*;
```

| Option key        | Applies to    | Meaning                                                              |
| ----------------- | ------------- | -------------------------------------------------------------------- |
| `open_password`   | Input PDF     | Password used to open an already-encrypted PDF before signing it.    |
| `encrypt_password`| Output PDF    | User password — required to open the signed PDF that ATick produces. |
| `owner_password`  | Output PDF    | Owner/permissions password for the signed output (optional).         |

## Password-protect the output

Add `encrypt_password` to encrypt the signed PDF that ATick writes. Supply `owner_password`
as well to set a separate owner/permissions password; if you omit it, the owner password
defaults to the user password.

```java
import io.github.aniketc068.atick.Atick;
import java.nio.file.*;

byte[] pdf = Files.readAllBytes(Path.of("contract.pdf"));
byte[] pfx = Files.readAllBytes(Path.of("signer.pfx"));

byte[] signed = Atick.signPfx(pdf, pfx,
    "{\"password\":\"••••\",\"encrypt_password\":\"open-me\",\"owner_password\":\"owner\"}");

Files.write(Path.of("contract-signed.pdf"), signed);
```

```{admonition} The signature stays valid
:class: note
The output is AES-128 encrypted. The signature's `/Contents` is exempt from encryption,
so the signed byte range still verifies in any compliant PDF reader.
```

## Sign an encrypted input

If the input PDF is already password-protected, pass `open_password` so ATick can open it
before signing. The decrypted document is signed and then written back out (encrypt the
output again with `encrypt_password` if you want the result to stay protected).

```java
import io.github.aniketc068.atick.Atick;
import java.nio.file.*;

byte[] pdf = Files.readAllBytes(Path.of("locked.pdf"));
byte[] pfx = Files.readAllBytes(Path.of("signer.pfx"));

byte[] signed = Atick.signPfx(pdf, pfx,
    "{\"password\":\"••••\",\"open_password\":\"the-input-password\"}");

Files.write(Path.of("locked-signed.pdf"), signed);
```

```{tip}
You can combine the keys: open an encrypted input with `open_password` and re-encrypt the
signed output in one call by also passing `encrypt_password` (and optionally `owner_password`).
```

## Decrypt a PDF

Use `Atick.decrypt` to strip the password protection from a PDF and obtain its plaintext bytes.

```java
import io.github.aniketc068.atick.Atick;
import java.nio.file.*;

byte[] encrypted = Files.readAllBytes(Path.of("locked.pdf"));

byte[] plain = Atick.decrypt(encrypted, "the-password");

Files.write(Path.of("unlocked.pdf"), plain);
```

## Handling failures

Both `Atick.signPfx` and `Atick.decrypt` throw `Atick.AtickException` on failure — for
example, when a password is wrong or the input PDF is not actually encrypted.

```java
import io.github.aniketc068.atick.Atick;
import java.nio.file.*;

try {
    byte[] plain = Atick.decrypt(Files.readAllBytes(Path.of("locked.pdf")), "wrong-pw");
    Files.write(Path.of("unlocked.pdf"), plain);
} catch (Atick.AtickException e) {
    System.err.println("Could not decrypt PDF: " + e.getMessage());
}
```
