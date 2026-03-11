package com.zerobase.homemate.dev;

import com.zerobase.homemate.auth.service.JwtService;
import com.zerobase.homemate.auth.support.RefreshTokenCookieFactory;
import com.zerobase.homemate.auth.token.RefreshTokenStore;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.repository.UserRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ConditionalOnProperty(
    prefix = "auth.dev",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = false
)
@RestController
@RequestMapping("/auth/dev")
@RequiredArgsConstructor
public class DevAuthController {
  private final JwtService jwtService;
  private final RefreshTokenStore refreshTokenStore;
  private final UserRepository userRepository;
  private final RefreshTokenCookieFactory refreshTokenCookieFactory;

  // 개발/테스트 전용 토큰 발급 엔드포인트
  @PostMapping("/token/{id}")
  public ResponseEntity<Map<String, String>> token(@PathVariable Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    String sid = UUID.randomUUID().toString();
    String at = jwtService.createAccessToken(user, sid);
    String rt = jwtService.createRefreshToken(user.getId(), sid);
    refreshTokenStore.save(user.getId(), sid, jwtService.getJti(rt));

    Map<String, String> response = new LinkedHashMap<>();
    response.put("accessToken", at);
    ResponseCookie responseCookie = refreshTokenCookieFactory.fromRefreshToken(rt);

    return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
            .body(response);
  }
}
