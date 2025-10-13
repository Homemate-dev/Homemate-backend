package com.zerobase.homemate.mypage.notification.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.mypage.notification.dto.FirstSetupStatusDto.FirstSetupResponse;
import com.zerobase.homemate.mypage.notification.dto.FirstSetupStatusDto.FirstSetupStatusResponse;
import com.zerobase.homemate.mypage.notification.service.MyPageNotificationService;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MyPageNotificationController.class)
class MyPageNotificationControllerTest {
  @Autowired
  MockMvc mockMvc;

  @MockitoBean
  MyPageNotificationService myPageNotificationService;

  @Test
  @DisplayName("최초 알림 시간 설정 여부 조회 성공")
  void getStatus_ok() throws Exception {
    // given
    long userId = 1L;
    var resp = new FirstSetupStatusResponse(false, LocalTime.of(9, 0));
    given(myPageNotificationService.getFirstSetupStatus(userId)).willReturn(resp);

    UserPrincipal principal = new UserPrincipal(userId, "nick", "ROLE_USER");
    Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());

    // when & then
    mockMvc.perform(get("/users/me/notification-settings/first-setup-status")
            .with(SecurityMockMvcRequestPostProcessors.authentication(auth)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstSetupCompleted").value(false))
        .andExpect(jsonPath("$.notificationTime").value("09:00"));

    then(myPageNotificationService).should().getFirstSetupStatus(userId);
    then(myPageNotificationService).shouldHaveNoMoreInteractions();
  }

  @Test
  @DisplayName("POST 첫 설정 성공")
  void firstSetup_ok() throws Exception {
    // given
    long userId = 1L;
    var resp = new FirstSetupResponse(
        true,
        true,
        LocalTime.of(18,0),
        LocalDateTime.now()
    );
    given(myPageNotificationService.completeFirstSetup(
        eq(userId),eq(LocalTime.of(18, 0)))).willReturn(resp);

    UserPrincipal principal = new UserPrincipal(userId, "nick", "ROLE_USER");
    Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());

    // when & then
    mockMvc.perform(post("/users/me/notification-settings/first-setup")
            .with(SecurityMockMvcRequestPostProcessors.authentication(auth))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{ \"notificationTime\": \"18:00\" }"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstSetupCompleted").value(true))
        .andExpect(jsonPath("$.masterEnabled").value(true))
        .andExpect(jsonPath("$.notificationTime").value("18:00"))
        .andExpect(jsonPath("$.updatedAt").exists());

    then(myPageNotificationService).should()
        .completeFirstSetup(eq(userId), eq(LocalTime.of(18, 0)));
    then(myPageNotificationService).shouldHaveNoMoreInteractions();
  }

  @Test
  @DisplayName("POST 첫 설정: 이미 완료 -> 409 CONFLICT")
  void firstSetup_alreadyCompleted_conflict() throws Exception {
    // given
    long userId = 1L;
    given(myPageNotificationService.completeFirstSetup(eq(userId), any(LocalTime.class)
    )).willThrow(new CustomException(ErrorCode.FIRST_SETUP_ALREADY_COMPLETED));

    UserPrincipal principal = new UserPrincipal(userId, "nick", "ROLE_USER");
    Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());

    // when & then
    mockMvc.perform(post("/users/me/notification-settings/first-setup")
            .with(SecurityMockMvcRequestPostProcessors.authentication(auth))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{ \"notificationTime\": \"18:00\" }"))
        .andExpect(status().isConflict());

    then(myPageNotificationService).should()
        .completeFirstSetup(eq(userId), any(LocalTime.class));
  }
}
