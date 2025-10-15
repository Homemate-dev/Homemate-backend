package com.zerobase.homemate.push.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.entity.enums.DeviceType;
import com.zerobase.homemate.push.dto.FcmTokenDto;
import com.zerobase.homemate.push.service.FcmTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FcmTokenController.class)
@WithMockUser(username = "user1", roles = "USER")
class FcmTokenControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    FcmTokenService fcmTokenService;

    @BeforeEach
    void setUp() {
        // 인증 정보 주입
        var principal = new UserPrincipal(1L, "tester", "USER");
        var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.authorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Nested
    @DisplayName("POST /push/subscriptions")
    class Subscribe {

        public static final String PATH = "/push/subscriptions";

        @Test
        @DisplayName(" -> 200 OK")
        void success() throws Exception {
            // given
            Long userId = 1L;
            String token = "test-token";
            DeviceType deviceType = DeviceType.WEB;

            FcmTokenDto.Request request = new FcmTokenDto.Request();
            ReflectionTestUtils.setField(request, "token", token);
            ReflectionTestUtils.setField(request, "deviceType", deviceType);

            FcmTokenDto.Response result = FcmTokenDto.Response.builder()
                    .id(1L)
                    .userId(1L)
                    .token(token)
                    .deviceType(deviceType)
                    .isActive(true)
                    .lastUsedAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(fcmTokenService.registerToken(eq(userId), any(FcmTokenDto.Request.class))).thenReturn(result);

            // when & then
            mockMvc.perform(post(PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token", equalTo(token)));
        }
    }

    @Nested
    @DisplayName("DELETE /push/subscriptions")
    class Unsubscribe {

        public static final String PATH = "/push/subscriptions";

        @Test
        @DisplayName(" -> 204 No Content")
        public void success() throws Exception {
            // given
            String token = "test-token";
            FcmTokenDto.Request request = new FcmTokenDto.Request();
            ReflectionTestUtils.setField(request, "token", token);

            // when & then
            mockMvc.perform(delete(PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isNoContent());
        }
    }
}