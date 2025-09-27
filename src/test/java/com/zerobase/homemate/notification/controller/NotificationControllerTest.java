package com.zerobase.homemate.notification.controller;

import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.entity.enums.NotificationStatus;
import com.zerobase.homemate.exception.GlobalExceptionHandler;
import com.zerobase.homemate.notification.dto.NotificationDto;
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
import java.util.ArrayList;
import java.util.List;

import static com.zerobase.homemate.entity.enums.NotificationCategory.CHORE;
import static com.zerobase.homemate.entity.enums.NotificationCategory.NOTICE;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    private List<NotificationDto> notificationDtos = new ArrayList<>();

    @BeforeEach
    void setUp() {
        // 인증 정보 주입
        var principal = new UserPrincipal(1L, "tester", "USER");
        var auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.authorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 테스트 데이터 생성
        LocalDateTime baseDateTime = LocalDateTime.now().withHour(12).withMinute(0).withSecond(0);

        notificationDtos = List.of(
                new NotificationDto(1L, 1L, 1L, 1L, CHORE, "화장실 청소", "", baseDateTime.plusDays(1), NotificationStatus.SCHEDULED, false, null, null, null),
                new NotificationDto(2L, 1L, 2L, 2L, CHORE, "쓰레기 버리기", "", baseDateTime.minusDays(1), NotificationStatus.SENT, true, baseDateTime.minusDays(1).plusHours(6), null, null),
                new NotificationDto(3L, null, null, null, NOTICE, "공지 사항", "", baseDateTime, NotificationStatus.SENT, false, null, null, null),
                new NotificationDto(4L, 1L, 2L, 3L, CHORE, "쓰레기 버리기", "", baseDateTime.plusDays(3), NotificationStatus.SCHEDULED, false, null, null, null)
        );
    }

    @Nested
    @DisplayName("GET /notifications")
    class GetNotifications {

        @Test
        @DisplayName("(no category) -> 200 OK")
        void success_WithNoCategory() throws Exception {
            // given
            Long userId = 1L;
            when(notificationService.getNotifications(userId)).thenReturn(notificationDtos);

            // when & then
            mockMvc.perform(get("/notifications"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(notificationDtos.size())))
                    .andExpect(jsonPath("$[*].notificationCategory", hasItem(CHORE.toString())))
                    .andExpect(jsonPath("$[*].notificationCategory", hasItem(NOTICE.toString())));
        }

        @Test
        @DisplayName("?category=ALL -> 200 OK")
        void success_WithCategoryAll() throws Exception {
            // given
            Long userId = 1L;
            when(notificationService.getNotifications(userId)).thenReturn(notificationDtos);

            // when & then
            mockMvc.perform(get("/notifications?category=ALL"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(notificationDtos.size())))
                    .andExpect(jsonPath("$[*].notificationCategory", hasItem(CHORE.toString())))
                    .andExpect(jsonPath("$[*].notificationCategory", hasItem(NOTICE.toString())));
        }

        @Test
        @DisplayName("?category=CHORE -> 200 OK")
        void success_WithCategoryChore() throws Exception {
            // given
            Long userId = 1L;
            List<NotificationDto> choreList = notificationDtos.stream().filter(e -> CHORE.equals(e.getNotificationCategory())).toList();
            when(notificationService.getNotificationsByCategory(userId, CHORE)).thenReturn(choreList);

            // when & then
            mockMvc.perform(get("/notifications?category=CHORE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(choreList.size())))
                    .andExpect(jsonPath("$[*].notificationCategory", hasItem(CHORE.toString())))
                    .andExpect(jsonPath("$[*].notificationCategory", not(hasItem(NOTICE.toString()))));
        }

        @Test
        @DisplayName("?category=NOTICE -> 200 OK")
        void success_WithCategoryNotice() throws Exception {
            // given
            Long userId = 1L;
            List<NotificationDto> noticeList = notificationDtos.stream().filter(e -> NOTICE.equals(e.getNotificationCategory())).toList();
            when(notificationService.getNotificationsByCategory(userId, NOTICE)).thenReturn(noticeList);

            // when & then
            mockMvc.perform(get("/notifications?category=NOTICE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(noticeList.size())))
                    .andExpect(jsonPath("$[*].notificationCategory", hasItem(NOTICE.toString())))
                    .andExpect(jsonPath("$[*].notificationCategory", not(hasItem(CHORE.toString()))));
        }

        @Test
        @DisplayName("?category=WRONG -> 400 Bad Request")
        void success_WithWrongCategory() throws Exception {
            // given
            Long userId = 1L;

            // when & then
            mockMvc.perform(get("/notifications?category=WRONG"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(notificationService);
        }
    }
}