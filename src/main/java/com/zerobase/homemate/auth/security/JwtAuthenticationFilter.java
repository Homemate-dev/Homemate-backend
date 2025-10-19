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
    return "OPTIONS".equalsIgnoreCase(request.getMethod());
  }
}
