package com.zerobase.homemate.auth.token;

import java.time.Duration;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenStore {
  private final StringRedisTemplate redis;

  @Value("${auth.jwt.refresh-exp-seconds}")
  private long refreshExp;

  private String key(long userId, String sid) {
    return String.format("RT:%d:%s", userId, sid);
  }

  public void save(long userId, String sid, String jti) {
    redis.opsForValue().set(key(userId, sid), jti, Duration.ofSeconds(refreshExp));
  }

  public boolean matchesCurrentJti(long userId, String sid, String jti) {
    return jti.equals(redis.opsForValue().get(key(userId, sid)));
  }

  public void delete(long userId, String sid) {
    redis.delete(key(userId, sid));
  }

  /** 원자 회전(CAS), 현재 jti가 일치할 때만 새로운 jti로 교체 */
  public boolean rotate(long userId, String sid, String currentJti, String nextJti) {
    return Long.valueOf(1L).equals(
        redis.execute(ROTATE_SCRIPT, Collections.singletonList(key(userId, sid)),
            currentJti, nextJti, String.valueOf(refreshExp)));
  }

  private static final DefaultRedisScript<Long> ROTATE_SCRIPT = new DefaultRedisScript<>(
      """
        local key=KEYS[1];
        local expect=ARGV[1];
        local next=ARGV[2];
        local ttl=tonumber(ARGV[3]);
        
        local currentJti = redis.call('GET', key)
        if currentJti == expect then
          redis.call('SET', key, next, 'EX', ttl)
          return 1
        else
          return 0
        end
        """, Long.class);
}
