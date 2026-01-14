package org.reserv.reserveme.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.reserv.reserveme.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final Key key;
    private final RefreshTokenRepository refreshTokenRepository;

    public JwtService(@Value("${app.jwt.secret:}") String secret, RefreshTokenRepository refreshTokenRepository) {
        // Use provided secret or a fallback (not recommended for production)
        if (secret == null || secret.isBlank()) {
            // Insecure fallback for local dev only
            secret = "insecure-default-change-me-in-prod-with-a-long-secret-key";
        }
        // Ensure key length is sufficient for HMAC-SHA algorithms
        byte[] keyBytes = secret.getBytes();
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(1, ChronoUnit.HOURS))) // 1h
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        // Create a DB-backed refresh token as a UUID and persist it with expiry
        var expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);
        var refreshToken = new RefreshToken(user, expiresAt);
        var saved = refreshTokenRepository.save(refreshToken);
        return saved.getToken().toString();
    }

    public Map<String, Object> parseToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return claims;
    }

    public Key getKey() { return key; }

    public java.util.Optional<RefreshToken> findRefreshToken(java.util.UUID token) {
        return refreshTokenRepository.findByToken(token);
    }

    public void deleteRefreshToken(RefreshToken t) {
        refreshTokenRepository.delete(t);
    }
}
