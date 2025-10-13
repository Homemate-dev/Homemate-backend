package com.zerobase.homemate.auth.support;

import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

@UtilityClass
public final class BearerTokenExtractor {
  private static final String PREFIX = "Bearer ";

  public static String resolveBearerToken(String authorizationHeader) {
    if (!StringUtils.hasText(authorizationHeader)) {
      throw new CustomException(ErrorCode.MISSING_AUTHORIZATION_HEADER);
    }
    String v = authorizationHeader.trim();
    if (!StringUtils.startsWithIgnoreCase(v, "Bearer ")) {
      throw new CustomException(ErrorCode.AUTHORIZATION_MUST_BE_BEARER);
    }
    String token = v.substring(PREFIX.length()).trim();
    if (!StringUtils.hasText(token)) {
      throw new CustomException(ErrorCode.EMPTY_BEARER_TOKEN);
    }
    return token;
  }
}
