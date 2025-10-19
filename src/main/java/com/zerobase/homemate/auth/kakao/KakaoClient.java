package com.zerobase.homemate.auth.kakao;

import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoClient {
  private final RestClient restClient;
  private final KakaoOAuthProperty property;

  // 인가코드로 액세스 토큰 교환
  public KakaoDto.TokenResponse exchangeToken(String authorizationCode, String redirectUri, String codeVerifier) {
    log.info("[KAKAO][REQ] token-exchange code.len={}, redirectUri='{}', codeVerifier.len={}, tokenUri={}",
        (authorizationCode == null ? "null" : authorizationCode.length()),
        redirectUri,
        (codeVerifier == null ? "null" : codeVerifier.length()),
        property.tokenUri()
    );
    
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

      log.info("[KAKAO][TOKEN] OK hasAccessToken={}, expiresIn={}",
          response != null && response.accessToken() != null,
          response != null ? response.expiresIn() : null
      );

      // 비즈니스 유효성 체크
      if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
        throw new CustomException(ErrorCode.PROVIDER_RESPONSE_MALFORMED);
      }
      return response;

    } catch (HttpClientErrorException e) {
      String body = e.getResponseBodyAsString();
      String head = (body == null) ? "null" : body.substring(0, Math.min(240, body.length())).replaceAll("\\s+"," ");
      log.warn("[KAKAO][TOKEN] HTTP {} {} body.head={}", e.getStatusCode().value(), e.getStatusText(), head);
      
      int sc = e.getStatusCode().value();
      if (sc == 401) throw new CustomException(ErrorCode.UNAUTHORIZED);
      if (sc == 403) throw new CustomException(ErrorCode.FORBIDDEN);
      if (sc == 429) throw new CustomException(ErrorCode.PROVIDER_RATE_LIMIT);

      if (body.contains("redirect_uri")) {
        log.warn("[KAKAO][HINT] redirect_uri mismatch 의심. 요청 redirectUri='{}'", redirectUri);
      }
      if (body.contains("PKCE") || body.contains("code_verifier")) {
        log.warn("[KAKAO][HINT] PKCE 검증 실패 의심. codeVerifier.len={}", (codeVerifier == null ? "null" : codeVerifier.length()));
      }
      if (body.toLowerCase().contains("already")) {
        log.warn("[KAKAO][HINT] authorization code 재사용/만료 의심");
      }
      throw new CustomException(ErrorCode.INVALID_AUTH_CODE);
    } catch (HttpServerErrorException e) {
      String body = e.getResponseBodyAsString();
      String head = body.substring(0, Math.min(240, body.length())).replaceAll("\\s+", " ");
      log.error("[KAKAO][TOKEN] HTTP {} {} body.head={}", e.getStatusCode().value(), e.getStatusText(), head);
      throw new CustomException(ErrorCode.PROVIDER_UNAVAILABLE);
    } catch (HttpMessageConversionException e) {
      log.error("[KAKAO][TOKEN] parse error", e);
      throw new CustomException(ErrorCode.PROVIDER_RESPONSE_MALFORMED);
    } catch (RestClientException e) {
      log.error("[KAKAO][TOKEN] comm error", e);
      throw new CustomException(ErrorCode.PROVIDER_COMM_ERROR);
    }
  }

  // 액세스 토큰으로 사용자 프로필 조회
  public KakaoDto.ProfileResponse fetchProfile(String kakaoAccessToken) {
    log.info("[KAKAO][REQ] user/me authHeader='Bearer ...', profileUri={}", property.profileUri());
    
    try {
      KakaoDto.ProfileResponse response = restClient.get()
          .uri(property.profileUri())
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + kakaoAccessToken)
          .retrieve()
          .body(KakaoDto.ProfileResponse.class);

      log.info("[KAKAO][PROFILE] OK id={}, hasProps={}",
          (response != null ? response.id() : null),
          (response != null && response.properties() != null)
      );

      if (response == null || response.id() == null) {
        throw new CustomException(ErrorCode.PROVIDER_RESPONSE_MALFORMED);
      }
      return response;

    } catch (HttpClientErrorException e) {
      String body = e.getResponseBodyAsString();
      String head = body.substring(0, Math.min(200, body.length())).replaceAll("\\s+", " ");
      log.warn("[KAKAO][PROFILE] HTTP {} {} body.head={}",
          e.getStatusCode().value(), e.getStatusText(), head);
      
      int sc = e.getStatusCode().value();
      if (sc == 401) throw new CustomException(ErrorCode.UNAUTHORIZED);
      if (sc == 403) throw new CustomException(ErrorCode.FORBIDDEN);
      if (sc == 429) throw new CustomException(ErrorCode.PROVIDER_RATE_LIMIT);
      throw new CustomException(ErrorCode.INVALID_PROVIDER_TOKEN);
    } catch (HttpServerErrorException e) {
      String body = e.getResponseBodyAsString();
      String head = body.substring(0, Math.min(200, body.length())).replaceAll("\\s+", " ");
      log.error("[KAKAO][PROFILE] HTTP {} {} body.head={}",
          e.getStatusCode().value(), e.getStatusText(), head);
      throw new CustomException(ErrorCode.PROVIDER_UNAVAILABLE);
    } catch (HttpMessageConversionException e) {
      log.error("[KAKAO][PROFILE] parse error", e);
      throw new CustomException(ErrorCode.PROVIDER_RESPONSE_MALFORMED);
    } catch (RestClientException e) {
      log.error("[KAKAO][PROFILE] comm error", e);
      throw new CustomException(ErrorCode.PROVIDER_COMM_ERROR);
    }
  }
}
