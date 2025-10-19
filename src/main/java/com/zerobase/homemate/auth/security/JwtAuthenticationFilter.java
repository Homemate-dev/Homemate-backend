package com.zerobase.homemate.auth.security;

import com.zerobase.homemate.auth.service.JwtService;
import com.zerobase.homemate.auth.support.BearerTokenExtractor;
import com.zerobase.homemate.auth.token.AccessTokenBlocklist;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.enums.UserStatus;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwtService;
  private final UserRepository userRepository;
  private final AccessTokenBlocklist accessTokenBlocklist;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {

    String token = BearerTokenExtractor.resolveBearerToken(request.getHeader(HttpHeaders.AUTHORIZATION));
    Claims claims = jwtService.parseOrThrow(token);

    if (claims.get("type", String.class).equals("AT")) {
      if (accessTokenBlocklist.isBlocked(claims.getId())) {
        throw new CustomException(ErrorCode.TOKEN_EXPIRED);
      }
    }

    try {
      Long userId = Long.parseLong(claims.getSubject());
      User user = userRepository.findById(userId).orElse(null);

      if (user != null && user.getUserStatus() == UserStatus.ACTIVE) {
        UserPrincipal principal = UserPrincipal.from(user);

        var auth = new UsernamePasswordAuthenticationToken(
            principal,null, principal.authorities());

        SecurityContextHolder.getContext().setAuthentication(auth);
      }
    } catch (JwtException | NumberFormatException e) {
      SecurityContextHolder.clearContext();
    }

    chain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    // 0) 공통: CORS preflight 는 무조건 스킵
    RequestMatcher optionsMatcher = req -> "OPTIONS".equalsIgnoreCase(req.getMethod());
    if (optionsMatcher.matches(request)) return true;

    // 1) 로그아웃은 반드시 인증 필요
    RequestMatcher logoutMatcher = new AntPathRequestMatcher("/auth/logout");
    if (logoutMatcher.matches(request)) return false;

    // 2) 푸시 구독 삭제는 인증 필요 (DELETE /push/subscriptions)
    RequestMatcher deletePushMatcher = req ->
        "DELETE".equalsIgnoreCase(req.getMethod()) &&
            new AntPathRequestMatcher("/push/subscriptions").matches(req);
    if (deletePushMatcher.matches(request)) return true;

    // 3) 공개(인증 스킵) 엔드포인트 화이트리스트
    RequestMatcher publicMatchers = new OrRequestMatcher(
        new AntPathRequestMatcher("/auth/login/**"),
        new AntPathRequestMatcher("/auth/refresh"),
        new AntPathRequestMatcher("/auth/dev/**")
    );

    return publicMatchers.matches(request);
  }
}
