package com.zerobase.homemate.recommend;

import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.enums.UserRole;
import com.zerobase.homemate.entity.enums.UserStatus;
import com.zerobase.homemate.recommend.controller.RecommendController;
import com.zerobase.homemate.recommend.dto.SpaceChoreResponse;
import com.zerobase.homemate.recommend.dto.TopItemDto;
import com.zerobase.homemate.recommend.service.RecommendService;
import com.zerobase.homemate.recommend.service.stats.ChoreStatsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecommendController.class)
@WithMockUser(username = "tester", roles = "USER")
public class RecommendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecommendService recommendService;

    @MockitoBean
    private ChoreStatsService choreStatsService;

    @Test
    void getRandomChores_shouldReturnThreeChores() throws Exception {
        // given - 익명 클래스 방식으로 두 필드 구현
        SpaceChoreResponse sc1 = new SpaceChoreResponse() {
            public Long getId() { return 1L; }
            public String getTitleKo() { return "주방 싱크대 청소하기"; }
        };
        SpaceChoreResponse sc2 = new SpaceChoreResponse() {
            public Long getId() { return 2L; }
            public String getTitleKo() { return "신발정리"; }
        };
        SpaceChoreResponse sc3 = new SpaceChoreResponse() {
            public Long getId() { return 3L; }
            public String getTitleKo() { return "환기하기"; }
        };

        when(recommendService.getRandomChores()).thenReturn(List.of(sc1, sc2, sc3));

        mockMvc.perform(get("/recommend/random")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titleKo").value("주방 싱크대 청소하기"))
                .andExpect(jsonPath("$[1].titleKo").value("신발정리"))
                .andExpect(jsonPath("$[2].titleKo").value("환기하기"));

    }

    @Test
    @DisplayName("유저별 미션 달성 집안일 + Top N 조회 (집안일 개수 포함)")
    void testGetTopOverall() throws Exception {
        // given
        User user = User.builder()
                .userRole(UserRole.USER)
                .id(1L)
                .userStatus(UserStatus.ACTIVE)
                .profileName("테스트")
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);


        List<TopItemDto> topList = List.of(
                new TopItemDto("미션 달성 집안일", "MISSIONS", 3L),
                new TopItemDto("기타 집안일", "ETC", 8L),
                new TopItemDto("15분 청소", "FIFTEEN", 6L),
                new TopItemDto("겨울철 집안일", "WINTER", 4L),
                new TopItemDto("주방", "KITCHEN", 2L)
        );

        when(choreStatsService.getTopOverallWithMissions(user.getId(), 5))
                .thenReturn(topList);

        // when & then
        mockMvc.perform(get("/recommend/trend")
                        .param("userId", String.valueOf(user.getId()))
                        .param("topN", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // name 검증
                .andExpect(jsonPath("$[0].name").value("미션 달성 집안일"))
                .andExpect(jsonPath("$[1].name").value("기타 집안일"))
                .andExpect(jsonPath("$[2].name").value("15분 청소"))
                // code 검증
                .andExpect(jsonPath("$[0].code").value("MISSIONS"))
                .andExpect(jsonPath("$[1].code").value("ETC"))
                .andExpect(jsonPath("$[2].code").value("FIFTEEN"))
                // count 검증
                .andExpect(jsonPath("$[0].count").value(3))
                .andExpect(jsonPath("$[1].count").value(8))
                .andExpect(jsonPath("$[2].count").value(6));

        verify(choreStatsService, times(1)).getTopOverallWithMissions(user.getId(), 5);
    }


}
