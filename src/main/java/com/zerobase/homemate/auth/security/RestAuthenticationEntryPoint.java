package com.zerobase.homemate.auth.security;

import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.exception.ErrorResponse;
import com.zerobase.homemate.exception.web.ErrorResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final ErrorResponseWriter writer;

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException) throws IOException {

    ErrorCode code = ErrorCode.UNAUTHORIZED;
    writer.writeJson(response, code.getHttpStatus(), ErrorResponse.of(code));
  }
}
