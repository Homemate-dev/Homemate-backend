package com.zerobase.homemate.notification.controller;

import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.GlobalExceptionHandler;
import com.zerobase.homemate.notification.dto.ChoreNotificationDto;
import com.zerobase.homemate.notification.dto.NotificationReadDto;
import com.zerobase.homemate.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import static com.zerobase.homemate.exception.ErrorCode.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@Import(GlobalExceptionHandler.class)
@WithMockUser(username = "user1", roles = "USER")
class NotificationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    NotificationService notificationService;

    private List<ChoreNotificationDto> choreNotificationDtoList;

    @BeforeEach
    void setUp() {
        // 인증 정보 주입
        var principal = new UserPrincipal(1L, "tester", "USER");
        var auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.authorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 테스트 데이터 생성
        LocalDateTime baseDateTime = LocalDateTime.now().withHour(12).withMinute(0).withSecond(0).withNano(0);

        choreNotificationDtoList = List.of(
            new ChoreNotificationDto(1L, 1L, "이불 빨래", "오늘은 이불 빨래가 예정되어 있습니다.", baseDateTime.minusDays(1), true, baseDateTime.minusDays(1).plusHours(1), null),
            new ChoreNotificationDto(2L, 2L, "화장실 청소", "오늘은 화장실 청소하는 날입니다.", baseDateTime.minusHours(1), false, null, null)
        );
    }

    @Nested
    @DisplayName("GET /notifications/chores")
    class GetChoreNotifications {

        private static final String PATH = "/notifications/chores";

        @Test
        @DisplayName(" -> 200 OK")
        void success() throws Exception {
            // given
            Long userId = 1L;
            List<ChoreNotificationDto> result = choreNotificationDtoList.stream()
                    .sorted(Comparator.comparing(ChoreNotificationDto::getScheduledAt).reversed())
                    .toList();
            when(notificationService.getChoreNotifications(userId)).thenReturn(result);

            // when & then
            mockMvc.perform(get(PATH))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(result.size())));
        }

    }

    @Nested
    @DisplayName("PATCH /notifications/chores/{notificationId}")
    class ReadChoreNotification {

        private static final String PATH = "/notifications/chores/{notificationId}";

        @Test
        @DisplayName(" -> 200 OK")
        void success() throws Exception {
            // given
            Long userId = 1L;
            Long notificationId = 1L;
            NotificationReadDto notificationReadDto = new NotificationReadDto(notificationId, true, LocalDateTime.now().withNano(0));
            when(notificationService.updateChoreNotificationToRead(userId, notificationId)).thenReturn(notificationReadDto);

            // when & then
            mockMvc.perform(patch(PATH, notificationId).with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(notificationId.intValue())))
                    .andExpect(jsonPath("$.isRead", equalTo(notificationReadDto.getIsRead())))
                    .andExpect(jsonPath("$.readAt", equalTo(notificationReadDto.getReadAt().toString())));
        }

        @Test
        @DisplayName("(wrong notificationId) -> 404 Not Found")
        void fail_WithWrongNotificationId() throws Exception {
            // given
            Long userId = 1L;
            Long notificationId = 999L;
            when(notificationService.updateChoreNotificationToRead(userId, notificationId))
                    .thenThrow(new CustomException(NOTIFICATION_NOT_FOUND));

            // when & then
            mockMvc.perform(patch(PATH, notificationId).with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code", equalTo(NOTIFICATION_NOT_FOUND.getCode())))
                    .andExpect(jsonPath("$.error.message", equalTo(NOTIFICATION_NOT_FOUND.getMessage())));
        }

        @Test
        @DisplayName("(userId - notificationId not Match) -> 403 Forbidden")
        void fail_UserIdNotMatchesWithNotificationId() throws Exception {
            // given
            Long userId = 1L;
            Long notificationId = 5L;
            when(notificationService.updateChoreNotificationToRead(userId, notificationId))
                    .thenThrow(new CustomException(FORBIDDEN, "해당 알림을 수정할 권한이 없습니다."));

            // when & then
            mockMvc.perform(patch(PATH, notificationId).with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code", equalTo(FORBIDDEN.getCode())))
                    .andExpect(jsonPath("$.error.message", equalTo("해당 알림을 수정할 권한이 없습니다.")));
        }
    }
}