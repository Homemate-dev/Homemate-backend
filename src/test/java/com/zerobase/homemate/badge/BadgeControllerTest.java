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

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BadgeController.class)
public class BadgeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BadgeService badgeService;

    @Test
    @DisplayName("획득한 뱃지 목록을 출력한다. -> 첫 시작 등록 뱃지 획득 상태")
    void testGetMyBadges_withMockPrincipal_shouldReturn200() throws Exception {
        UserPrincipal principal = new UserPrincipal(1L, "mockUser", "USER");

        // SecurityContext에 직접 인증 정보 세팅
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.authorities())
        );

        // BadgeService mocking
        given(badgeService.getAcquiredBadges(principal.id())).willReturn(
                List.of(
                        BadgeProgressResponse.of(BadgeType.START_HALF, 1) // currentCount=1 → acquired=true, remainingCount=0
                )
        );

        mockMvc.perform(get("/badges/acquired"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].badgeType").value(BadgeType.START_HALF.name()))
                .andExpect(jsonPath("$[0].acquired").value(true))
                .andExpect(jsonPath("$[0].description").value("아무 집안일 1회 완료"))
                .andExpect(jsonPath("$[0].currentCount").value(1))
                .andExpect(jsonPath("$[0].requiredCount").value(1))
                .andExpect(jsonPath("$[0].remainingCount").value(0));
    }

    @Test
    @DisplayName("빈 리스트 출력")
    void testGetMyBadges_withMockPrincipal_emptyList() throws Exception {
        UserPrincipal principal = new UserPrincipal(1L, "mockUser", "USER");

        // SecurityContext에 직접 인증 정보 세팅
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.authorities())
        );

        // BadgeService mocking
        given(badgeService.getAcquiredBadges(principal.id())).willReturn(List.of());
        mockMvc.perform(get("/badges/acquired"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

    }

    @Test
    void testGetClosestBadges_withBadges_shouldReturn200() throws Exception {
        UserPrincipal principal = new UserPrincipal(1L, "mockUser", "USER");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.authorities())
        );

        List<BadgeProgressResponse> closest = List.of(
                BadgeProgressResponse.of(BadgeType.EXPERT_BATHROOM, 2), // remaining = 1
                BadgeProgressResponse.of(BadgeType.EXPERT_FAIRY, 88),   // remaining = 2
                BadgeProgressResponse.of(BadgeType.BEGINNER_KITCHEN, 25) // remaining = 5
        );

        given(badgeService.getClosestBadges(principal.id())).willReturn(closest);

        mockMvc.perform(get("/badges/closest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].badgeType").value(BadgeType.EXPERT_BATHROOM.name()))
                .andExpect(jsonPath("$[0].acquired").value(false))
                .andExpect(jsonPath("$[0].remainingCount").value(1))
                .andExpect(jsonPath("$[1].badgeType").value(BadgeType.EXPERT_FAIRY.name()))
                .andExpect(jsonPath("$[1].acquired").value(false))
                .andExpect(jsonPath("$[1].remainingCount").value(2))
                .andExpect(jsonPath("$[2].badgeType").value(BadgeType.BEGINNER_KITCHEN.name()))
                .andExpect(jsonPath("$[2].acquired").value(false))
                .andExpect(jsonPath("$[2].remainingCount").value(5));
    }

    @Test
    @DisplayName("모든 뱃지를 받은 사람이 받게 될 빈 리스트 출력")
    void testGetClosestBadges_emptyList_shouldReturn200() throws Exception {
        UserPrincipal principal = new UserPrincipal(1L, "mockUser", "USER");
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
    @DisplayName("인증 안될 시 401 Error 반환")
    void testUnauthorized_shouldReturn401() throws Exception {
        // 인증 안 된 경우 SecurityContext를 비워둠
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/badges/acquired"))
                .andExpect(status().isUnauthorized());
    }
}
