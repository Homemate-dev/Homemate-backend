package com.zerobase.homemate.auth.service;

import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
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

  // 만료/서명 검증 후 Claims 반환
  public Claims parseOrThrow(String token) {
    try {
      return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    } catch (ExpiredJwtException e) {
      throw new CustomException(ErrorCode.TOKEN_EXPIRED);
    } catch (JwtException e) {
      throw new CustomException(ErrorCode.INVALID_TOKEN);
    }
  }

  // 토큰 유형까지 검증하고 Claims 반환
  public Claims parseAndValidateType(String token, String type) {
    Claims claims = parseOrThrow(token);
    if (!type.equals(claims.get("type", String.class))) {
      throw new CustomException(ErrorCode.INVALID_TOKEN_TYPE);
    }
    return claims;
  }

  public Long getSubjectAsLong(String token) {
    return Long.parseLong(parseOrThrow(token).getSubject());
  }

  public Instant getExpiry(String token) {
    return parseOrThrow(token).getExpiration().toInstant();
  }

  public String getType(String token) {
    return parseOrThrow(token).get("type", String.class);
  }

  public String getSid(String token) {
    return parseOrThrow(token).get("sid", String.class);
  }

  public String getJti(String token) {
    return parseOrThrow(token).getId();
  }

  public long getAccessTokenValiditySeconds() {
    return accessExp;
  }

  public long getRefreshTokenValiditySeconds() {
    return refreshExp;
  }
}
