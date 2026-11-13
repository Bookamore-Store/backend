package com.bookamore.backend.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Date;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Service
@Slf4j
public class JwtTokenService {
    static final String PASSWORD_FINGERPRINT_CLAIM = "pwd";

    private final Key signingKey;
    private final long jwtExpiration;

    public JwtTokenService(@Value("${JWT_SECRET}") String jwtSecret,
                           @Value("${JWT_EXPIRATION}") long jwtExpiration) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.jwtExpiration = jwtExpiration;
    }

    public String generateToken(UUID userId, String passwordHash) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim(PASSWORD_FINGERPRINT_CLAIM, fingerprint(passwordHash))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(signingKey) // HS256 вибирається автоматично за розміром ключа
                .compact();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final UUID userIdFromToken = extractUserId(token);

            return Optional.ofNullable(userDetails)
                    .filter(JwtUserDetails.class::isInstance)
                    .map(JwtUserDetails.class::cast)
                    .map(user -> user.getId().equals(userIdFromToken)
                            && passwordFingerprintMatches(token, user.getPasswordHash())
                            && !isTokenExpired(token))
                    .orElse(false);
        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractClaim(token, Claims::getSubject));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    private boolean passwordFingerprintMatches(String token, String passwordHash) {
        String expected = fingerprint(passwordHash);
        String actual = extractClaim(token, claims -> claims.get(PASSWORD_FINGERPRINT_CLAIM, String.class));
        if (actual == null) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String fingerprint(String passwordHash) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init((SecretKey) signingKey);
            byte[] digest = mac.doFinal(
                    (passwordHash == null ? "" : passwordHash).getBytes(StandardCharsets.UTF_8)
            );
            return HexFormat.of().formatHex(digest, 0, 8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to compute password fingerprint", e);
        }
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
