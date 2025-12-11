package com.zerobase.homemate.badge;

import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.badge.controller.BadgeController;
import com.zerobase.homemate.badge.service.BadgeService;
import com.zerobase.homemate.entity.enums.BadgeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BadgeController.class)
class BadgeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BadgeService badgeService;

    @Test
    @DisplayName("획득한 뱃지를 정상 반환한다")
    void getAcquiredBadges() throws Exception {

        UserPrincipal principal = new UserPrincipal(1L, "mockUser", "ROLE_USER");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.authorities())
        );

        given(badgeService.getAcquiredBadges(principal.id())).willReturn(
                List.of(BadgeProgressResponse.of(BadgeType.START_HALF, 1, true, LocalDateTime.now()))
        );

        mockMvc.perform(get("/badges/acquired"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].badgeType").value("START_HALF"))
                .andExpect(jsonPath("$[0].acquired").value(true));
    }

    @Test
    @DisplayName("획득한 뱃지가 없으면 빈 배열을 반환")
    void getAcquiredBadges_empty() throws Exception {

        UserPrincipal principal = new UserPrincipal(1L, "mockUser", "ROLE_USER");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.authorities())
        );

        given(badgeService.getAcquiredBadges(principal.id()))
                .willReturn(List.of());

        mockMvc.perform(get("/badges/acquired"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("가장 가까운 뱃지 3개 반환")
    void getClosestBadges() throws Exception {

        UserPrincipal principal = new UserPrincipal(1L, "mockUser", "ROLE_USER");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.authorities())
        );

        List<BadgeProgressResponse> mockList = List.of(
                BadgeProgressResponse.of(BadgeType.EXPERT_BATHROOM, 2, false, null),
                BadgeProgressResponse.of(BadgeType.EXPERT_FAIRY, 88, false, null),
                BadgeProgressResponse.of(BadgeType.BEGINNER_KITCHEN, 25, false, null)
        );

        given(badgeService.getClosestBadgesCached(principal.id()))
                .willReturn(mockList);

        mockMvc.perform(get("/badges/closest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].badgeType").value("EXPERT_BATHROOM"))
                .andExpect(jsonPath("$[1].badgeType").value("EXPERT_FAIRY"))
                .andExpect(jsonPath("$[2].badgeType").value("BEGINNER_KITCHEN"));
    }

    @Test
    @DisplayName("모두 획득했다면 빈 배열 반환")
    void getClosestBadges_empty() throws Exception {

        UserPrincipal principal = new UserPrincipal(1L, "mockUser", "ROLE_USER");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.authorities())
        );

        given(badgeService.getClosestBadges(principal.id())).willReturn(List.of());

        mockMvc.perform(get("/badges/closest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("인증 안되면 401")
    void unauthorized() throws Exception {

        mockMvc.perform(get("/badges/acquired"))
                .andExpect(status().isUnauthorized());
    }
}
