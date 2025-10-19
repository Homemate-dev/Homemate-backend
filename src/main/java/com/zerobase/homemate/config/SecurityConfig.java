package com.zerobase.homemate.config;

import com.zerobase.homemate.auth.security.JwtAuthenticationFilter;
import com.zerobase.homemate.auth.security.RestAccessDeniedHandler;
import com.zerobase.homemate.auth.security.RestAuthenticationEntryPoint;
import com.zerobase.homemate.auth.service.JwtService;
import com.zerobase.homemate.auth.token.AccessTokenBlocklist;
import com.zerobase.homemate.repository.UserRepository;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final JwtService jwtService;
  private final AccessTokenBlocklist accessTokenBlocklist;
  private final UserRepository userRepository;

  private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
  private final RestAccessDeniedHandler restAccessDeniedHandler;

  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter() {
    return new JwtAuthenticationFilter(jwtService, userRepository, accessTokenBlocklist);
  }

  // 전역(서블릿) 자동 등록 OFF
  @Bean
  public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(
      JwtAuthenticationFilter filter) {
    var reg = new FilterRegistrationBean<>(filter);
    reg.setEnabled(false);
    return reg;
  }

  // 공개 URL 집합
  private final RequestMatcher publicMatchers = request -> {
    String uri = request.getRequestURI();
    return uri.startsWith("/auth/login/")
        || uri.equals("/auth/refresh")
        || uri.startsWith("/auth/dev/");
  };

  // 보호해야 하는 주요 API 패턴
  private static final String[] PROTECTED_PATTERNS = {
      "/users/**",
      "/chore/**",
      "/notifications/**",
      "/missions",
      "/push/subscriptions",
      "/auth/logout"
  };

  /** 공개 체인: JWT 필터 등록하지 않음 */
  @Bean
  @Order(1)
  public SecurityFilterChain publicChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(restAuthenticationEntryPoint)
            .accessDeniedHandler(restAccessDeniedHandler)
        )
        .securityMatcher("/auth/login/**", "/auth/refresh", "/auth/dev/**")
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .logout(AbstractHttpConfigurer::disable);
    return http.build();
  }

  /** 보호 체인: JWT 필터 적용 */
  @Bean
  @Order(2)
  public SecurityFilterChain appChain(HttpSecurity http, JwtAuthenticationFilter filter) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(restAuthenticationEntryPoint)
            .accessDeniedHandler(restAccessDeniedHandler)
        )
        .securityMatcher(new NegatedRequestMatcher(publicMatchers))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.DELETE, "/push/subscriptions").permitAll()
            .requestMatchers(HttpMethod.POST, "/auth/logout").authenticated()
            .requestMatchers(HttpMethod.POST, "/recommend/**").authenticated()
            .requestMatchers(HttpMethod.GET , "/recommend/**").permitAll()
            .requestMatchers(PROTECTED_PATTERNS).authenticated()
            .anyRequest().permitAll()
        )
        .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  @Order(99)
  public SecurityFilterChain fallbackChain(HttpSecurity http) throws Exception {
    return http
        .securityMatcher(req -> true)
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(a -> a.anyRequest().permitAll())
        .build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("*"));

    // 배포 후 추가(예시)
    // config.addAllowedOrigin("https://www.homemate.com");

    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setExposedHeaders(List.of("Authorization", "Location", "Link", "X-Total-Count"));
    config.setMaxAge(Duration.ofHours(1));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
