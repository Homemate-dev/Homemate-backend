package com.zerobase.homemate.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import com.zerobase.homemate.entity.Users;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final Key key;
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

  public String createAccessToken(Users user) {
    Instant now = Instant.now();
    return Jwts.builder()
        .setId(UUID.randomUUID().toString())
        .setSubject(user.getId().toString())
        .claim("nickname", user.getProfileName())
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(now.plusSeconds(accessExp)))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public String createRefreshToken(Long userId) {
    Instant now = Instant.now();
    return Jwts.builder()
        .setSubject(userId.toString())
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(now.plusSeconds(refreshExp)))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public Jws<Claims> parse(String token) {
    return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
  }

  public Long getSubjectAsLong(String token) {
    String sub = parse(token).getBody().getSubject();
    return Long.parseLong(sub);
  }

  // 만료시각 반환
  public Instant getExpiry(String token) {
    return parse(token).getBody().getExpiration().toInstant();
  }
}
