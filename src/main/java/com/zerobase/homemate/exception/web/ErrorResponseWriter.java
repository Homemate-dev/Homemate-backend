package com.zerobase.homemate.exception.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ErrorResponseWriter {
  private final ObjectMapper objectMapper;

  public void writeJson(HttpServletResponse response, HttpStatus status, Object body) throws IOException {
    response.setStatus(status.value());
    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");
    objectMapper.writeValue(response.getWriter(), body);
  }
}
