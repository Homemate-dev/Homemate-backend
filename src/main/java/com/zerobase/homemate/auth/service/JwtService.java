package com.zerobase.homemate.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Jwts;
import com.zerobase.homemate.entity.User;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final SecretKey key;
  private final long accessExp;
  private final long refreshExp;

  public JwtService(
      @Value("${auth.jwt.secret}") String secret,
      @Value("${auth.jwt.access-exp-seconds}") long accessExp,
      @Value("${auth.jwt.refresh-exp-seconds}") long refreshExp) {

    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.accessExp = accessExp;
    this.refreshExp = refreshExp;
  }

  public String createAccessToken(User user) {
    Instant now = Instant.now();
    return Jwts.builder()
        .id(UUID.randomUUID().toString())
        .subject(user.getId().toString())
        .claim("nickname", user.getProfileName())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(accessExp)))
        .signWith(key)
        .compact();
  }

  public String createRefreshToken(Long userId) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(userId.toString())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(refreshExp)))
        .signWith(key)
        .compact();
  }

  public Jws<Claims> parse(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
  }

  public Long getSubjectAsLong(String token) {
    String sub = parse(token).getPayload().getSubject();
    return Long.parseLong(sub);
  }

  // 만료시각 반환
  public Instant getExpiry(String token) {
    return parse(token).getPayload().getExpiration().toInstant();
  }

  public long getATValiditySeconds() {
    return accessExp;
  }

  public long getRTValiditySeconds() {
    return refreshExp;
  }
}
