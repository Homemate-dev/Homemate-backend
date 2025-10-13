package com.zerobase.homemate.auth.token;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessTokenBlocklist {
  private final StringRedisTemplate redis;

  private static final String PREFIX = "ATBL:";

  public void block(String jti, Duration ttl) {
    if (ttl.isNegative() || ttl.isZero()) {
      ttl = Duration.ofSeconds(1);
    }
    redis.opsForValue().setIfAbsent(PREFIX + jti, "1", ttl);
  }

  public boolean isBlocked(String jti) {
    return redis.hasKey(PREFIX + jti);
  }
}
