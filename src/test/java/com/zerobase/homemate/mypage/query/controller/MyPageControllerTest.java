package com.zerobase.homemate.mypage.query.controller;

import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.entity.enums.SocialProvider;
import com.zerobase.homemate.mypage.query.dto.MyPageResponseDto;
import com.zerobase.homemate.mypage.query.service.MyPageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MyPageController.class)
class MyPageControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MyPageService myPageService;

    @Test
    @DisplayName("마이페이지 조회 성공")
    void getMe_ok() throws Exception {
        // given
        long userId = 1L;
        var dto = new MyPageResponseDto(
                1L,
                SocialProvider.KAKAO,
                "Nickname",
                "https://img",
                LocalDateTime.of(2025, 9, 19, 7, 10),
                LocalDateTime.of(2025, 9, 19, 7, 10),
                LocalDateTime.of(2025, 9, 19, 7, 10),
                false,  // masterEnabled
                true,   // choreEnabled
                false,  // noticeEnabled
                LocalTime.of(18, 0),
                30,
                1
        );
        given(myPageService.getMyPage(userId)).willReturn(dto);

        UserPrincipal principal = new UserPrincipal(userId, "nick", "ROLE_USER");
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());

        // when & then
        mockMvc.perform(get("/users/me")
                        .with(authentication(auth))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.provider").value("KAKAO"))
                .andExpect(jsonPath("$.nickname").value("Nickname"))
                .andExpect(jsonPath("$.profileImageUrl").value("https://img"))
                .andExpect(jsonPath("$.masterEnabled").value(false))
                .andExpect(jsonPath("$.choreEnabled").value(true))
                .andExpect(jsonPath("$.noticeEnabled").value(false))
                .andExpect(jsonPath("$.notificationTime").value("18:00"))
                .andExpect(jsonPath("$.totalBadgeCount").value(30))
                .andExpect(jsonPath("$.acquiredBadgeCount").value(1));

        then(myPageService).should().getMyPage(userId);
        then(myPageService).shouldHaveNoMoreInteractions();
    }
}
