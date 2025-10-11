package com.zerobase.homemate.config;

import com.zerobase.homemate.auth.security.JwtAuthenticationFilter;
import com.zerobase.homemate.auth.service.JwtService;
import com.zerobase.homemate.auth.token.AccessTokenBlocklist;
import com.zerobase.homemate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final JwtService jwtService;
  private final AccessTokenBlocklist accessTokenBlocklist;
  private final UserRepository userRepository;

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    JwtAuthenticationFilter jwtFilter =
        new JwtAuthenticationFilter(jwtService, userRepository, accessTokenBlocklist);

    http
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/login/**", "/auth/refresh", "/auth/dev/**", "/policies/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/auth/logout").authenticated()
            .anyRequest().authenticated()
        )
//        .exceptionHandling() 예외처리 추가 예정
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .logout(AbstractHttpConfigurer::disable);

    return http.build();
  }
}
