package com.zerobase.homemate.auth.security;

import com.zerobase.homemate.auth.service.JwtService;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.enums.UserStatus;
import com.zerobase.homemate.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwtService;
  private final UserRepository userRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {

    String token = resolveToken(request.getHeader(HttpHeaders.AUTHORIZATION));

    try {
      Long userId = jwtService.getSubjectAsLong(token);
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

  // TO DO : CustomException으로 수정
  private String resolveToken(String authorizationHeader) {
    if (!StringUtils.hasText(authorizationHeader)) {
      throw new BadCredentialsException("Missing Authorization header");
    }
    String v = authorizationHeader.trim();
    if (!StringUtils.startsWithIgnoreCase(v, "Bearer ")) {
      throw new BadCredentialsException("Authorization header must be 'Bearer <token>'");
    }
    String token = v.substring(7).trim();
    if (!StringUtils.hasText(token)) {
      throw new BadCredentialsException("Empty bearer token");
    }
    return token;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    // 인증 예외 경로(필터 스킵)
    String path = request.getRequestURI();
    return path.startsWith("/auth/") || path.startsWith("/policies/") || path.startsWith("/recommend/");
  }
}
