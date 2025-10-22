package com.zerobase.homemate.badge;

import com.zerobase.homemate.badge.controller.BadgeController;
import com.zerobase.homemate.badge.service.BadgeService;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.enums.BadgeType;
import com.zerobase.homemate.entity.enums.UserRole;
import com.zerobase.homemate.entity.enums.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = { BadgeController.class })
@WithMockUser(username = "test")
public class BadgeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BadgeService badgeService;

    @Test
    @DisplayName("GET / badges 요청 시 획득 배지와 잠금 배지 반환")
    void testGetAcquiredBadges() throws Exception {
        // given
        User user = User.builder()
                .id(1L)
                .profileName("test")
                .userRole(UserRole.USER)
                .userStatus(UserStatus.ACTIVE)
                .build();

        when(badgeService.getAcquiredBadges(any()))
                .thenReturn(List.of(
                        new BadgeResponse(BadgeType.START_HALF, true, 0),
                        new BadgeResponse(BadgeType.BEGINNER_BATHROOM, true, 0),
                        new BadgeResponse(BadgeType.EXPERT_BATHROOM, false, 15)

                ));

        // when & then
        mockMvc.perform(get("/badges/acquired")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value(BadgeType.START_HALF.name()))
                .andExpect(jsonPath("$[0].acquired").value(true))
                .andExpect(jsonPath("$[0].remainingCount").value(0))
                .andExpect(jsonPath("$[1].type").value(BadgeType.BEGINNER_BATHROOM.name()))
                .andExpect(jsonPath("$[1].acquired").value(true))
                .andExpect(jsonPath("$[1].remainingCount").value(0))
                .andExpect(jsonPath("$[2].type").value(BadgeType.EXPERT_BATHROOM.name()))
                .andExpect(jsonPath("$[2].acquired").value(false))
                .andExpect(jsonPath("$[2].remainingCount").value(15));
    }

    @Test
    @DisplayName("남은 횟수가 적은 3개 배지를 API로 조회한다")
    void testGetClosestBadges() throws Exception {
        // given
        List<BadgeResponse> mockBadges = List.of(
                new BadgeResponse(BadgeType.SEED_MISSION, false, 1),
                new BadgeResponse(BadgeType.CHECK_FIRE_EXHAUSTER, false, 2),
                new BadgeResponse(BadgeType.SEED_LAUNDRY, false, 3)
        );
        when(badgeService.getClosestBadges(any()))
                .thenReturn(mockBadges);

        // when & then
        mockMvc.perform(get("/badges/closest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].remainingCount").value(1))
                .andExpect(jsonPath("$[1].remainingCount").value(2))
                .andExpect(jsonPath("$[2].remainingCount").value(3));
    }
}
