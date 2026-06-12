# Fast signing

Fast signing is **ON by default**. When LTV is enabled, the first signature fetches the
certificate's CRL/OCSP over the network; ATick then keeps that revocation in an in-memory cache, so
every later signature **with the same certificate** reuses it instead of fetching again. This is a
large speed-up for batch and multi-signature runs (≈ 6× in practice).

```java
import io.github.aniketc068.atick.Atick;

Atick.setFastSigning(true);    // default — reuse cached revocation for the same certificate
Atick.setFastSigning(false);   // always fetch fresh (also clears the cache)
```

## Signing with LTV

Pass options as a JSON string. The first signature with a given certificate populates the cache;
the rest reuse it.

```java
import io.github.aniketc068.atick.Atick;
import java.nio.file.*;

byte[] pdf = Files.readAllBytes(Paths.get("in.pdf"));
byte[] pfx = Files.readAllBytes(Paths.get("my.pfx"));

String options = "{\"password\":\"secret\",\"ltv\":true}";

byte[] signed = Atick.signPfx(pdf, pfx, options);
Files.write(Paths.get("out.pdf"), signed);
```

## Batch signing

Because the cache is keyed per certificate, signing many PDFs with the **same** `.pfx` fetches
revocation once and reuses it for the rest of the run.

```java
import io.github.aniketc068.atick.Atick;
import java.nio.file.*;

byte[] pfx = Files.readAllBytes(Paths.get("my.pfx"));
String options = "{\"password\":\"secret\",\"ltv\":true}";

String[] inputs = {"a.pdf", "b.pdf", "c.pdf", "d.pdf"};

Atick.setFastSigning(true);   // default; shown here for clarity

for (String name : inputs) {
    byte[] pdf = Files.readAllBytes(Paths.get(name));
    try {
        byte[] signed = Atick.signPfx(pdf, pfx, options);   // first call fetches, rest reuse cache
        Files.write(Paths.get("signed-" + name), signed);
    } catch (Atick.AtickException e) {
        System.err.println("Failed to sign " + name + ": " + e.getMessage());
    }
}
```

## Disabling the cache

To force a fresh CRL/OCSP fetch on every signature, turn fast signing off before signing.

```java
import io.github.aniketc068.atick.Atick;

Atick.setFastSigning(false);   // always fetch fresh, also clears the cache
```

```{tip}
Leave fast signing on for batch runs. Turn it off only when you need each signature to reflect the
very latest revocation state.
```

## Behaviour at a glance

| Setting | First signature | Later signatures (same certificate) | Timestamps |
| --- | --- | --- | --- |
| `setFastSigning(true)` (default) | Fetch CRL/OCSP, cache it | Reuse cached revocation | Always fresh |
| `setFastSigning(false)` | Fetch CRL/OCSP | Fetch CRL/OCSP again | Always fresh |

## Notes

- The cache lives in **process memory** only and is gone when the process ends.
- It is keyed per request, so a **different / removed certificate** simply misses and is fetched
  fresh — there is no risk of reusing the wrong certificate's revocation.
- **Timestamps are never cached** — each signature must carry its own unique RFC-3161 token, so the
  timestamp authority is always contacted per signature.
- Any failure (bad password, network error, malformed PDF) throws `Atick.AtickException`; wrap calls
  in a `try`/`catch` as shown in the batch loop above.
