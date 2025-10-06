package com.zerobase.homemate.auth.token;

import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import java.time.Duration;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
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
    try {
      redis.opsForValue().set(key(userId, sid), jti, Duration.ofSeconds(refreshExp));
    } catch (RedisConnectionFailureException e) {
      throw new CustomException(ErrorCode.REFRESH_STORE_UNAVAILABLE);
    }
  }

  public boolean isCurrent(long userId, String sid, String jti) {
    try {
      return jti.equals(redis.opsForValue().get(key(userId, sid)));
    } catch (RedisConnectionFailureException e) {
      throw new CustomException(ErrorCode.REFRESH_STORE_UNAVAILABLE);
    }
  }

  public void delete(long userId, String sid) {
    try {
      redis.delete(key(userId, sid));
    } catch (RedisConnectionFailureException e) {
      throw new CustomException(ErrorCode.REFRESH_STORE_UNAVAILABLE);
    }
  }

  /** 원자 회전(CAS), 현재 jti가 일치할 때만 새로운 jti로 교체 */
  public boolean rotate(long userId, String sid, String currentJti, String nextJti) {
    String key = key(userId, sid);
    try {
      return Long.valueOf(1L).equals(
          redis.execute(ROTATE_SCRIPT, Collections.singletonList(key),
          currentJti, nextJti, String.valueOf(refreshExp)));

    } catch (RedisConnectionFailureException e) {
      throw new CustomException(ErrorCode.REFRESH_STORE_UNAVAILABLE);
    }
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
