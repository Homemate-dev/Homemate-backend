package com.zerobase.homemate.auth.security;

import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.exception.ErrorResponse;
import com.zerobase.homemate.exception.web.ErrorResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {
  private final ErrorResponseWriter writer;

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      AccessDeniedException accessDeniedException) throws IOException {

    ErrorCode code = ErrorCode.FORBIDDEN;
    writer.writeJson(response, code.getHttpStatus(), ErrorResponse.of(code));
  }
}
