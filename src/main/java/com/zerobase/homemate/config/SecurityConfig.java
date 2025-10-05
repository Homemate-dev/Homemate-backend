package com.zerobase.homemate.config;

import com.zerobase.homemate.auth.security.JwtAuthenticationFilter;
import com.zerobase.homemate.auth.service.JwtService;
import com.zerobase.homemate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
  private final UserRepository userRepository;

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService, userRepository);

    http
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/**", "/policies/**").permitAll()
            .anyRequest().authenticated()
        )
//        .exceptionHandling() 예외처리 추가 예정
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .logout(AbstractHttpConfigurer::disable);

    return http.build();
  }
}
