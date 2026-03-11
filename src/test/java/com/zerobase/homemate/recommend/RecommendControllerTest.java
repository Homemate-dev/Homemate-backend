package com.zerobase.homemate.recommend;

import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.entity.enums.UserRole;
import com.zerobase.homemate.recommend.controller.RecommendController;
import com.zerobase.homemate.recommend.dto.SpaceChoreResponse;
import com.zerobase.homemate.recommend.dto.TopItemDto;
import com.zerobase.homemate.recommend.service.RecommendService;
import com.zerobase.homemate.recommend.service.stats.ChoreStatsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
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
            public String getTitleKo() { return "주방 싱크대 청소하기";}
            public Space getSpace() { return Space.KITCHEN; }
        };
        SpaceChoreResponse sc2 = new SpaceChoreResponse() {
            public Long getId() { return 2L; }
            public String getTitleKo() { return "신발정리"; }
            public Space getSpace() { return Space.PORCH; }
        };
        SpaceChoreResponse sc3 = new SpaceChoreResponse() {
            public Long getId() { return 3L; }
            public String getTitleKo() { return "환기하기"; }
            public Space getSpace() { return Space.ETC; }
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
    void getTopCategories_success() throws Exception {
        // given
        UserPrincipal user = new UserPrincipal(1L, "Delcastin", UserRole.USER.name());

        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.authorities()
                );

        List<TopItemDto> mockResult = List.of(
                new TopItemDto("미션", Category.MISSIONS, 5L),
                new TopItemDto("WINTER", null, 10L),
                new TopItemDto("1월 추천 집안일", null, 7L)
        );

        when(choreStatsService.getTopCategories(1L))
                .thenReturn(mockResult);

        // when & then
        mockMvc.perform(get("/recommend/total")
                        .with(authentication(auth)))   // 👈 핵심
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))

                // 1번: 미션
                .andExpect(jsonPath("$[0].name").value("미션"))
                .andExpect(jsonPath("$[0].category").value("MISSIONS"))
                .andExpect(jsonPath("$[0].count").value(5))

                // 2번: 시즌
                .andExpect(jsonPath("$[1].name").value("WINTER"))
                .andExpect(jsonPath("$[1].category").doesNotExist())
                .andExpect(jsonPath("$[1].count").value(10))

                // 3번: 월간
                .andExpect(jsonPath("$[2].name").value("1월 추천 집안일"))
                .andExpect(jsonPath("$[2].category").doesNotExist())
                .andExpect(jsonPath("$[2].count").value(7));
    }


}
