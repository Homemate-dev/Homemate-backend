package com.zerobase.homemate.auth.kakao;

import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class KakaoClient {
  private final RestClient restClient;
  private final KakaoOAuthProperty property;

  // 인가코드로 액세스 토큰 교환
  public KakaoDto.TokenResponse exchangeToken(String authorizationCode, String redirectUri, String codeVerifier) {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("grant_type", "authorization_code");
    form.add("client_id", property.clientId());
    if (property.clientSecret() != null && !property.clientSecret().isBlank()) {
      form.add("client_secret", property.clientSecret());
    }
    form.add("code", authorizationCode);
    form.add("redirect_uri", redirectUri);
    form.add("code_verifier", codeVerifier);  // PKCE 사용

    try {
      KakaoDto.TokenResponse response = restClient.post()
          .uri(property.tokenUri())
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .body(form)
          .retrieve()
          .body(KakaoDto.TokenResponse.class);

      // 비즈니스 유효성 체크
      if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
        throw new CustomException(ErrorCode.PROVIDER_RESPONSE_MALFORMED);
      }
      return response;

    } catch (HttpClientErrorException e) {
      int sc = e.getStatusCode().value();
      if (sc == 401) throw new CustomException(ErrorCode.UNAUTHORIZED);
      if (sc == 403) throw new CustomException(ErrorCode.FORBIDDEN);
      if (sc == 429) throw new CustomException(ErrorCode.PROVIDER_RATE_LIMIT);
      throw new CustomException(ErrorCode.INVALID_AUTH_CODE);
    } catch (HttpServerErrorException e) {
      throw new CustomException(ErrorCode.PROVIDER_UNAVAILABLE);
    } catch (HttpMessageConversionException e) {
      throw new CustomException(ErrorCode.PROVIDER_RESPONSE_MALFORMED);
    } catch (RestClientException e) {
      throw new CustomException(ErrorCode.PROVIDER_COMM_ERROR);
    }
  }

  // 액세스 토큰으로 사용자 프로필 조회
  public KakaoDto.ProfileResponse fetchProfile(String kakaoAccessToken) {
    try {
      KakaoDto.ProfileResponse response = restClient.get()
          .uri(property.profileUri())
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + kakaoAccessToken)
          .retrieve()
          .body(KakaoDto.ProfileResponse.class);

      if (response == null || response.id() == null) {
        throw new CustomException(ErrorCode.PROVIDER_RESPONSE_MALFORMED);
      }
      return response;

    } catch (HttpClientErrorException e) {
      int sc = e.getStatusCode().value();
      if (sc == 401) throw new CustomException(ErrorCode.UNAUTHORIZED);
      if (sc == 403) throw new CustomException(ErrorCode.FORBIDDEN);
      if (sc == 429) throw new CustomException(ErrorCode.PROVIDER_RATE_LIMIT);
      throw new CustomException(ErrorCode.INVALID_PROVIDER_TOKEN);
    } catch (HttpServerErrorException e) {
      throw new CustomException(ErrorCode.PROVIDER_UNAVAILABLE);
    } catch (HttpMessageConversionException e) {
      throw new CustomException(ErrorCode.PROVIDER_RESPONSE_MALFORMED);
    } catch (RestClientException e) {
      throw new CustomException(ErrorCode.PROVIDER_COMM_ERROR);
    }
  }
}
