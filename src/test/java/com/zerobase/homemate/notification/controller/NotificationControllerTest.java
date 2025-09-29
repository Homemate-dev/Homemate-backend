package com.zerobase.homemate.notification.controller;

import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.GlobalExceptionHandler;
import com.zerobase.homemate.notification.dto.NotificationDto;
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
import java.util.List;

import static com.zerobase.homemate.entity.enums.NotificationCategory.CHORE;
import static com.zerobase.homemate.entity.enums.NotificationCategory.NOTICE;
import static com.zerobase.homemate.entity.enums.NotificationStatus.SCHEDULED;
import static com.zerobase.homemate.entity.enums.NotificationStatus.SENT;
import static com.zerobase.homemate.exception.ErrorCode.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verifyNoInteractions;
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

    private List<NotificationDto> notificationDtos;

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
                new NotificationDto(1L, 1L, 1L, 1L, CHORE, "화장실 청소", "", baseDateTime.plusDays(1), SCHEDULED, false, null, null, null),
                new NotificationDto(2L, 1L, 2L, 2L, CHORE, "쓰레기 버리기", "", baseDateTime.minusDays(1), SENT, true, baseDateTime.minusDays(1).plusHours(6), null, null),
                new NotificationDto(3L, null, null, null, NOTICE, "공지 사항", "", baseDateTime, SENT, false, null, null, null),
                new NotificationDto(4L, 1L, 2L, 3L, CHORE, "쓰레기 버리기", "", baseDateTime.plusDays(3), SCHEDULED, false, null, null, null),
                new NotificationDto(5L, 2L, 3L, 4L, CHORE, "창문 닦기", "", baseDateTime.plusDays(3), SCHEDULED, false, null, null, null)
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
            List<NotificationDto> allList = notificationDtos.stream().filter(e -> userId.equals(e.getUserId())).toList();
            when(notificationService.getNotifications(userId)).thenReturn(allList);

            // when & then
            mockMvc.perform(get("/notifications"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(allList.size())))
                    .andExpect(jsonPath("$[*].notificationCategory", hasItem(CHORE.toString())));
        }

        @Test
        @DisplayName("?category=ALL -> 200 OK")
        void success_WithCategoryAll() throws Exception {
            // given
            Long userId = 1L;
            List<NotificationDto> allList = notificationDtos.stream().filter(e -> userId.equals(e.getUserId())).toList();
            when(notificationService.getNotifications(userId)).thenReturn(allList);

            // when & then
            mockMvc.perform(get("/notifications?category=ALL"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(allList.size())))
                    .andExpect(jsonPath("$[*].notificationCategory", hasItem(CHORE.toString())));
        }

        @Test
        @DisplayName("?category=CHORE -> 200 OK")
        void success_WithCategoryChore() throws Exception {
            // given
            Long userId = 1L;
            List<NotificationDto> choreList = notificationDtos.stream().filter(e -> userId.equals(e.getUserId()) && CHORE.equals(e.getNotificationCategory())).toList();
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
        void fail_WithWrongCategory() throws Exception {
            // given
            Long userId = 1L;

            // when & then
            mockMvc.perform(get("/notifications?category=WRONG"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code", equalTo(VALIDATION_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", equalTo("잘못된 카테고리 입력입니다."))); // NotificationCategory.from() 참고

            verifyNoInteractions(notificationService);
        }

        @Nested
        @DisplayName("PATCH /notifications/{notificationId}")
        class UpdateNotificationToRead {

            @Test
            @DisplayName(" -> 200 OK")
            void success() throws Exception {
                // given
                Long userId = 1L;
                Long notificationId = 1L;
                NotificationReadDto notificationReadDto = new NotificationReadDto(notificationId, true, LocalDateTime.now().withNano(0));
                when(notificationService.updateNotificationToRead(userId, notificationId)).thenReturn(notificationReadDto);

                // when & then
                mockMvc.perform(patch("/notifications/{notificationId}", notificationId).with(csrf()))
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
                when(notificationService.updateNotificationToRead(userId, notificationId))
                        .thenThrow(new CustomException(NOTIFICATION_NOT_FOUND));

                // when & then
                mockMvc.perform(patch("/notifications/{notificationId}", notificationId).with(csrf()))
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
                when(notificationService.updateNotificationToRead(userId, notificationId))
                        .thenThrow(new CustomException(FORBIDDEN, "해당 알림을 수정할 권한이 없습니다."));

                // when & then
                mockMvc.perform(patch("/notifications/{notificationId}", notificationId).with(csrf()))
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.error.code", equalTo(FORBIDDEN.getCode())))
                        .andExpect(jsonPath("$.error.message", equalTo("해당 알림을 수정할 권한이 없습니다.")));
            }

        }
    }
}