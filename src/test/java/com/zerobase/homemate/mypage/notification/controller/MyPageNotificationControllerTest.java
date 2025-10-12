package com.zerobase.homemate.mypage.notification.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.mypage.notification.dto.FirstSetupStatusDto.FirstSetupStatusResponse;
import com.zerobase.homemate.mypage.notification.dto.NotificationTimeDto.NotiTimeResponse;
import com.zerobase.homemate.mypage.notification.service.MyPageNotificationService;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
        .andExpect(jsonPath("$.defaultTime").value("09:00"));

    then(myPageNotificationService).should().getFirstSetupStatus(userId);
    then(myPageNotificationService).shouldHaveNoMoreInteractions();
  }

  @Test
  @DisplayName("GET 알림 시간 조회 성공")
  void getTime_ok() throws Exception {
    long userId = 1L;
    var resp = new NotiTimeResponse(LocalTime.of(18,0), LocalDateTime.now());

    given(myPageNotificationService.getNotificationTime(userId)).willReturn(resp);

    Authentication auth = new UsernamePasswordAuthenticationToken(
        new UserPrincipal(userId, "nick", "ROLE_USER"), null, List.of());

    mockMvc.perform(get("/users/me/notification-settings/time")
            .with(SecurityMockMvcRequestPostProcessors.authentication(auth)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.notificationTime").value("18:00"))
        .andExpect(jsonPath("$.updatedAt").exists());

    then(myPageNotificationService).should().getNotificationTime(userId);
    then(myPageNotificationService).shouldHaveNoMoreInteractions();
  }
}
