package com.zerobase.homemate.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
  @Value("${http.client.connect-timeout-ms:3000}")
  private int connectTimeout;
  @Value("${http.client.read-timeout-ms:5000}")
  private int readTimeout;

  @Bean
  public RestClient restClient(RestClient.Builder builder) {
    var factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(connectTimeout);
    factory.setReadTimeout(readTimeout);
    return builder.requestFactory(factory).build();
  }
}
