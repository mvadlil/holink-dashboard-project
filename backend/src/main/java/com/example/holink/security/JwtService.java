package com.example.holink.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.holink.entity.User;
import com.example.holink.exception.UnauthorizedException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class JwtService {

    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;
    private final byte[] secretBytes;
    private final long expirationMinutes;

    public JwtService(ObjectMapper objectMapper,
                      @Value("${auth.jwt.secret}") String secret,
                      @Value("${auth.jwt.expiration-minutes}") long expirationMinutes) {
        this.objectMapper = objectMapper;
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(User user) {
        try {
            long issuedAt = Instant.now().getEpochSecond();
            long expiresAt = Instant.now().plusSeconds(expirationMinutes * 60).getEpochSecond();

            Map<String, Object> header = Map.of(
                    "alg", "HS256",
                    "typ", "JWT"
            );

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", user.getId());
            payload.put("name", user.getName());
            payload.put("email", user.getEmail());
            payload.put("iat", issuedAt);
            payload.put("exp", expiresAt);

            String encodedHeader = encodeJson(header);
            String encodedPayload = encodeJson(payload);
            String signature = sign(encodedHeader + "." + encodedPayload);

            return encodedHeader + "." + encodedPayload + "." + signature;
        } catch (UnauthorizedException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UnauthorizedException("Failed to generate token");
        }
    }

    public String extractUserId(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new UnauthorizedException("Invalid token");
            }

            String signingInput = parts[0] + "." + parts[1];
            String expectedSignature = sign(signingInput);
            if (!MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    parts[2].getBytes(StandardCharsets.UTF_8))) {
                throw new UnauthorizedException("Invalid token");
            }

            Map<String, Object> payload = objectMapper.readValue(
                    URL_DECODER.decode(parts[1]),
                    MAP_TYPE
            );

            Number exp = (Number) payload.get("exp");
            String subject = (String) payload.get("sub");
            if (exp == null || subject == null || subject.isBlank()) {
                throw new UnauthorizedException("Invalid token");
            }
            if (exp.longValue() <= Instant.now().getEpochSecond()) {
                throw new UnauthorizedException("Token has expired");
            }

            return subject;
        } catch (UnauthorizedException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UnauthorizedException("Invalid token");
        }
    }

    private String encodeJson(Map<String, Object> value) throws Exception {
        byte[] bytes = objectMapper.writeValueAsBytes(value);
        return URL_ENCODER.encodeToString(bytes);
    }

    private String sign(String signingInput) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secretBytes, "HmacSHA256"));
        byte[] signatureBytes = mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8));
        return URL_ENCODER.encodeToString(signatureBytes);
    }
}
