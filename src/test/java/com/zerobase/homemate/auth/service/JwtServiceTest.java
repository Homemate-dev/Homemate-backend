package com.zerobase.homemate.auth.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.enums.UserRole;
import com.zerobase.homemate.entity.enums.UserStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
  @Test
  @DisplayName("토큰 생성/파싱")
  void create_and_parse_tokens() {
    // given
    var secret = "0123456789abcdefghijklmnopqrstuvwxyzSECRET-256!!!!";
    long accessExp = 600;   // 10m
    long refreshExp = 3600; // 1h
    var jwt = new JwtService(secret, accessExp, refreshExp);

    var user = User.builder()
        .id(1L).profileName("test")
        .userRole(UserRole.USER).userStatus(UserStatus.ACTIVE)
        .build();

    // when
    Instant before = Instant.now();
    String at = jwt.createAccessToken(user);
    String rt = jwt.createRefreshToken(user.getId());
    Instant after = Instant.now();

    Instant expAt = jwt.getExpiry(at);
    Instant expRt = jwt.getExpiry(rt);

    // then
    Jws<Claims> parsed = jwt.parse(at);
    assertThat(parsed.getBody().getSubject()).isEqualTo("1");
    assertThat(parsed.getBody().get("nickname")).isEqualTo("test");
    assertThat(expAt).isAfterOrEqualTo(before.truncatedTo(ChronoUnit.SECONDS).plusSeconds(accessExp));
    assertThat(expAt).isBeforeOrEqualTo(after.truncatedTo(ChronoUnit.SECONDS).plusSeconds(accessExp));

    assertThat(jwt.getSubjectAsLong(rt)).isEqualTo(1L);
    assertThat(expRt).isAfterOrEqualTo(before.truncatedTo(ChronoUnit.SECONDS).plusSeconds(refreshExp));
    assertThat(expRt).isBeforeOrEqualTo(after.truncatedTo(ChronoUnit.SECONDS).plusSeconds(refreshExp));
  }
}
