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

  public String createAccessToken(User user, String sid) {
    Instant now = Instant.now();
    return Jwts.builder()
        .id(UUID.randomUUID().toString())
        .subject(user.getId().toString())
        .claim("type", "AT")
        .claim("sid", sid)
        .claim("nickname", user.getProfileName())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(accessExp)))
        .signWith(key)
        .compact();
  }

  public String createRefreshToken(Long userId, String sid) {
    Instant now = Instant.now();
    return Jwts.builder()
        .id(UUID.randomUUID().toString())
        .subject(userId.toString())
        .claim("type", "RT")
        .claim("sid", sid)
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

  public String getType(String token) {
    return parse(token).getPayload().get("type", String.class);
  }

  public String getSid(String token) {
    return parse(token).getPayload().get("sid", String.class);
  }

  public String getJti(String token) {
    return parse(token).getPayload().getId();
  }

  public long getAccessTokenValiditySeconds() {
    return accessExp;
  }

  public long getRefreshTokenValiditySeconds() {
    return refreshExp;
  }
}
