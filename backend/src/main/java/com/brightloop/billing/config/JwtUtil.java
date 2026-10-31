package com.brightloop.billing.config;

import com.brightloop.billing.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minimal HMAC-SHA256 JWT — no external libraries needed.
 * Format: Base64url(header).Base64url(payload).Base64url(signature)
 */
@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms:86400000}")
    private long expirationMs;

    public String generate(User user) {
        long now = System.currentTimeMillis();
        String header  = b64("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = b64(String.format(
            "{\"sub\":\"%d\",\"email\":\"%s\",\"role\":\"%s\",\"iat\":%d,\"exp\":%d}",
            user.getId(), esc(user.getEmail()), user.getRole().name(),
            now / 1000, (now + expirationMs) / 1000));
        String sig = sign(header + "." + payload);
        return header + "." + payload + "." + sig;
    }

    public Map<String, String> parse(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) throw new IllegalArgumentException("Invalid token");
        String expected = sign(parts[0] + "." + parts[1]);
        if (!expected.equals(parts[2])) throw new IllegalArgumentException("Invalid signature");
        String json = new String(Base64.getUrlDecoder().decode(pad(parts[1])), StandardCharsets.UTF_8);
        Map<String, String> map = new ConcurrentHashMap<>();
        for (String pair : json.replaceAll("[{}\"]", "").split(",")) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) map.put(kv[0].trim(), kv[1].trim());
        }
        long exp = Long.parseLong(map.getOrDefault("exp", "0"));
        if (System.currentTimeMillis() / 1000 > exp) throw new IllegalArgumentException("Token expired");
        return map;
    }

    public Long getUserId(String token) {
        return Long.parseLong(parse(token).get("sub"));
    }

    public String getRole(String token) {
        return parse(token).get("role");
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private static String b64(String s) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    private static String pad(String s) {
        int p = s.length() % 4;
        return p == 0 ? s : s + "====".substring(p);
    }

    private static String esc(String s) { return s.replace("\\", "\\\\").replace("\"", "\\\""); }
}
