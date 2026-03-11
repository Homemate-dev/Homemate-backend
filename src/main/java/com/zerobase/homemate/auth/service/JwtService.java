package com.zerobase.homemate.auth.service;

import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Jwts;
import com.zerobase.homemate.entity.User;
import io.jsonwebtoken.security.WeakKeyException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.function.Consumer;
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
    return buildToken(b -> b
        .subject(user.getId().toString())
        .claim("type", "AT")
        .claim("sid", sid)
        .claim("nickname", user.getProfileName())
        .expiration(Date.from(Instant.now().plusSeconds(accessExp))));
  }

  public String createRefreshToken(Long userId, String sid) {
    return buildToken(b -> b
        .subject(userId.toString())
        .claim("type", "RT")
        .claim("sid", sid)
        .expiration(Date.from(Instant.now().plusSeconds(refreshExp))));
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

  private String buildToken(Consumer<JwtBuilder> customizer) {
    try {
      Instant now = Instant.now();
      JwtBuilder jwtBuilder = Jwts.builder()
          .id(UUID.randomUUID().toString())
          .issuedAt(Date.from(now))
          .signWith(key);

      customizer.accept(jwtBuilder);
      return jwtBuilder.compact();

    } catch (WeakKeyException e) {
      throw new CustomException(ErrorCode.JWT_SIGNING_KEY_INVALID);
    } catch (JwtException | IllegalArgumentException e) {
      throw new CustomException(ErrorCode.JWT_BUILD_FAILED);
    }
  }
}
